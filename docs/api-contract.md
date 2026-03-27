# Chat API Contract (MVP)

## 1. Models

### `GET /api/models`

- Response `200`

```json
[
  {
    "id": 1,
    "displayName": "DeepSeek Chat",
    "modelCode": "deepseek-chat",
    "providerName": "DeepSeek"
  }
]
```

## 2. Sessions

### `POST /api/sessions`

- Request

```json
{
  "title": "工作助手"
}
```

- Response `201`

```json
{
  "id": 10,
  "title": "工作助手",
  "updatedAt": "2026-03-27T16:00:00"
}
```

### `GET /api/sessions`

- Response `200`

```json
[
  {
    "id": 10,
    "title": "工作助手",
    "updatedAt": "2026-03-27T16:00:00"
  }
]
```

### `PATCH /api/sessions/{id}/title`

- Request

```json
{
  "title": "重命名后的会话"
}
```

- Response `200`: same as `SessionDto`.

### `DELETE /api/sessions/{id}`

- Response `204`.

### `GET /api/sessions/{id}/messages`

- Response `200`

```json
[
  {
    "id": 1,
    "role": "USER",
    "content": "你好",
    "status": "DONE",
    "seqNo": 1,
    "createdAt": "2026-03-27T16:00:00"
  }
]
```

## 3. Streaming Chat

### `POST /api/chat/stream` (`text/event-stream`)

- Request

```json
{
  "sessionId": 10,
  "modelId": 1,
  "message": "帮我总结今天的工作"
}
```

- SSE Events
1. `start`

```json
{
  "assistantMessageId": 22
}
```

2. `delta` (multiple times)

```json
{
  "delta": "今天"
}
```

3. `done`

```json
{
  "assistantMessageId": 22,
  "content": "今天你完成了..."
}
```

4. `error` (if upstream fails)

```json
{
  "message": "Upstream error 401: ..."
}
```
