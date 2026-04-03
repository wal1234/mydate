<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Bell, MessageCircle, Bot, Settings, Plus } from 'lucide-vue-next'
import type { UserNotificationConfig, LlmApiConfig } from '../lib/types'
import { getNotificationConfig, saveNotificationConfig, getLlmConfigs, saveLlmConfig, activateLlmConfig, deleteLlmConfig } from '../lib/api'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const errorMsg = ref<string | null>(null)
const okMsg = ref<string | null>(null)

const config = ref<Partial<UserNotificationConfig>>({
  wechatServerChanKey: '',
  qqNumber: '',
  qmsgKey: '',
  wechatEnabled: false,
  qqEnabled: false
})

const llmConfigs = ref<LlmApiConfig[]>([])
const showLlmForm = ref(false)
const editingLlmConfig = ref<Partial<LlmApiConfig>>({
  configName: '',
  apiUrl: '',
  apiKey: '',
  modelName: 'gpt-4o-mini',
  providerType: 'openai',
  streamEnabled: 1,
  temperature: 0.7,
  maxTokens: 2048
})

async function loadConfig() {
  loading.value = true
  try {
    const data = await getNotificationConfig()
    if (data) {
      config.value = {
        ...data,
        wechatEnabled: Boolean(data.wechatEnabled),
        qqEnabled: Boolean(data.qqEnabled)
      }
    }
    llmConfigs.value = await getLlmConfigs()
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  errorMsg.value = null
  okMsg.value = null
  try {
    await saveNotificationConfig(config.value)
    okMsg.value = '保存成功'
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    saving.value = false
  }
}

async function saveLlmConfigHandler() {
  saving.value = true
  errorMsg.value = null
  try {
    await saveLlmConfig(editingLlmConfig.value)
    llmConfigs.value = await getLlmConfigs()
    showLlmForm.value = false
    editingLlmConfig.value = {
      configName: '',
      apiUrl: '',
      apiKey: '',
      modelName: 'gpt-4o-mini',
      providerType: 'openai',
      streamEnabled: 1,
      temperature: 0.7,
      maxTokens: 2048
    }
    okMsg.value = 'AI配置保存成功'
  } catch (e) {
    errorMsg.value = '保存AI配置失败：' + (e as Error).message
  } finally {
    saving.value = false
  }
}

async function activateLlmConfigHandler(configId: number) {
  try {
    await activateLlmConfig(configId)
    llmConfigs.value = await getLlmConfigs()
    okMsg.value = '配置已激活'
  } catch (e) {
    errorMsg.value = '激活配置失败：' + (e as Error).message
  }
}

async function deleteLlmConfigHandler(configId: number) {
  if (!confirm('确定要删除这个配置吗？')) return
  try {
    await deleteLlmConfig(configId)
    llmConfigs.value = await getLlmConfigs()
    okMsg.value = '配置已删除'
  } catch (e) {
    errorMsg.value = '删除配置失败：' + (e as Error).message
  }
}

onMounted(loadConfig)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center gap-3">
      <button
        class="p-2 rounded-lg hover:bg-zinc-100"
        @click="router.push('/')"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <div>
        <div class="text-lg font-semibold">消息推送设置</div>
        <div class="text-sm text-zinc-500">配置日程提醒推送渠道</div>
      </div>
    </div>

    <div v-if="errorMsg" class="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
      {{ errorMsg }}
    </div>
    <div v-if="okMsg" class="rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
      {{ okMsg }}
    </div>

    <div class="space-y-6">
      <section class="rounded-xl border border-zinc-200 p-4">
        <div class="flex items-center gap-2 mb-4">
          <MessageCircle class="h-5 w-5 text-green-600" />
          <h3 class="font-semibold">微信推送 (Server酱)</h3>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">Server酱 Key</label>
            <input
              v-model="config.wechatServerChanKey"
              type="text"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="请输入 Server酱 的 SCKEY"
            />
            <p class="mt-1 text-xs text-zinc-500">
              请先到 <a href="https://sct.ftqq.com/" target="_blank" class="text-indigo-600 hover:underline">sct.ftqq.com</a> 注册并获取 Key
            </p>
          </div>

          <div class="flex items-center gap-2">
            <input
              id="wechatEnabled"
              v-model="config.wechatEnabled"
              type="checkbox"
              class="w-4 h-4 rounded border-zinc-300 text-indigo-600"
            />
            <label for="wechatEnabled" class="text-sm text-zinc-700">启用微信推送</label>
          </div>
        </div>
      </section>

      <section class="rounded-xl border border-zinc-200 p-4">
        <div class="flex items-center gap-2 mb-4">
          <MessageCircle class="h-5 w-5 text-blue-600" />
          <h3 class="font-semibold">QQ推送 (Qmsg酱)</h3>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">Qmsg酱 Key</label>
            <input
              v-model="config.qmsgKey"
              type="text"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="请输入 Qmsg酱 的 KEY"
            />
            <p class="mt-1 text-xs text-zinc-500">
              请先到 <a href="https://qmsg.zendee.cn/" target="_blank" class="text-indigo-600 hover:underline">qmsg.zendee.cn</a> 注册并获取 Key
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">接收推送的QQ号</label>
            <input
              v-model="config.qqNumber"
              type="text"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="请输入您的 QQ 号"
            />
          </div>

          <div class="flex items-center gap-2">
            <input
              id="qqEnabled"
              v-model="config.qqEnabled"
              type="checkbox"
              class="w-4 h-4 rounded border-zinc-300 text-indigo-600"
            />
            <label for="qqEnabled" class="text-sm text-zinc-700">启用QQ推送</label>
          </div>
        </div>
      </section>

      <section class="rounded-xl border border-amber-200 bg-amber-50 p-4">
        <div class="flex items-center gap-2 mb-2">
          <Bell class="h-5 w-5 text-amber-600" />
          <h3 class="font-semibold text-amber-800">使用说明</h3>
        </div>
        <ul class="text-sm text-amber-700 space-y-1 list-disc list-inside">
          <li>Server酱可将消息推送到您的微信</li>
          <li>Qmsg酱可将消息推送到您的 QQ</li>
          <li>配置完成后，在日程设置中可以开启提醒</li>
          <li>开启提醒后，系统会在日程开始前发送通知</li>
        </ul>
      </section>

      <button
        class="w-full py-3 bg-indigo-600 text-white rounded-xl font-medium hover:bg-indigo-700 disabled:opacity-60"
        :disabled="saving"
        @click="handleSave"
      >
        {{ saving ? '保存中...' : '保存配置' }}
      </button>

      <section class="rounded-xl border border-indigo-200 bg-gradient-to-r from-indigo-50 to-purple-50 p-4">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <Bot class="h-5 w-5 text-indigo-600" />
            <h3 class="font-semibold text-indigo-800">AI Chat 模型配置</h3>
          </div>
          <button
            v-if="!showLlmForm"
            @click="showLlmForm = true"
            class="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 flex items-center gap-2"
          >
            <Plus class="h-4 w-4" />
            添加配置
          </button>
        </div>

        <div v-if="llmConfigs.length > 0" class="space-y-3 mb-4">
          <div
            v-for="cfg in llmConfigs"
            :key="cfg.id"
            class="p-3 bg-white rounded-lg border border-indigo-200 flex items-center justify-between"
          >
            <div>
              <div class="font-medium text-sm text-zinc-800">{{ cfg.configName }}</div>
              <div class="text-xs text-zinc-500 mt-1">
                {{ cfg.modelName }} · {{ cfg.providerType }}
              </div>
            </div>
            <div class="flex gap-2">
              <button
                v-if="cfg.isActive !== 1"
                @click="activateLlmConfigHandler(cfg.id!)"
                class="px-3 py-1 text-xs bg-indigo-100 text-indigo-700 rounded-lg hover:bg-indigo-200 transition-colors"
              >
                激活
              </button>
              <span v-else class="px-3 py-1 text-xs bg-green-100 text-green-700 rounded-lg">使用中</span>
              <button
                @click="deleteLlmConfigHandler(cfg.id!)"
                class="px-3 py-1 text-xs bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors"
              >
                删除
              </button>
            </div>
          </div>
        </div>

        <div v-if="showLlmForm" class="space-y-4 p-4 bg-white rounded-lg border border-indigo-200">
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">配置名称</label>
            <input
              v-model="editingLlmConfig.configName"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="例如：我的GPT-4"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">API 地址</label>
            <input
              v-model="editingLlmConfig.apiUrl"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="https://api.openai.com/v1"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">API Key</label>
            <input
              v-model="editingLlmConfig.apiKey"
              type="password"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="sk-..."
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">模型名称</label>
            <input
              v-model="editingLlmConfig.modelName"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
              placeholder="gpt-4o-mini"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">提供商类型</label>
            <select
              v-model="editingLlmConfig.providerType"
              class="w-full px-3 py-2 border border-zinc-300 rounded-lg text-sm"
            >
              <option value="openai">OpenAI 兼容</option>
              <option value="claude">Claude</option>
              <option value="gemini">Gemini</option>
              <option value="custom">自定义</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">
              温度 (Temperature): {{ editingLlmConfig.temperature || 0.7 }}
            </label>
            <input
              v-model.number="editingLlmConfig.temperature"
              type="range"
              min="0"
              max="2"
              step="0.1"
              class="w-full"
            />
            <p class="text-xs text-zinc-500 mt-1">较低的值更确定性，较高的值更有创造性</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">
              最大回复长度: {{ editingLlmConfig.maxTokens || 2048 }}
            </label>
            <input
              v-model.number="editingLlmConfig.maxTokens"
              type="range"
              min="256"
              max="8192"
              step="256"
              class="w-full"
            />
          </div>

          <div class="flex gap-2">
            <button
              @click="saveLlmConfigHandler"
              :disabled="saving || !editingLlmConfig.configName || !editingLlmConfig.apiUrl || !editingLlmConfig.apiKey || !editingLlmConfig.modelName"
              class="flex-1 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ saving ? '保存中...' : '保存' }}
            </button>
            <button
              @click="showLlmForm = false"
              class="px-4 py-2 bg-zinc-200 text-zinc-700 rounded-lg text-sm hover:bg-zinc-300"
            >
              取消
            </button>
          </div>
        </div>

        <div v-if="!showLlmForm && llmConfigs.length === 0" class="text-center py-6">
          <Bot class="h-12 w-12 mx-auto text-indigo-300 mb-2" />
          <p class="text-sm text-indigo-600">还没有配置AI模型</p>
          <p class="text-xs text-zinc-500 mt-1">点击上方按钮添加您的第一个AI配置</p>
        </div>

        <div class="mt-4 p-3 bg-indigo-50 rounded-lg border border-indigo-100">
          <div class="flex items-center gap-2 mb-2">
            <Settings class="h-4 w-4 text-indigo-600" />
            <span class="text-sm font-medium text-indigo-800">使用说明</span>
          </div>
          <ul class="text-xs text-indigo-700 space-y-1 list-disc list-inside">
            <li>配置您的大模型API以启用AI对话功能</li>
            <li>支持OpenAI兼容的API格式</li>
            <li>激活配置后，可以在AI助手页面进行对话</li>
            <li>AI能够读取您的日程信息，提供智能回答</li>
          </ul>
        </div>
      </section>
    </div>
  </div>
</template>
