<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 p-6">
    <div class="max-w-6xl mx-auto">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-zinc-800 mb-2">提示词模板管理</h1>
        <p class="text-zinc-500">管理和优化您的AI提示词，支持版本控制和变量模板</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-sm border border-zinc-200 p-6">
            <h2 class="text-lg font-bold text-zinc-800 mb-4">模板列表</h2>
            <button @click="showCreateModal = true" class="w-full btn-primary mb-4 flex items-center justify-center gap-2">
              <Plus class="h-5 w-5" />
              创建模板
            </button>

            <div class="space-y-2">
              <div
                v-for="template in templates"
                :key="template.id"
                @click="selectTemplate(template)"
                class="p-3 rounded-xl cursor-pointer transition-all duration-200"
                :class="selectedTemplate?.id === template.id ? 'bg-indigo-50 border border-indigo-200' : 'hover:bg-zinc-50 border border-transparent'"
              >
                <div class="font-medium text-sm text-zinc-800">{{ template.templateName }}</div>
                <div class="text-xs text-zinc-500 mt-1">{{ template.description || '暂无描述' }}</div>
              </div>
              <div v-if="templates.length === 0" class="text-center py-8 text-zinc-500">
                <FileText class="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p class="text-sm">暂无模板</p>
              </div>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div v-if="selectedTemplate" class="bg-white rounded-2xl shadow-sm border border-zinc-200 p-6">
            <div class="flex items-center justify-between mb-6">
              <div>
                <h2 class="text-xl font-bold text-zinc-800">{{ selectedTemplate.templateName }}</h2>
                <p class="text-sm text-zinc-500 mt-1">{{ selectedTemplate.description || '暂无描述' }}</p>
              </div>
              <div class="flex gap-2">
                <button @click="showEditModal = true" class="btn-secondary">
                  <Edit class="h-4 w-4" />
                  编辑
                </button>
                <button @click="deleteTemplate" class="px-4 py-2 text-sm bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors">
                  <Trash2 class="h-4 w-4" />
                </button>
              </div>
            </div>

            <div class="mb-6">
              <h3 class="text-sm font-semibold text-zinc-700 mb-2">当前模板内容</h3>
              <div class="bg-zinc-50 rounded-xl p-4 border border-zinc-200">
                <pre class="whitespace-pre-wrap text-sm text-zinc-700 font-mono">{{ selectedTemplate.templateContent }}</pre>
              </div>
            </div>

            <div class="mb-6">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-sm font-semibold text-zinc-700">版本历史</h3>
                <button @click="showVersionModal = true" class="text-sm text-indigo-600 hover:text-indigo-700">
                  + 创建新版本
                </button>
              </div>
              <div class="space-y-2">
                <div
                  v-for="version in versions"
                  :key="version.id"
                  class="p-3 rounded-xl border transition-all"
                  :class="version.isActive === 1 ? 'bg-green-50 border-green-200' : 'bg-zinc-50 border-zinc-200'"
                >
                  <div class="flex items-center justify-between">
                    <div>
                      <div class="font-medium text-sm text-zinc-800">
                        版本 {{ version.versionNumber }}
                        <span v-if="version.isActive === 1" class="ml-2 px-2 py-0.5 text-xs bg-green-100 text-green-700 rounded-full">使用中</span>
                      </div>
                      <div class="text-xs text-zinc-500 mt-1">{{ version.releaseNotes || '无说明' }}</div>
                    </div>
                    <div class="flex gap-2">
                      <button
                        v-if="version.isActive !== 1"
                        @click="activateVersion(version.id!)"
                        class="text-xs px-3 py-1 bg-indigo-100 text-indigo-700 rounded-lg hover:bg-indigo-200"
                      >
                        激活
                      </button>
                      <button
                        @click="rollbackVersion(version.id!)"
                        class="text-xs px-3 py-1 bg-orange-100 text-orange-700 rounded-lg hover:bg-orange-200"
                      >
                        回滚
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-blue-50 rounded-xl p-4 border border-blue-200">
              <h3 class="text-sm font-semibold text-blue-800 mb-2">检测到的变量</h3>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="variable in detectedVariables"
                  :key="variable"
                  class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs"
                >
                  {{`\{\{${variable}\}\}`}}
                </span>
                <span v-if="detectedVariables.length === 0" class="text-sm text-blue-600">无变量</span>
              </div>
            </div>
          </div>

          <div v-else class="bg-white rounded-2xl shadow-sm border border-zinc-200 p-12 text-center">
            <FileText class="h-16 w-16 mx-auto mb-4 text-zinc-300" />
            <h3 class="text-lg font-medium text-zinc-600 mb-2">选择模板</h3>
            <p class="text-zinc-500">从左侧选择一个模板进行编辑和管理</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal-card">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-zinc-800">创建提示词模板</h3>
          <button @click="showCreateModal = false" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="form-label">模板名称</label>
            <input v-model="newTemplate.name" class="form-input" placeholder="例如：日程助手" />
          </div>
          <div>
            <label class="form-label">描述</label>
            <input v-model="newTemplate.description" class="form-input" placeholder="模板描述（可选）" />
          </div>
          <div>
            <label class="form-label">模板内容</label>
            <textarea v-model="newTemplate.content" class="form-input h-40" placeholder="使用 {{variable}} 定义变量..."></textarea>
          </div>
          <button @click="createTemplate" class="w-full btn-primary">创建</button>
        </div>
      </div>
    </div>

    <div v-if="showEditModal && selectedTemplate" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-card">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-zinc-800">编辑模板</h3>
          <button @click="showEditModal = false" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="form-label">模板名称</label>
            <input v-model="editTemplate.name" class="form-input" />
          </div>
          <div>
            <label class="form-label">描述</label>
            <input v-model="editTemplate.description" class="form-input" />
          </div>
          <div>
            <label class="form-label">模板内容</label>
            <textarea v-model="editTemplate.content" class="form-input h-40"></textarea>
          </div>
          <button @click="updateTemplate" class="w-full btn-primary">保存</button>
        </div>
      </div>
    </div>

    <div v-if="showVersionModal && selectedTemplate" class="modal-overlay" @click.self="showVersionModal = false">
      <div class="modal-card">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-zinc-800">创建新版本</h3>
          <button @click="showVersionModal = false" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="form-label">版本说明</label>
            <input v-model="newVersion.notes" class="form-input" placeholder="版本更新说明（可选）" />
          </div>
          <div>
            <label class="form-label">内容（留空使用当前内容）</label>
            <textarea v-model="newVersion.content" class="form-input h-40"></textarea>
          </div>
          <button @click="createNewVersion" class="w-full btn-primary">创建版本</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Edit, Trash2, X, FileText } from 'lucide-vue-next'
