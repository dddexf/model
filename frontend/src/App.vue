<template>
  <div class="app-shell">
    <aside class="session-pane">
      <div class="brand-block">
        <p class="brand-caption">Model Workspace</p>
        <h1>多模型对话台</h1>
      </div>

      <el-button class="new-session-btn" type="primary" @click="handleCreateSession">
        <el-icon><Plus /></el-icon>
        新建会话
      </el-button>

      <div class="session-list">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: session.id === activeSessionId }"
          @click="handleSelectSession(session.id)"
        >
          <div class="session-main">
            <span class="session-title">{{ session.title }}</span>
            <span class="session-time">{{ formatTime(session.updatedAt) }}</span>
          </div>
          <div class="session-actions">
            <el-button text size="small" @click.stop="handleRenameSession(session)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text size="small" @click.stop="handleDeleteSession(session)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </button>
      </div>
    </aside>

    <main class="chat-pane">
      <header class="chat-header">
        <div>
          <h2>{{ activeSessionTitle }}</h2>
          <p>{{ transportModeLabel }} · 会话自动保存</p>
        </div>
        <div class="header-controls">
          <el-select
            v-model="transportMode"
            class="transport-select"
            :disabled="isStreaming"
            @change="handleTransportModeChange"
          >
            <el-option label="SSE 流式" value="sse" />
            <el-option label="WebSocket 流式" value="websocket" />
          </el-select>
          <el-select
            v-model="selectedModelId"
            placeholder="选择模型"
            class="model-select"
            :disabled="models.length === 0"
          >
            <el-option
              v-for="model in models"
              :key="model.id"
              :label="`${model.displayName} (${model.providerName})`"
              :value="model.id"
            />
          </el-select>
        </div>
      </header>

      <section ref="messageContainerRef" class="message-stream">
        <div v-if="messages.length === 0" class="empty-hint">
          <p>从输入框开始提问，系统会按你选择的模型进行流式回答。</p>
        </div>

        <div
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="message.role === 'USER' ? 'is-user' : 'is-assistant'"
        >
          <div class="message-meta">
            <span>{{ message.role === "USER" ? "你" : "助手" }}</span>
            <span v-if="message.status === 'FAILED'" class="status-failed">失败</span>
          </div>
          <article class="message-content">
            <div
              v-if="message.role === 'ASSISTANT'"
              class="markdown-body"
              v-html="renderMarkdown(message.content)"
            />
            <p v-else class="user-text">{{ message.content }}</p>
          </article>
        </div>
      </section>

      <footer class="chat-input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="4"
          resize="none"
          placeholder="请输入内容，Enter 发送，Shift+Enter 换行"
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="input-actions">
          <el-button v-if="isStreaming" @click="stopStream">
            <el-icon><VideoPause /></el-icon>
            停止生成
          </el-button>
          <el-button type="primary" :loading="isStreaming" @click="handleSend">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from "vue";
import { Delete, Edit, Plus, Promotion, VideoPause } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import DOMPurify from "dompurify";
import { marked } from "marked";
import hljs from "highlight.js";
import {
  connectChatWebSocket,
  createSession,
  deleteSession,
  disconnectChatWebSocket,
  listMessages,
  listModels,
  listSessions,
  renameSession,
  streamChat,
  streamChatWebSocket
} from "./api/http";

marked.setOptions({ breaks: true, gfm: true });

const sessions = ref([]);
const models = ref([]);
const messages = ref([]);
const activeSessionId = ref(null);
const selectedModelId = ref(null);
const inputMessage = ref("");
const transportMode = ref("sse");
const isStreaming = ref(false);
const streamAbortController = ref(null);
const messageContainerRef = ref(null);

const transportModeLabel = computed(() =>
  transportMode.value === "websocket" ? "WebSocket 流式输出" : "SSE 流式输出"
);

const activeSessionTitle = computed(() => {
  const target = sessions.value.find((it) => it.id === activeSessionId.value);
  return target ? target.title : "新会话";
});

onMounted(async () => {
  try {
    await initialize();
  } catch (error) {
    ElMessage.error(error.message || "初始化失败");
  }
});

watch(
  () => messages.value.map((item) => `${item.id}-${item.content}`).join("|"),
  async () => {
    await nextTick();
    scrollToBottom();
    highlightBlocks();
  }
);

function renderMarkdown(content) {
  const html = marked.parse(content || "");
  return DOMPurify.sanitize(String(html));
}

async function initialize() {
  models.value = await listModels();
  if (models.value.length > 0) {
    selectedModelId.value = models.value[0].id;
  }
  await loadSessionsAndMessages();
  if (transportMode.value === "websocket") {
    await connectChatWebSocket();
  }
}

async function handleTransportModeChange(mode) {
  if (isStreaming.value) {
    return;
  }
  if (mode === "websocket") {
    try {
      await connectChatWebSocket();
    } catch (error) {
      transportMode.value = "sse";
      ElMessage.error(error.message || "WebSocket 连接失败");
    }
    return;
  }
  disconnectChatWebSocket();
}

