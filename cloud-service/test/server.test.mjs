import assert from "node:assert/strict";
import { mkdtemp, readFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";
import { once } from "node:events";
import { createEventServer } from "../src/server.mjs";

const example = {
  id: 7,
  name: "Greenwich Mobile Conference",
  date: "2026-11-18",
  organiser: "Computing Society",
  location: "Greenwich Campus",
  eventType: "Conference",
  status: "Upcoming",
  description: "Mobile development sessions",
  contactEmail: "events@example.org",
  contactPhone: "+44 20 8331 8000",
  updatedAt: 1794950400000,
  sessions: [
    {
      id: 3,
      name: "Opening Keynote",
      startMinutes: 570,
      speaker: "Dr Maya Chen",
      room: "QA 180",
      type: "Keynote",
      description: "Opening talk",
      updatedAt: 1794950400000,
    },
  ],
};

async function fixture() {
  const directory = await mkdtemp(join(tmpdir(), "event-api-"));
  const dataFile = join(directory, "events.json");
  const server = createEventServer({ dataFile, adminApiKey: "test-key" });
  server.listen(0, "127.0.0.1");
  await once(server, "listening");
  const { port } = server.address();
  return { server, dataFile, baseUrl: `http://127.0.0.1:${port}` };
}

test("admin sync persists events and public search returns them", async (context) => {
  const { server, dataFile, baseUrl } = await fixture();
  context.after(() => server.close());

  const unauthorised = await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ events: [example] }),
  });
  assert.equal(unauthorised.status, 401);

  const synced = await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", "X-Admin-Key": "test-key" },
    body: JSON.stringify({ events: [example] }),
  });
  assert.equal(synced.status, 200);
  assert.equal((await synced.json()).eventCount, 1);

  const searched = await fetch(`${baseUrl}/api/events?name=green&date=2026-11-18`);
  assert.equal(searched.status, 200);
  const result = await searched.json();
  assert.equal(result.events.length, 1);
  assert.equal(result.events[0].sessions[0].name, "Opening Keynote");
  assert.equal(result.events[0].sessions[0].startMinutes, 570);

  const persisted = JSON.parse(await readFile(dataFile, "utf8"));
  assert.equal(persisted.events[0].name, example.name);
});

test("invalid calendar dates and session minutes are rejected", async (context) => {
  const { server, baseUrl } = await fixture();
  context.after(() => server.close());
  const headers = { "Content-Type": "application/json", "X-Admin-Key": "test-key" };

  const invalidDate = await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ events: [{ ...example, date: "2026-02-31" }] }),
  });
  assert.equal(invalidDate.status, 400);

  const invalidTime = await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers,
    body: JSON.stringify({
      events: [{ ...example, sessions: [{ ...example.sessions[0], startMinutes: 1440 }] }],
    }),
  });
  assert.equal(invalidTime.status, 400);
});

test("a later full sync removes deleted events", async (context) => {
  const { server, baseUrl } = await fixture();
  context.after(() => server.close());
  const headers = { "Content-Type": "application/json", "X-Admin-Key": "test-key" };

  await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ events: [example] }),
  });
  await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ events: [] }),
  });

  const result = await (await fetch(`${baseUrl}/api/events`)).json();
  assert.deepEqual(result.events, []);
});

test("invalid event data is rejected without replacing stored data", async (context) => {
  const { server, baseUrl } = await fixture();
  context.after(() => server.close());
  const headers = { "Content-Type": "application/json", "X-Admin-Key": "test-key" };

  await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ events: [example] }),
  });
  const invalid = await fetch(`${baseUrl}/api/events`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ events: [{ id: 2, name: "Missing required fields" }] }),
  });
  assert.equal(invalid.status, 400);
  const result = await (await fetch(`${baseUrl}/api/events`)).json();
  assert.equal(result.events.length, 1);
});
