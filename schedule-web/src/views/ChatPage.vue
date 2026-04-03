<template>
  <div class="flex h-full">
    <aside class="w-80 bg-white/50 backdrop-blur-sm border-r border-zinc-200 flex flex-col">
      <div class="p-4 border-b border-zinc-200">
        <button
          @click="createNewSession"
          class="w-full btn-primary flex items-center justify-center gap-2"
        >
          <Plus class="h-5 w-5" />
          新建对话
        </button>
      </div>

      <div class="flex-1 overflow-y-auto">
        <div class="p-2">
          <div
            v-for="session in sessions"
            :key="session.id"
            @click="selectSession(session.id)"
            class="group p-3 rounded-xl cursor-pointer mb-1 transition-all duration-200 flex items-center justify-between"
            :class="currentSessionId === session.id ? 'bg-indigo-50 border border-indigo-200' : 'hover:bg-zinc-50'"
          >
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-zinc-800 truncate">
                {{ session.sessionTitle }}
              </div>
              <div class="text-xs text-zinc-500 mt-1">
                {{ formatTime(session.updatedAt || session.createdAt) }}
              </div>
            </div>
            <button
              @click.stop="handleDeleteSession(session.id)"
              class="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-100 rounded transition-opacity"
            >
              <Trash2 class="h-4 w-4 text-red-500" />
            </button>
          </div>
        </div>
      </div>
    </aside>

    <main class="flex-1 flex flex-col bg-gradient-to-br from-indigo-50 via-white to-purple-50">
      <div v-if="!currentSessionId" class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <div class="w-20 h-20 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <Bot class="h-10 w-10 text-white" />
          </div>
          <h2 class="text-2xl font-bold text-zinc-800 mb-2">AI 智能助手</h2>
          <p class="text-zinc-500 mb-6">选择左侧会话或创建新对话开始聊天</p>
          <button @click="createNewSession" class="btn-primary inline-flex items-center gap-2">
            <Plus class="h-5 w-5" />
            开始新对话
          </button>
        </div>
      </div>

      <div v-else class="flex-1 flex flex-col">
        <div ref="messagesContainer" class="flex-1 overflow-y-auto p-6 space-y-6">
          <div v-if="messages.length === 0" class="text-center py-12">
            <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-indigo-100 flex items-center justify-center">
              <MessageCircle class="h-8 w-8 text-indigo-600" />
            </div>
            <p class="text-zinc-500">开始和AI对话吧！我可以帮你总结日程、回答问题。</p>
          </div>

          <div
            v-for="msg in messages"
            :key="msg.id"
            class="flex gap-4 animate-fade-in"
            :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
          >
            <div
              class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
              :class="msg.role === 'user' ? 'bg-gradient-to-br from-indigo-500 to-purple-600' : 'bg-gradient-to-br from-emerald-500 to-teal-600'"
            >
              <User v-if="msg.role === 'user'" class="h-5 w-5 text-white" />
              <Bot v-else class="h-5 w-5 text-white" />
            </div>

            <div
              class="max-w-2xl px-5 py-3 rounded-2xl"
              :class="msg.role === 'user' ? 'bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-tr-sm' : 'bg-white shadow-sm border border-zinc-100 text-zinc-800 rounded-tl-sm'"
            >
              <p class="whitespace-pre-wrap leading-relaxed">{{ msg.content }}</p>
            </div>
          </div>

          <div v-if="isLoading" class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center flex-shrink-0">
              <Bot class="h-5 w-5 text-white" />
            </div>
            <div class="bg-white shadow-sm border border-zinc-100 px-5 py-3 rounded-2xl rounded-tl-sm">
              <div class="flex gap-1">
                <div class="w-2 h-2 bg-indigo-500 rounded-full animate-bounce"></div>
                <div class="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                <div class="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="p-4 border-t border-zinc-200 bg-white/50 backdrop-blur-sm">
          <div class="flex gap-3 max-w-4xl mx-auto">
            <input
              v-model="inputMessage"
              @keyup.enter="sendMessage"
              type="text"
              placeholder="输入消息..."
              class="form-input flex-1"
              :disabled="isLoading"
            />
            <button
              @click="sendMessage"
              :disabled="isLoading || !inputMessage.trim()"
              class="btn-primary px-6 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Send class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showSettings" class="modal-overlay" @click.self="showSettings = false">
      <div class="modal-card max-w-2xl">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
              <Settings class="h-5 w-5 text-white" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-zinc-800">API 配置</h3>
              <p class="text-xs text-zinc-500">配置您的大模型API</p>
            </div>
          </div>
          <button @click="showSettings = false" class="p-2 hover:bg-zinc-100 rounded-lg transition-colors">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>

        <div class="space-y-4 max-h-[60vh] overflow-y-auto">
          <div v-if="llmConfigs.length > 0" class="mb-4">
            <label class="form-label">已保存的配置</label>
            <div class="space-y-2">
              <div
                v-for="config in llmConfigs"
                :key="config.id"
                class="p-3 bg-zinc-50 rounded-xl border border-zinc-200 flex items-center justify-between"
              >
                <div>
                  <div class="font-medium text-sm">{{ config.configName }}</div>
                  <div class="text-xs text-zinc-500 mt-1">
                    {{ config.modelName }} · {{ config.providerType }}
                  </div>
                </div>
                <div class="flex gap-2">
                  <button
                    v-if="config.isActive !== 1"
                    @click="activateConfig(config.id!)"
                    class="px-3 py-1 text-xs bg-indigo-100 text-indigo-700 rounded-lg hover:bg-indigo-200 transition-colors"
                  >
                    激活
                  </button>
                  <span v-else class="px-3 py-1 text-xs bg-green-100 text-green-700 rounded-lg">使用中</span>
                  <button
                    @click="deleteConfig(config.id!)"
                    class="px-3 py-1 text-xs bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div>
            <label class="form-label">配置名称</label>
            <input v-model="newConfig.configName" class="form-input" placeholder="例如：我的GPT-4" />
          </div>

          <div>
            <label class="form-label">API 地址</label>
            <input v-model="newConfig.apiUrl" class="form-input" placeholder="https://api.openai.com/v1" />
          </div>

          <div>
            <label class="form-label">API Key</label>
            <input v-model="newConfig.apiKey" type="password" class="form-input" placeholder="sk-..." />
          </div>

          <div>
            <label class="form-label">模型名称</label>
            <input v-model="newConfig.modelName" class="form-input" placeholder="gpt-4o-mini" />
          </div>

          <div>
            <label class="form-label">提供商类型</label>
            <select v-model="newConfig.providerType" class="form-input">
              <option value="openai">OpenAI 兼容</option>
              <option value="claude">Claude</option>
              <option value="gemini">Gemini</option>
              <option value="custom">自定义</option>
            </select>
          </div>

          <div>
            <label class="form-label">温度 (Temperature): {{ newConfig.temperature || 0.7 }}</label>
            <input
              v-model.number="newConfig.temperature"
              type="range"
              min="0"
              max="2"
              step="0.1"
              class="w-full"
            />
            <div class="text-xs text-zinc-500 mt-1">较低的值更确定性，较高的值更有创造性</div>
          </div>

          <div>
            <label class="form-label">最大回复长度: {{ newConfig.maxTokens || 2048 }}</label>
            <input
              v-model.number="newConfig.maxTokens"
              type="range"
              min="256"
              max="8192"
              step="256"
              class="w-full"
            />
          </div>

          <div class="flex items-center gap-2">
            <input v-model="newConfig.streamEnabled" type="checkbox" id="streamEnabled" class="w-4 h-4" />
            <label for="streamEnabled" class="text-sm">启用流式输出</label>
          </div>

          <button
            @click="saveConfig"
            class="w-full btn-primary"
            :disabled="!newConfig.configName || !newConfig.apiUrl || !newConfig.apiKey || !newConfig.modelName"
          >
            保存配置
          </button>
        </div>
      </div>
    </div>

    <button
      @click="showSettings = true"
      class="fixed bottom-6 right-6 w-14 h-14 rounded-full bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-lg hover:shadow-xl transition-all flex items-center justify-center z-40"
    >
      <Settings class="h-6 w-6" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { Plus, Trash2, Bot, User, Send, Settings, X, MessageCircle } from 'lucide-vue-next'