async function loadSessionsAndMessages() {
  let list = await listSessions();
  if (list.length === 0) {
    await createSession({ title: "新会话" });
    list = await listSessions();
  }
  sessions.value = list;
  if (!activeSessionId.value || !list.some((it) => it.id === activeSessionId.value)) {
    activeSessionId.value = list[0]?.id ?? null;
  }
  if (activeSessionId.value) {
    messages.value = await listMessages(activeSessionId.value);
  }
}

async function handleCreateSession() {
  const created = await createSession({ title: "新会话" });
  await refreshSessionsOnly();
  activeSessionId.value = created.id;
  messages.value = [];
}

async function refreshSessionsOnly() {
  sessions.value = await listSessions();
}

async function handleSelectSession(sessionId) {
  if (isStreaming.value) {
    ElMessage.warning("请先停止当前生成");
    return;
  }
  activeSessionId.value = sessionId;
  messages.value = await listMessages(sessionId);
}

async function handleRenameSession(session) {
  try {
    const { value } = await ElMessageBox.prompt("请输入新的会话名", "重命名会话", {
      inputValue: session.title,
      inputPattern: /^.{1,120}$/,
      inputErrorMessage: "长度需在 1-120 之间"
    });
    await renameSession(session.id, value.trim());
    await refreshSessionsOnly();
  } catch {
    // ignore user cancel
  }
}

async function handleDeleteSession(session) {
  try {
    await ElMessageBox.confirm("删除后不可恢复，是否继续？", "删除会话", {
      confirmButtonText: "删除",
      cancelButtonText: "取消",
      type: "warning"
    });

    await deleteSession(session.id);
    await refreshSessionsOnly();
    if (session.id === activeSessionId.value) {
      if (sessions.value.length === 0) {
        await handleCreateSession();
      } else {
        activeSessionId.value = sessions.value[0].id;
        messages.value = await listMessages(activeSessionId.value);
      }
    }
  } catch {
    // ignore user cancel
  }
}

async function handleSend() {
  if (isStreaming.value) {
    return;
  }

  const text = inputMessage.value.trim();
  if (!text) {
    return;
  }
  if (!selectedModelId.value) {
    ElMessage.warning("请先选择模型");
    return;
  }
  if (!activeSessionId.value) {
    await handleCreateSession();
  }

  const sessionId = activeSessionId.value;
  const userTmp = reactive({
    id: `u-${Date.now()}`,
    role: "USER",
    content: text,
    status: "DONE"
  });
  const assistantTmp = reactive({
    id: `a-${Date.now()}`,
    role: "ASSISTANT",
    content: "",
    status: "STREAMING"
  });

  messages.value.push(userTmp, assistantTmp);
  inputMessage.value = "";

  isStreaming.value = true;
  streamAbortController.value = new AbortController();

  const streamPayload = {
    sessionId,
    modelId: selectedModelId.value,
    message: text
  };
  const streamOptions = {
    signal: streamAbortController.value.signal,
    onEvent: (event, payload) => {
      if (event === "start") {
        assistantTmp.id = payload.assistantMessageId ?? assistantTmp.id;
        return;
      }
      if (event === "delta") {
        assistantTmp.content += payload.delta ?? "";
        return;
      }
      if (event === "done") {
        assistantTmp.content = payload.content ?? assistantTmp.content;
        assistantTmp.status = "DONE";
        return;
      }
      if (event === "error") {
        assistantTmp.status = "FAILED";
        if (!assistantTmp.content) {
          assistantTmp.content = "生成失败，请重试。";
        }
        ElMessage.error(payload.message || "生成失败");
      }
    }
  };
  const streamFn = transportMode.value === "websocket" ? streamChatWebSocket : streamChat;

  try {
    await streamFn(streamPayload, streamOptions);
  } catch (error) {
    if (streamAbortController.value?.signal.aborted) {
      assistantTmp.status = "FAILED";
      ElMessage.info("已停止生成");
    } else {
      assistantTmp.status = "FAILED";
      ElMessage.error(error.message || "请求失败");
    }
  } finally {
    isStreaming.value = false;
    streamAbortController.value = null;
    await refreshSessionsOnly();
    messages.value = await listMessages(sessionId);
  }
}

function stopStream() {
  if (streamAbortController.value) {
    streamAbortController.value.abort();
  }
}

function formatTime(time) {
  if (!time) {
    return "";
  }
  return new Date(time).toLocaleTimeString("zh-CN", {
    hour: "2-digit",
    minute: "2-digit"
  });
}

function scrollToBottom() {
  if (messageContainerRef.value) {
    messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight;
  }
}

function highlightBlocks() {
  document.querySelectorAll(".markdown-body pre code").forEach((block) => {
    hljs.highlightElement(block);
  });
}
</script>
