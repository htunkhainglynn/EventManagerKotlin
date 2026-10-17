import { mkdir, readFile, rename, writeFile } from "node:fs/promises";
import { dirname } from "node:path";

const sessionTypes = new Set([
  "Workshop",
  "Presentation",
  "Panel Discussion",
  "Networking",
  "Keynote",
  "Others",
]);

function text(value) {
  return typeof value === "string" ? value.trim() : "";
}

function positiveId(value, label) {
  const number = Number(value);
  if (!Number.isSafeInteger(number) || number <= 0) {
    throw new ValidationError(`${label} must be a positive integer`);
  }
  return number;
}

function timestamp(value) {
  const number = Number(value);
  return Number.isFinite(number) && number > 0 ? Math.trunc(number) : Date.now();
}

export class ValidationError extends Error {}

export function normalizeEvents(input) {
  if (!Array.isArray(input)) throw new ValidationError("events must be an array");

  const eventKeys = new Set();
  return input.map((rawEvent, eventIndex) => {
    if (!rawEvent || typeof rawEvent !== "object") {
      throw new ValidationError(`events[${eventIndex}] must be an object`);
    }

    const event = {
      id: positiveId(rawEvent.id, `events[${eventIndex}].id`),
      name: text(rawEvent.name),
      date: text(rawEvent.date),
      organiser: text(rawEvent.organiser),
      location: text(rawEvent.location),
      description: text(rawEvent.description),
      contactEmail: text(rawEvent.contactEmail),
      contactPhone: text(rawEvent.contactPhone),
      updatedAt: timestamp(rawEvent.updatedAt),
      sessions: [],
    };

    if (!event.name || !event.date || !event.organiser || !event.location) {
      throw new ValidationError(
        `events[${eventIndex}] requires name, date, organiser and location`,
      );
    }

    const duplicateKey = `${event.name.toLocaleLowerCase()}\u0000${event.date}`;
    if (eventKeys.has(duplicateKey)) {
      throw new ValidationError(`duplicate event name and date at events[${eventIndex}]`);
    }
    eventKeys.add(duplicateKey);

    const sessions = rawEvent.sessions ?? [];
    if (!Array.isArray(sessions)) {
      throw new ValidationError(`events[${eventIndex}].sessions must be an array`);
    }
    const sessionIds = new Set();
    event.sessions = sessions.map((rawSession, sessionIndex) => {
      if (!rawSession || typeof rawSession !== "object") {
        throw new ValidationError(
          `events[${eventIndex}].sessions[${sessionIndex}] must be an object`,
        );
      }
      const session = {
        id: positiveId(
          rawSession.id,
          `events[${eventIndex}].sessions[${sessionIndex}].id`,
        ),
        name: text(rawSession.name),
        time: text(rawSession.time),
        speaker: text(rawSession.speaker),
        room: text(rawSession.room),
        type: text(rawSession.type),
        description: text(rawSession.description),
        updatedAt: timestamp(rawSession.updatedAt),
      };
      if (!session.name || !session.time) {
        throw new ValidationError(
          `events[${eventIndex}].sessions[${sessionIndex}] requires name and time`,
        );
      }
      if (!sessionTypes.has(session.type)) {
        throw new ValidationError(
          `events[${eventIndex}].sessions[${sessionIndex}] has an unsupported type`,
        );
      }
      if (sessionIds.has(session.id)) {
        throw new ValidationError(
          `events[${eventIndex}] contains duplicate session id ${session.id}`,
        );
      }
      sessionIds.add(session.id);
      return session;
    });
    event.sessions.sort((a, b) => a.time.localeCompare(b.time) || a.name.localeCompare(b.name));
    return event;
  });
}

export class FileEventStore {
  constructor(filePath) {
    this.filePath = filePath;
    this.writeChain = Promise.resolve();
  }

  async read() {
    try {
      const parsed = JSON.parse(await readFile(this.filePath, "utf8"));
      return {
        events: normalizeEvents(parsed.events ?? []),
        updatedAt: timestamp(parsed.updatedAt),
      };
    } catch (error) {
      if (error?.code === "ENOENT") return { events: [], updatedAt: null };
      throw error;
    }
  }

  async replace(events) {
    const normalized = normalizeEvents(events);
    const snapshot = { events: normalized, updatedAt: Date.now() };
    this.writeChain = this.writeChain.then(async () => {
      await mkdir(dirname(this.filePath), { recursive: true });
      const temporary = `${this.filePath}.${process.pid}.tmp`;
      await writeFile(temporary, `${JSON.stringify(snapshot, null, 2)}\n`, "utf8");
      await rename(temporary, this.filePath);
    });
    await this.writeChain;
    return snapshot;
  }
}
