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

## 4. WebSocket Streaming Chat

### `WS /api/chat/ws`

前端先建立 WebSocket 长连接，再通过文本消息发起聊天。后端调用上游大模型（上游返回 SSE），并将 `start` / `delta` / `done` / `error` 事件转换为 WebSocket JSON 消息推送给前端。

#### Client → Server

连接建立后发送：

```json
{
  "action": "chat",
  "sessionId": 10,
  "modelId": 1,
  "message": "帮我总结今天的工作"
}
```

停止当前生成：

```json
{
  "action": "cancel"
}
```

#### Server → Client

每条消息统一封装为：

```json
{
  "event": "delta",
  "data": {
    "delta": "今天"
  }
}
```

事件类型与 SSE 模式保持一致：

1. `start`

```json
{
  "event": "start",
  "data": {
    "assistantMessageId": 22
  }
}
```

2. `delta` (multiple times)

```json
{
  "event": "delta",
  "data": {
    "delta": "今天"
  }
}
```

3. `done`

```json
{
  "event": "done",
  "data": {
    "assistantMessageId": 22,
    "content": "今天你完成了..."
  }
}
```

4. `error`

```json
{
  "event": "error",
  "data": {
    "message": "Upstream error 401: ..."
  }
}
```

#### Notes

- 同一个 WebSocket 连接同一时间只允许一个进行中的聊天请求。
- 聊天结束后连接保持打开，可继续发送下一条 `chat` 消息。
- `cancel` 会停止向客户端继续推送，但上游请求可能仍在后台执行。
