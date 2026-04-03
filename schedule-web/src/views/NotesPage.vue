<template>
  <div class="space-y-6">
    <!-- 页面头部 -->
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-zinc-800 flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center shadow-lg shadow-amber-500/30">
            <StickyNote class="h-5 w-5 text-white" />
          </div>
          笔记管理
        </h1>
        <p class="text-sm text-zinc-500 mt-1">📝 记录您的想法和灵感</p>
      </div>
      
      <button
        class="btn-primary flex items-center gap-2 shadow-lg shadow-amber-500/20"
        @click="handleCreateNote"
      >
        <Plus class="h-4 w-4" />
        新建笔记
      </button>
    </div>

    <!-- 标签切换和搜索 -->
    <div class="flex flex-col md:flex-row gap-4 items-stretch md:items-center">
      <!-- 标签切换 -->
      <div class="inline-flex rounded-xl bg-zinc-100 p-1">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="activeTab = tab.value"
          class="px-5 py-2.5 rounded-lg text-sm font-medium transition-all flex items-center gap-2"
          :class="activeTab === tab.value 
            ? 'bg-white text-amber-600 shadow-sm' 
            : 'text-zinc-600 hover:text-zinc-900'"
        >
          <component :is="tab.icon" class="h-4 w-4" />
          {{ tab.label }}
        </button>
      </div>
      
      <!-- 搜索框 -->
      <div class="flex-1 relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-400" />
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索笔记..."
          class="form-input pl-10 w-full"
          @input="handleSearch"
        />
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-gradient-to-br from-amber-50 to-orange-50 rounded-2xl p-4 border border-amber-100/50">
        <div class="text-2xl font-bold text-amber-600">{{ notes.length }}</div>
        <div class="text-xs text-zinc-500 mt-1">{{ activeTab === 'quick' ? '快速笔记' : '富文本笔记' }}</div>
      </div>
      <div class="bg-gradient-to-br from-green-50 to-emerald-50 rounded-2xl p-4 border border-green-100/50">
        <div class="text-2xl font-bold text-green-600">{{ pinnedCount }}</div>
        <div class="text-xs text-zinc-500 mt-1">已置顶笔记</div>
      </div>
      <div class="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl p-4 border border-blue-100/50">
        <div class="text-2xl font-bold text-blue-600">{{ todayCount }}</div>
        <div class="text-xs text-zinc-500 mt-1">今日新增</div>
      </div>
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-4 border border-purple-100/50">
        <div class="text-2xl font-bold text-purple-600">{{ totalWords }}</div>
        <div class="text-xs text-zinc-500 mt-1">总字数</div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="animate-spin w-10 h-10 border-4 border-amber-200 border-t-amber-600 rounded-full"></div>
    </div>
    
    <!-- 错误提示 -->
    <div v-else-if="errorMsg" class="bg-red-50 border border-red-200 rounded-2xl p-6 text-center">
      <AlertCircle class="h-12 w-12 mx-auto mb-3 text-red-400" />
      <p class="text-red-600">{{ errorMsg }}</p>
    </div>
    
    <!-- 笔记网格 -->
    <div v-else-if="notes.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="note in notes"
        :key="note.id"
        class="group relative rounded-2xl p-5 border border-zinc-200/50 hover:shadow-lg transition-all cursor-pointer"
        :style="{ backgroundColor: note.color || '#fef3c7' }"
        @click="handleNoteClick(note)"
      >
        <!-- 置顶标记 -->
        <div v-if="note.isPinned === 1" class="absolute -top-2 -right-2 w-6 h-6 bg-amber-500 rounded-full flex items-center justify-center shadow-lg">
          <Pin class="h-3 w-3 text-white" />
        </div>
        
        <!-- 内容 -->
        <div class="space-y-3">
          <h3 class="font-bold text-zinc-800 line-clamp-2">
            {{ note.title || '无标题' }}
          </h3>
          <p class="text-sm text-zinc-600 line-clamp-3">
            {{ note.content }}
          </p>
        </div>
        
        <!-- 操作按钮 -->
        <div class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1">
          <button
            @click.stop="handleTogglePin(note.id!)"
            class="p-2 rounded-lg bg-white/80 hover:bg-white shadow-sm transition-colors"
            :title="note.isPinned === 1 ? '取消置顶' : '置顶'"
          >
            <Pin class="h-4 w-4" :class="note.isPinned === 1 ? 'text-amber-600' : 'text-zinc-400'" />
          </button>
          <button
            @click.stop="handleDelete(note.id!)"
            class="p-2 rounded-lg bg-white/80 hover:bg-red-50 shadow-sm transition-colors"
            title="删除"
          >
            <Trash2 class="h-4 w-4 text-zinc-400 hover:text-red-600" />
          </button>
        </div>
        
        <!-- 底部信息 -->
        <div class="mt-4 pt-3 border-t border-zinc-200/30 flex items-center justify-between text-xs text-zinc-500">
          <span>{{ formatDate(note.updatedAt) }}</span>
          <span v-if="note.content">{{ note.content.length }} 字</span>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="bg-zinc-50 rounded-2xl border border-zinc-200 p-12 text-center">
      <StickyNote class="h-16 w-16 mx-auto mb-4 text-zinc-300" />
      <h3 class="text-lg font-medium text-zinc-600 mb-2">暂无笔记</h3>
      <p class="text-zinc-500 mb-4">点击上方按钮创建您的第一篇笔记</p>
      <button @click="handleCreateNote" class="btn-primary">
        <Plus class="h-4 w-4 inline mr-2" />
        新建笔记
      </button>
    </div>

    <!-- 创建/编辑笔记弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card max-w-2xl">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center shadow-lg">
              <StickyNote class="h-6 w-6 text-white" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-zinc-800">{{ isEditing ? '编辑笔记' : '新建笔记' }}</h3>
              <p class="text-xs text-zinc-500">{{ isEditing ? '修改笔记内容' : '记录您的想法' }}</p>
            </div>
          </div>
          <button @click="showModal = false" class="p-2 rounded-xl hover:bg-zinc-100 transition-colors">
            <X class="h-5 w-5 text-zinc-400" />
          </button>
        </div>

        <div class="space-y-5">
          <!-- 标题 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <FileText class="h-4 w-4 text-amber-500" />
              标题
            </label>
            <input
              v-model="editingNote.title"
              type="text"
              class="form-input"
              placeholder="输入笔记标题（可选）"
            />
          </div>

          <!-- 内容 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <AlignLeft class="h-4 w-4 text-amber-500" />
              内容
            </label>
            <textarea
              v-model="editingNote.content"
              rows="8"
              class="form-input resize-none font-mono text-sm"
              :placeholder="activeTab === 'quick' ? '输入笔记内容...' : '使用 Markdown 编写内容...'"
            />
          </div>

          <!-- 颜色选择 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <Palette class="h-4 w-4 text-amber-500" />
              笔记颜色
            </label>
            <div class="flex gap-2">
              <button
                v-for="color in noteColors"
                :key="color.value"
                @click="editingNote.color = color.value"
                class="w-8 h-8 rounded-full transition-transform hover:scale-110 ring-2 ring-offset-2"
                :class="editingNote.color === color.value ? 'ring-amber-500' : 'ring-transparent'"
                :style="{ backgroundColor: color.value }"
                :title="color.label"
              />
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-3 pt-2">
            <button @click="showModal = false" class="btn-secondary flex-1">
              取消
            </button>
            <button @click="handleSaveNote" class="btn-primary flex-1 flex items-center justify-center gap-2">
              <Check class="h-4 w-4" />
              {{ isEditing ? '保存修改' : '创建笔记' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Pin, Trash2, BookOpen, StickyNote, FileText, AlignLeft, Palette, Check, X, AlertCircle } from 'lucide-vue-next'
import { getNotes, createNote, updateNote, deleteNote, toggleNotePin } from '../lib/api'
import type { Note } from '../lib/types'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref<string | null>(null)
const notes = ref<Note[]>([])
const keyword = ref('')
const activeTab = ref<'quick' | 'rich'>('quick')
const showModal = ref(false)
const isEditing = ref(false)
const editingNote = ref({
  id: null as number | null,
  title: '',
  content: '',
  contentType: 'quick' as 'quick' | 'rich',
  color: '#fef3c7'
})

const noteColors = [
  { value: '#fef3c7', label: '黄色' },
  { value: '#d1fae5', label: '绿色' },
  { value: '#dbeafe', label: '蓝色' },
  { value: '#fce7f3', label: '粉色' },
  { value: '#e5e7eb', label: '灰色' },
  { value: '#ffffff', label: '白色' }
]

const tabs = [
  { label: '快速笔记', value: 'quick' as const, icon: StickyNote },
  { label: '富文本笔记', value: 'rich' as const, icon: BookOpen }
]

const pinnedCount = computed(() => notes.value.filter(n => n.isPinned === 1).length)
const todayCount = computed(() => {
  const today = new Date().toDateString()
  return notes.value.filter(n => n.createdAt && new Date(n.createdAt).toDateString() === today).length
})
const totalWords = computed(() => {
  return notes.value.reduce((sum, note) => sum + (note.content?.length || 0), 0)
})

function formatDate(date?: string) {
  if (!date) return ''
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

onMounted(async () => {
  await loadNotes()
})

async function loadNotes() {
  loading.value = true
  errorMsg.value = null
  try {
    const result = await getNotes({
      contentType: activeTab.value,
      keyword: keyword.value || undefined,
      page: 1,
      size: 100
    })
    notes.value = result.list || []
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

let searchTimeout: number | null = null
function handleSearch() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = window.setTimeout(() => {
    loadNotes()
  }, 300)
}

function handleCreateNote() {
  isEditing.value = false
  editingNote.value = {
    id: null,
    title: '',
    content: '',
    contentType: activeTab.value,
    color: '#fef3c7'
  }
  showModal.value = true
}

function handleNoteClick(note: Note) {
  if (note.contentType === 'rich') {
    router.push(`/notes/${note.id}`)
  } else {
    isEditing.value = true
    editingNote.value = {
      id: note.id || null,
      title: note.title || '',
      content: note.content || '',
      contentType: 'quick',
      color: note.color || '#fef3c7'
    }
    showModal.value = true
  }
}

async function handleSaveNote() {
  if (!editingNote.value.content && !editingNote.value.title) {
    errorMsg.value = '请输入内容'
    return
  }
  
  try {
    if (isEditing.value && editingNote.value.id) {
      await updateNote(editingNote.value.id, {
        title: editingNote.value.title,
        content: editingNote.value.content,
        color: editingNote.value.color
      })
    } else {
      await createNote({
        title: editingNote.value.title || editingNote.value.content?.slice(0, 30),
        content: editingNote.value.content,
        contentType: editingNote.value.contentType,
        color: editingNote.value.color
      })
    }
    showModal.value = false
    await loadNotes()
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定删除这条笔记吗？')) return
  try {
    await deleteNote(id)
    await loadNotes()
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}

async function handleTogglePin(id: number) {
  try {
    await toggleNotePin(id)
    await loadNotes()
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}
</script>
