import { createServer } from "node:http";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { FileEventStore, ValidationError } from "./store.mjs";

const moduleDirectory = fileURLToPath(new URL(".", import.meta.url));
const defaultDataFile = resolve(moduleDirectory, "../data/events.json");

function send(response, status, value) {
  const body = JSON.stringify(value);
  response.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(body),
    "Cache-Control": "no-store",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type, X-Admin-Key",
    "Access-Control-Allow-Methods": "GET, PUT, OPTIONS",
  });
  response.end(body);
}

async function jsonBody(request, limit = 1_000_000) {
  const chunks = [];
  let size = 0;
  for await (const chunk of request) {
    size += chunk.length;
    if (size > limit) throw new ValidationError("request body exceeds 1 MB");
    chunks.push(chunk);
  }
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch {
    throw new ValidationError("request body must be valid JSON");
  }
}

function isAuthorised(request, adminApiKey) {
  return !adminApiKey || request.headers["x-admin-key"] === adminApiKey;
}

export function createRequestHandler({ store, adminApiKey = "" }) {
  return async (request, response) => {
    try {
      const url = new URL(request.url ?? "/", `http://${request.headers.host ?? "localhost"}`);
      if (request.method === "OPTIONS") return send(response, 204, {});
      if (request.method === "GET" && url.pathname === "/health") {
        return send(response, 200, { status: "ok" });
      }
      if (request.method === "GET" && url.pathname === "/api/events") {
        const snapshot = await store.read();
        const name = (url.searchParams.get("name") ?? "").trim().toLocaleLowerCase();
        const date = (url.searchParams.get("date") ?? "").trim();
        const events = snapshot.events.filter(
          (event) =>
            (!name ||
              event.name.toLocaleLowerCase().includes(name) ||
              event.description.toLocaleLowerCase().includes(name)) &&
            (!date || event.date === date),
        );
        return send(response, 200, { events, updatedAt: snapshot.updatedAt });
      }
      if (request.method === "PUT" && url.pathname === "/api/events") {
        if (!isAuthorised(request, adminApiKey)) {
          return send(response, 401, { error: "invalid admin key" });
        }
        const body = await jsonBody(request);
        const snapshot = await store.replace(body.events);
        return send(response, 200, {
          message: `Synchronised ${snapshot.events.length} event(s)`,
          eventCount: snapshot.events.length,
          updatedAt: snapshot.updatedAt,
        });
      }
      return send(response, 404, { error: "route not found" });
    } catch (error) {
      if (error instanceof ValidationError) {
        return send(response, 400, { error: error.message });
      }
      console.error(error);
      return send(response, 500, { error: "internal server error" });
    }
  };
}

export function createEventServer(options = {}) {
  const store = options.store ?? new FileEventStore(options.dataFile ?? defaultDataFile);
  return createServer(
    createRequestHandler({ store, adminApiKey: options.adminApiKey ?? process.env.ADMIN_API_KEY ?? "" }),
  );
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const port = Number(process.env.PORT ?? 8080);
  const host = process.env.HOST ?? "0.0.0.0";
  const server = createEventServer({ dataFile: process.env.DATA_FILE ?? defaultDataFile });
  server.listen(port, host, () => {
    console.log(`Event Manager API listening on http://${host}:${port}`);
  });
}