import { getChatSessions, createChatSession, deleteChatSession, getChatMessages, sendChatMessage, getLlmConfigs, saveLlmConfig, activateLlmConfig, deleteLlmConfig, type ChatSession, type ChatMessage, type LlmApiConfig } from '../lib/api'

const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<number | null>(null)
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const showSettings = ref(false)
const llmConfigs = ref<LlmApiConfig[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

const newConfig = ref<Partial<LlmApiConfig>>({
  configName: '',
  apiUrl: '',
  apiKey: '',
  modelName: 'gpt-4o-mini',
  providerType: 'openai',
  streamEnabled: 1,
  temperature: 0.7,
  maxTokens: 2048
})

onMounted(async () => {
  await loadSessions()
  await loadConfigs()
})

async function loadSessions() {
  try {
    sessions.value = await getChatSessions()
  } catch (error) {
    console.error('加载会话失败:', error)
  }
}

async function loadConfigs() {
  try {
    llmConfigs.value = await getLlmConfigs()
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

async function createNewSession() {
  try {
    const session = await createChatSession(undefined, '新对话')
    sessions.value.unshift(session)
    selectSession(session.id)
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

async function selectSession(sessionId: number) {
  currentSessionId.value = sessionId
  try {
    messages.value = await getChatMessages(sessionId)
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

async function handleDeleteSession(sessionId: number) {
  if (!confirm('确定要删除这个会话吗？')) return
  try {
    await deleteChatSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

async function sendMessage() {
  if (!inputMessage.value.trim() || !currentSessionId.value || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  isLoading.value = true

  try {
    await sendChatMessage(currentSessionId.value, userMessage)
    messages.value = await getChatMessages(currentSessionId.value)
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    alert('发送消息失败：' + (error as Error).message)
  } finally {
    isLoading.value = false
  }
}

async function saveConfig() {
  try {
    await saveLlmConfig(newConfig.value)
    await loadConfigs()
    newConfig.value = {
      configName: '',
      apiUrl: '',
      apiKey: '',
      modelName: 'gpt-4o-mini',
      providerType: 'openai',
      streamEnabled: 1,
      temperature: 0.7,
      maxTokens: 2048
    }
    alert('配置保存成功！')
  } catch (error) {
    console.error('保存配置失败:', error)
    alert('保存配置失败：' + (error as Error).message)
  }
}

async function activateConfig(configId: number) {
  try {
    await activateLlmConfig(configId)
    await loadConfigs()
    alert('配置已激活！')
  } catch (error) {
    console.error('激活配置失败:', error)
    alert('激活配置失败：' + (error as Error).message)
  }
}

async function deleteConfig(configId: number) {
  if (!confirm('确定要删除这个配置吗？')) return
  try {
    await deleteLlmConfig(configId)
    await loadConfigs()
  } catch (error) {
    console.error('删除配置失败:', error)
    alert('删除配置失败：' + (error as Error).message)
  }
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function formatTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return date.toLocaleDateString('zh-CN', { weekday: 'short' })
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