import type { PromptTemplate, PromptVersion } from '../lib/types'
import { getPromptTemplates, createPromptTemplate, updatePromptTemplate, deletePromptTemplate, getPromptVersions, activatePromptVersion, rollbackPromptVersion, createPromptVersion } from '../lib/api'

const templates = ref<PromptTemplate[]>([])
const selectedTemplate = ref<PromptTemplate | null>(null)
const versions = ref<PromptVersion[]>([])

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showVersionModal = ref(false)

const newTemplate = ref({
  name: '',
  description: '',
  content: ''
})

const editTemplate = ref({
  name: '',
  description: '',
  content: ''
})

const newVersion = ref({
  notes: '',
  content: ''
})

const detectedVariables = computed((): string[] => {
  if (!selectedTemplate.value?.templateContent) return []
  const matches = selectedTemplate.value.templateContent.match(/\{\{(\w+)\}\}/g) || []
  return [...new Set(matches.map((m: string) => m.replace(/\{\{|\}\}/g, '')))]
})

onMounted(async () => {
  await loadTemplates()
})

async function loadTemplates() {
  try {
    templates.value = await getPromptTemplates()
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

async function selectTemplate(template: PromptTemplate) {
  selectedTemplate.value = template
  editTemplate.value = {
    name: template.templateName,
    description: template.description || '',
    content: template.templateContent
  }
  await loadVersions()
}

async function loadVersions() {
  if (!selectedTemplate.value?.id) return
  try {
    versions.value = await getPromptVersions(selectedTemplate.value.id)
  } catch (error) {
    console.error('加载版本失败:', error)
  }
}

async function createTemplate() {
  try {
    await createPromptTemplate({
      name: newTemplate.value.name,
      description: newTemplate.value.description,
      content: newTemplate.value.content
    })
    showCreateModal.value = false
    newTemplate.value = { name: '', description: '', content: '' }
    await loadTemplates()
    alert('模板创建成功！')
  } catch (error) {
    console.error('创建模板失败:', error)
    alert('创建失败：' + (error as Error).message)
  }
}

async function updateTemplate() {
  if (!selectedTemplate.value?.id) return
  try {
    await updatePromptTemplate(selectedTemplate.value.id, {
      name: editTemplate.value.name,
      description: editTemplate.value.description,
      content: editTemplate.value.content
    })
    showEditModal.value = false
    await loadTemplates()
    if (selectedTemplate.value) {
      selectedTemplate.value.templateName = editTemplate.value.name
      selectedTemplate.value.description = editTemplate.value.description
      selectedTemplate.value.templateContent = editTemplate.value.content
    }
    alert('模板更新成功！')
  } catch (error) {
    console.error('更新模板失败:', error)
    alert('更新失败：' + (error as Error).message)
  }
}

async function deleteTemplate() {
  if (!selectedTemplate.value?.id || !confirm('确定要删除这个模板吗？')) return
  try {
    await deletePromptTemplate(selectedTemplate.value.id)
    selectedTemplate.value = null
    await loadTemplates()
    alert('模板删除成功！')
  } catch (error) {
    console.error('删除模板失败:', error)
    alert('删除失败：' + (error as Error).message)
  }
}

async function createNewVersion() {
  if (!selectedTemplate.value?.id) return
  try {
    await createPromptVersion(selectedTemplate.value.id, {
      content: newVersion.value.content || undefined,
      releaseNotes: newVersion.value.notes || undefined
    })
    showVersionModal.value = false
    newVersion.value = { notes: '', content: '' }
    await loadVersions()
    alert('版本创建成功！')
  } catch (error) {
    console.error('创建版本失败:', error)
    alert('创建失败：' + (error as Error).message)
  }
}

async function activateVersion(versionId: number) {
  try {
    await activatePromptVersion(versionId)
    await loadVersions()
    await loadTemplates()
    alert('版本激活成功！')
  } catch (error) {
    console.error('激活版本失败:', error)
    alert('激活失败：' + (error as Error).message)
  }
}

async function rollbackVersion(versionId: number) {
  if (!confirm('确定要回滚到这个版本吗？')) return
  try {
    await rollbackPromptVersion(versionId)
    await loadVersions()
    await loadTemplates()
    if (selectedTemplate.value) {
      const activeVersion = versions.value.find((v: PromptVersion) => v.isActive === 1)
      if (activeVersion) {
        selectedTemplate.value.templateContent = activeVersion.content || ''
      }
    }
    alert('版本回滚成功！')
  } catch (error) {
    console.error('回滚版本失败:', error)
    alert('回滚失败：' + (error as Error).message)
  }
}
</script>
