<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 p-6">
    <div class="max-w-7xl mx-auto">
      <div class="mb-8 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-zinc-800 mb-2">Skills 管理</h1>
          <p class="text-zinc-500">管理和配置AI技能，支持版本控制和分类管理</p>
        </div>
        <button @click="showCreateModal = true" class="btn-primary flex items-center gap-2">
          <Plus class="h-5 w-5" />
          创建技能
        </button>
      </div>

      <div class="mb-6 flex gap-4">
        <div class="flex-1">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索技能..."
            class="form-input"
            @input="handleSearch"
          />
        </div>
        <select v-model="selectedCategory" class="form-input w-48" @change="loadSkills">
          <option :value="undefined">所有分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.categoryName }}
          </option>
        </select>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="skill in skills"
          :key="skill.id"
          class="bg-white rounded-2xl shadow-sm border border-zinc-200 p-6 hover:shadow-lg transition-shadow cursor-pointer"
          :style="{ borderLeftColor: skill.color || '#3b82f6', borderLeftWidth: '4px' }"
          @click="selectSkill(skill)"
        >
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-3">
              <div
                class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl"
                :style="{ backgroundColor: skill.color ? skill.color + '20' : '#3b82f620' }"
              >
                {{ skill.icon || '🤖' }}
              </div>
              <div>
                <h3 class="font-bold text-zinc-800">{{ skill.skillName }}</h3>
                <span class="text-xs text-zinc-500">{{ skill.skillType || 'assistant' }}</span>
              </div>
            </div>
            <div
              class="px-2 py-1 rounded-full text-xs font-medium"
              :class="skill.status === 1 ? 'bg-green-100 text-green-700' : 'bg-zinc-100 text-zinc-500'"
            >
              {{ skill.status === 1 ? '启用' : '禁用' }}
            </div>
          </div>

          <p class="text-sm text-zinc-600 mb-4 line-clamp-2">
            {{ skill.description || '暂无描述' }}
          </p>

          <div class="flex items-center justify-between text-xs text-zinc-500">
            <span>优先级: {{ skill.priority || 0 }}</span>
            <span>{{ formatDate(skill.createdAt) }}</span>
          </div>
        </div>

        <div v-if="skills.length === 0" class="col-span-full text-center py-12">
          <Bot class="h-16 w-16 mx-auto mb-4 text-zinc-300" />
          <h3 class="text-lg font-medium text-zinc-600 mb-2">暂无技能</h3>
          <p class="text-zinc-500 mb-4">点击上方按钮创建第一个AI技能</p>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal-card max-w-2xl">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-zinc-800">创建新技能</h3>
          <button @click="showCreateModal = false" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="form-label">技能名称 *</label>
            <input v-model="newSkill.skillName" class="form-input" placeholder="例如：日程助手" />
          </div>

          <div>
            <label class="form-label">技能代码 *</label>
            <input v-model="newSkill.skillCode" class="form-input" placeholder="例如：schedule_assistant" />
          </div>

          <div>
            <label class="form-label">描述</label>
            <textarea v-model="newSkill.description" class="form-input h-20" placeholder="技能描述..."></textarea>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label">分类</label>
              <select v-model="newSkill.categoryId" class="form-input">
                <option :value="null">无分类</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.categoryName }}
                </option>
              </select>
            </div>
            <div>
              <label class="form-label">类型</label>
              <select v-model="newSkill.skillType" class="form-input">
                <option value="assistant">助手</option>
                <option value="analyzer">分析器</option>
                <option value="generator">生成器</option>
                <option value="custom">自定义</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label">图标</label>
              <input v-model="newSkill.icon" class="form-input" placeholder="🤖" />
            </div>
            <div>
              <label class="form-label">颜色</label>
              <input v-model="newSkill.color" type="color" class="form-input h-10" />
            </div>
          </div>

          <div>
            <label class="form-label">提示词模板</label>
            <textarea v-model="newSkill.promptTemplate" class="form-input h-32" placeholder="定义技能的提示词..."></textarea>
          </div>

          <div class="grid grid-cols-3 gap-4">
            <div>
              <label class="form-label">模型</label>
              <input v-model="newSkill.modelName" class="form-input" placeholder="gpt-4o-mini" />
            </div>
            <div>
              <label class="form-label">温度: {{ newSkill.temperature || 0.7 }}</label>
              <input v-model.number="newSkill.temperature" type="range" min="0" max="2" step="0.1" class="w-full" />
            </div>
            <div>
              <label class="form-label">最大Token</label>
              <input v-model.number="newSkill.maxTokens" type="number" class="form-input" placeholder="2048" />
            </div>
          </div>

          <button @click="handleCreate" class="w-full btn-primary">创建</button>
        </div>
      </div>
    </div>

    <div v-if="selectedSkillData" class="modal-overlay" @click.self="selectedSkillData = null">
      <div class="modal-card max-w-4xl max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-4">
            <div
              class="w-16 h-16 rounded-2xl flex items-center justify-center text-3xl"
              :style="{ backgroundColor: selectedSkillData.skill.color ? selectedSkillData.skill.color + '20' : '#3b82f620' }"
            >
              {{ selectedSkillData.skill.icon || '🤖' }}
            </div>
            <div>
              <h3 class="text-xl font-bold text-zinc-800">{{ selectedSkillData.skill.skillName }}</h3>
              <p class="text-sm text-zinc-500">{{ selectedSkillData.skill.description }}</p>
            </div>
          </div>
          <button @click="selectedSkillData = null" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>

        <div class="grid grid-cols-2 gap-6 mb-6">
          <div>
            <h4 class="font-semibold text-zinc-700 mb-3">基本信息</h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-zinc-500">类型:</span>
                <span>{{ selectedSkillData.skill.skillType }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-zinc-500">模型:</span>
                <span>{{ selectedSkillData.skill.modelName || '默认' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-zinc-500">温度:</span>
                <span>{{ selectedSkillData.skill.temperature || 0.7 }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-zinc-500">优先级:</span>
                <span>{{ selectedSkillData.skill.priority || 0 }}</span>
              </div>
            </div>
          </div>

          <div>
            <h4 class="font-semibold text-zinc-700 mb-3">版本历史</h4>
            <div class="space-y-2">
              <div
                v-for="version in selectedSkillData.versions"
                :key="version.id"
                class="p-3 rounded-lg border"
                :class="version.isActive === 1 ? 'bg-green-50 border-green-200' : 'bg-zinc-50 border-zinc-200'"
              >
                <div class="flex items-center justify-between mb-1">
                  <span class="font-medium">{{ version.versionName }}</span>
                  <span v-if="version.isActive === 1" class="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded-full">使用中</span>
                </div>
                <p class="text-xs text-zinc-500">{{ version.releaseNotes || '无说明' }}</p>
                <div class="flex gap-2 mt-2">
                  <button
                    v-if="version.isActive !== 1"
                    @click="activateVersion(version.id!)"
                    class="text-xs px-2 py-1 bg-indigo-100 text-indigo-700 rounded hover:bg-indigo-200"
                  >
                    激活
                  </button>
                  <span class="text-xs text-zinc-400">使用 {{ version.usageCount || 0 }} 次</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="mb-6">
          <h4 class="font-semibold text-zinc-700 mb-3">当前提示词模板</h4>
          <div class="bg-zinc-50 rounded-xl p-4 border border-zinc-200">
            <pre class="whitespace-pre-wrap text-sm text-zinc-700 font-mono">{{ selectedSkillData.skill.promptTemplate || '暂无模板' }}</pre>
          </div>
        </div>

        <div class="flex gap-3">
          <button @click="showEditModal = true" class="btn-secondary flex-1">编辑技能</button>
          <button @click="handleDelete(selectedSkillData.skill.id!)" class="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200">删除</button>
        </div>
      </div>
    </div>

    <div v-if="showEditModal && selectedSkillData" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-card max-w-2xl">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-zinc-800">编辑技能</h3>
          <button @click="showEditModal = false" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="form-label">技能名称 *</label>
            <input v-model="editSkill.skillName" class="form-input" />
          </div>

          <div>
            <label class="form-label">描述</label>
            <textarea v-model="editSkill.description" class="form-input h-20"></textarea>
          </div>

          <div>
            <label class="form-label">提示词模板</label>
            <textarea v-model="editSkill.promptTemplate" class="form-input h-32"></textarea>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label">模型</label>
              <input v-model="editSkill.modelName" class="form-input" />
            </div>
            <div>
              <label class="form-label">优先级</label>
              <input v-model.number="editSkill.priority" type="number" class="form-input" />
            </div>
          </div>

          <div class="flex gap-3">
            <button @click="handleUpdate" class="btn-primary flex-1">保存更改</button>
            <button @click="createNewVersion" class="btn-secondary flex-1">创建新版本</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCategoryModal" class="modal-overlay" @click.self="showCategoryModal = false">
      <div class="modal-card max-w-md">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-zinc-800">管理分类</h3>
          <button @click="showCategoryModal = false" class="p-2 hover:bg-zinc-100 rounded-lg">
            <X class="h-5 w-5 text-zinc-500" />
          </button>
        </div>

        <div class="space-y-3 mb-6">
          <div v-for="cat in categories" :key="cat.id" class="flex items-center justify-between p-3 bg-zinc-50 rounded-lg">
            <div>
              <div class="font-medium">{{ cat.categoryName }}</div>
              <div class="text-xs text-zinc-500">{{ cat.categoryCode }}</div>
            </div>
            <button @click="handleDeleteCategory(cat.id!)" class="text-red-500 hover:text-red-700">
              <Trash2 class="h-4 w-4" />
            </button>
          </div>
        </div>

        <div class="space-y-3">
          <input v-model="newCategory.name" class="form-input" placeholder="分类名称" />
          <input v-model="newCategory.code" class="form-input" placeholder="分类代码" />
          <button @click="handleCreateCategory" class="w-full btn-primary">添加分类</button>
        </div>
      </div>
    </div>

    <button
      @click="showCategoryModal = true"
      class="fixed bottom-6 left-6 w-12 h-12 rounded-full bg-white border border-zinc-200 shadow-lg text-zinc-600 hover:text-zinc-900 flex items-center justify-center"
      title="管理分类"
    >
      <Layers class="h-5 w-5" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, X, Bot, Trash2, Layers } from 'lucide-vue-next'
import type { Skill, SkillCategory } from '../lib/types'
import { getSkills, createSkill, updateSkill, deleteSkill, getSkillCategories, createSkillCategory, deleteSkillCategory, getSkillDetails, activateSkillVersion, createSkillVersion } from '../lib/api'

const skills = ref<Skill[]>([])
const categories = ref<SkillCategory[]>([])
const selectedSkillData = ref<any>(null)
const searchKeyword = ref('')
const selectedCategory = ref<number | undefined>(undefined)

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showCategoryModal = ref(false)

const newSkill = ref({
  skillName: '',
  skillCode: '',
  description: '',
  categoryId: null as number | null,
  skillType: 'assistant',
  icon: '🤖',
  color: '#3b82f6',
  promptTemplate: '',
  modelName: '',
  temperature: 0.7,
  maxTokens: 2048
})

const editSkill = ref({
  skillName: '',
  description: '',
  promptTemplate: '',
  modelName: '',
  priority: 0
})

const newCategory = ref({
  name: '',
  code: ''
})

onMounted(async () => {
  await loadSkills()
  await loadCategories()
})

async function loadSkills() {
  try {
    skills.value = await getSkills(1, selectedCategory.value || undefined)
  } catch (error) {
    console.error('加载技能失败:', error)
  }
}

async function loadCategories() {
  try {
    categories.value = await getSkillCategories()
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

async function handleSearch() {
  if (searchKeyword.value.trim()) {
    try {
      skills.value = await getSkills(1)
      skills.value = skills.value.filter(s =>
        s.skillName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        (s.description && s.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
      )
    } catch (error) {
      console.error('搜索失败:', error)
    }
  } else {
    await loadSkills()
  }
}

async function selectSkill(skill: Skill) {
  try {
    selectedSkillData.value = await getSkillDetails(skill.id!)
    editSkill.value = {
      skillName: selectedSkillData.value.skill.skillName,
      description: selectedSkillData.value.skill.description || '',
      promptTemplate: selectedSkillData.value.skill.promptTemplate || '',
      modelName: selectedSkillData.value.skill.modelName || '',
      priority: selectedSkillData.value.skill.priority || 0
    }
  } catch (error) {
    console.error('加载技能详情失败:', error)
  }
}

async function handleCreate() {
  try {
    await createSkill(1, {
      ...newSkill.value,
      categoryId: newSkill.value.categoryId || null
    })
    showCreateModal.value = false
    newSkill.value = {
      skillName: '',
      skillCode: '',
      description: '',
      categoryId: null as number | null,
      skillType: 'assistant',
      icon: '🤖',
      color: '#3b82f6',
      promptTemplate: '',
      modelName: '',
      temperature: 0.7,
      maxTokens: 2048
    }
    await loadSkills()
    alert('技能创建成功！')
  } catch (error) {
    console.error('创建失败:', error)
    alert('创建失败：' + (error as Error).message)
  }
}

async function handleUpdate() {
  if (!selectedSkillData.value) return
  try {
    await updateSkill(selectedSkillData.value.skill.id!, editSkill.value)
    showEditModal.value = false
    await selectSkill(selectedSkillData.value.skill)
    await loadSkills()
    alert('更新成功！')
  } catch (error) {
    console.error('更新失败:', error)
    alert('更新失败：' + (error as Error).message)
  }
}

async function handleDelete(skillId: number) {
  if (!confirm('确定要删除这个技能吗？')) return
  try {
    await deleteSkill(skillId)
    selectedSkillData.value = null
    await loadSkills()
    alert('删除成功！')
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败：' + (error as Error).message)
  }
}

async function activateVersion(versionId: number) {
  try {
    await activateSkillVersion(versionId)
    if (selectedSkillData.value) {
      await selectSkill(selectedSkillData.value.skill)
    }
    alert('版本激活成功！')
  } catch (error) {
    console.error('激活失败:', error)
    alert('激活失败：' + (error as Error).message)
  }
}

async function createNewVersion() {
  if (!selectedSkillData.value) return
  try {
    await createSkillVersion(selectedSkillData.value.skill.id!, {
      promptTemplate: editSkill.value.promptTemplate,
      releaseNotes: '手动创建版本'
    })
    await selectSkill(selectedSkillData.value.skill)
    alert('版本创建成功！')
  } catch (error) {
    console.error('创建版本失败:', error)
    alert('创建版本失败：' + (error as Error).message)
  }
}

async function handleCreateCategory() {
  if (!newCategory.value.name || !newCategory.value.code) {
    alert('请填写完整的分类信息')
    return
  }
  try {
    await createSkillCategory(1, newCategory.value)
    newCategory.value = { name: '', code: '' }
    await loadCategories()
    alert('分类创建成功！')
  } catch (error) {
    console.error('创建分类失败:', error)
    alert('创建分类失败：' + (error as Error).message)
  }
}

async function handleDeleteCategory(categoryId: number) {
  if (!confirm('确定要删除这个分类吗？')) return
  try {
    await deleteSkillCategory(categoryId)
    await loadCategories()
    alert('分类删除成功！')
  } catch (error) {
    console.error('删除分类失败:', error)
    alert('删除分类失败：' + (error as Error).message)
  }
}

function formatDate(date?: string) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}
</script>
