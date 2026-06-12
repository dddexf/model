const JSON_HEADERS = {
  "Content-Type": "application/json"
};

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed: ${response.status}`);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export function listModels() {
  return requestJson("/api/models");
}

export function createSession(payload = {}) {
  return requestJson("/api/sessions", {
    method: "POST",
    headers: JSON_HEADERS,
    body: JSON.stringify(payload)
  });
}

export function listSessions() {
  return requestJson("/api/sessions");
}

export function renameSession(sessionId, title) {
  return requestJson(`/api/sessions/${sessionId}/title`, {
    method: "PATCH",
    headers: JSON_HEADERS,
    body: JSON.stringify({ title })
  });
}

export function deleteSession(sessionId) {
  return requestJson(`/api/sessions/${sessionId}`, {
    method: "DELETE"
  });
}

export function listMessages(sessionId) {
  return requestJson(`/api/sessions/${sessionId}/messages`);
}

let sharedChatSocket = null;
let sharedChatSocketReady = null;

function buildWebSocketUrl() {
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}/api/chat/ws`;
}

export function connectChatWebSocket() {
  if (sharedChatSocket?.readyState === WebSocket.OPEN) {
    return Promise.resolve(sharedChatSocket);
  }
  if (sharedChatSocket?.readyState === WebSocket.CONNECTING && sharedChatSocketReady) {
    return sharedChatSocketReady;
  }

  sharedChatSocketReady = new Promise((resolve, reject) => {
    const ws = new WebSocket(buildWebSocketUrl());
    sharedChatSocket = ws;

    ws.addEventListener("open", () => resolve(ws), { once: true });
    ws.addEventListener("error", () => {
      sharedChatSocket = null;
      sharedChatSocketReady = null;
      reject(new Error("WebSocket connection failed"));
    }, { once: true });
    ws.addEventListener("close", () => {
      sharedChatSocket = null;
      sharedChatSocketReady = null;
    });
  });

  return sharedChatSocketReady;
}

export function disconnectChatWebSocket() {
  if (sharedChatSocket) {
    sharedChatSocket.close();
    sharedChatSocket = null;
    sharedChatSocketReady = null;
  }
}

export async function streamChatWebSocket(payload, { onEvent, onClose, signal }) {
  const ws = await connectChatWebSocket();

  return new Promise((resolve, reject) => {
    let settled = false;

    const cleanup = () => {
      ws.removeEventListener("message", onMessage);
      signal?.removeEventListener("abort", onAbort);
      if (onClose) {
        onClose();
      }
    };

    const finish = (error) => {
      if (settled) {
        return;
      }
      settled = true;
      cleanup();
      if (error) {
        reject(error);
      } else {
        resolve();
      }
    };

    const onMessage = (event) => {
      let envelope;
      try {
        envelope = JSON.parse(event.data);
      } catch {
        return;
      }

      const eventName = envelope.event ?? "message";
      const data = envelope.data ?? envelope;
      if (onEvent) {
        onEvent(eventName, data);
      }

      if (eventName === "done" || eventName === "error") {
        finish();
      }
    };

    const onAbort = () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ action: "cancel" }));
      }
      finish(new DOMException("Aborted", "AbortError"));
    };

    ws.addEventListener("message", onMessage);
    signal?.addEventListener("abort", onAbort);

    if (ws.readyState !== WebSocket.OPEN) {
      finish(new Error("WebSocket is not open"));
      return;
    }

    ws.send(JSON.stringify({
      action: "chat",
      sessionId: payload.sessionId,
      modelId: payload.modelId,
      message: payload.message
    }));
  });
}

export async function streamChat(payload, { onEvent, onClose, signal }) {
  const response = await fetch("/api/chat/stream", {
    method: "POST",
    headers: {
      Accept: "text/event-stream",
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload),
    signal
  });

  if (!response.ok || !response.body) {
    const text = await response.text();
    throw new Error(text || `Stream request failed: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const blocks = buffer.split(/\r?\n\r?\n/);
    buffer = blocks.pop() ?? "";

    for (const block of blocks) {
      parseSseBlock(block, onEvent);
    }
  }

  if (buffer.trim()) {
    parseSseBlock(buffer, onEvent);
  }
  if (onClose) {
    onClose();
  }
}

function parseSseBlock(block, onEvent) {
  let eventName = "message";
  const dataLines = [];

  for (const rawLine of block.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line) {
      continue;
    }
    if (line.startsWith("event:")) {
      eventName = line.slice(6).trim();
      continue;
    }
    if (line.startsWith("data:")) {
      dataLines.push(line.slice(5).trim());
    }
  }

  if (dataLines.length === 0 || !onEvent) {
    return;
  }

  const text = dataLines.join("\n");
  let payload = text;
  try {
    payload = JSON.parse(text);
  } catch {
    // Keep plain text payload if it is not a JSON event.
  }
  onEvent(eventName, payload);
}
