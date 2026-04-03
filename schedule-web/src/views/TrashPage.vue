<template>
  <div class="min-h-screen bg-gradient-to-br from-zinc-50 via-white to-stone-50 p-6">
    <div class="max-w-6xl mx-auto">
      <div class="mb-8 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-zinc-800 mb-2">垃圾箱</h1>
          <p class="text-zinc-500">查看和管理已删除的项目，30天后自动永久删除</p>
        </div>
        <div class="flex gap-3">
          <button @click="loadTrashItems" class="btn-secondary flex items-center gap-2">
            <RefreshCw class="h-4 w-4" />
            刷新
          </button>
          <button @click="handleEmptyTrash" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 flex items-center gap-2 transition-colors">
            <Trash2 class="h-4 w-4" />
            清空垃圾箱
          </button>
        </div>
      </div>

      <div v-if="stats" class="mb-6 grid grid-cols-4 gap-4">
        <div class="bg-white rounded-xl p-4 border border-zinc-200">
          <div class="text-2xl font-bold text-zinc-800">{{ stats.total }}</div>
          <div class="text-sm text-zinc-500">总计</div>
        </div>
        <div class="bg-white rounded-xl p-4 border border-zinc-200">
          <div class="text-2xl font-bold text-blue-600">{{ stats.schedules }}</div>
          <div class="text-sm text-zinc-500">日程</div>
        </div>
        <div class="bg-white rounded-xl p-4 border border-zinc-200">
          <div class="text-2xl font-bold text-green-600">{{ stats.documents }}</div>
          <div class="text-sm text-zinc-500">文档</div>
        </div>
        <div class="bg-white rounded-xl p-4 border border-zinc-200">
          <div class="text-2xl font-bold text-purple-600">{{ stats.notes }}</div>
          <div class="text-sm text-zinc-500">笔记</div>
        </div>
      </div>

      <div class="mb-6 flex gap-4">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="selectedTab = tab.value"
          class="px-4 py-2 rounded-lg transition-colors"
          :class="selectedTab === tab.value ? 'bg-indigo-600 text-white' : 'bg-white text-zinc-600 hover:bg-zinc-50'"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="space-y-4">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="bg-white rounded-2xl shadow-sm border border-zinc-200 p-6 hover:shadow-md transition-shadow"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start gap-4 flex-1">
              <div
                class="w-12 h-12 rounded-xl flex items-center justify-center text-2xl"
                :class="getTypeBgClass(item.itemType)"
              >
                {{ getTypeIcon(item.itemType) }}
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <h3 class="font-bold text-lg text-zinc-800">{{ item.itemTitle || '未命名' }}</h3>
                  <span class="px-2 py-1 rounded-full text-xs" :class="getTypeBadgeClass(item.itemType)">
                    {{ getTypeLabel(item.itemType) }}
                  </span>
                </div>
                <p v-if="item.itemContent" class="text-sm text-zinc-600 mb-3 line-clamp-2">
                  {{ item.itemContent }}
                </p>
                <div class="flex items-center gap-4 text-xs text-zinc-500">
                  <span>删除于 {{ formatDate(item.deletedAt) }}</span>
                  <span>·</span>
                  <span>将于 {{ formatDate(item.expireAt) }} 过期</span>
                  <span v-if="getDaysLeft(item.expireAt) <= 7" class="text-orange-600 font-medium">
                    · {{ getDaysLeft(item.expireAt) }} 天后永久删除
                  </span>
                </div>
              </div>
            </div>
            <div class="flex gap-2 ml-4">
              <button
                @click="handleRestore(item)"
                class="px-4 py-2 bg-green-100 text-green-700 rounded-lg hover:bg-green-200 flex items-center gap-2 transition-colors"
              >
                <RotateCcw class="h-4 w-4" />
                恢复
              </button>
              <button
                @click="handlePermanentDelete(item)"
                class="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 flex items-center gap-2 transition-colors"
              >
                <Trash2 class="h-4 w-4" />
                永久删除
              </button>
            </div>
          </div>
        </div>

        <div v-if="filteredItems.length === 0" class="bg-white rounded-2xl shadow-sm border border-zinc-200 p-12 text-center">
          <Trash2 class="h-16 w-16 mx-auto mb-4 text-zinc-300" />
          <h3 class="text-lg font-medium text-zinc-600 mb-2">垃圾箱是空的</h3>
          <p class="text-zinc-500">删除的项目会显示在这里</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RotateCcw, Trash2, RefreshCw } from 'lucide-vue-next'
import type { TrashItem } from '../lib/types'
import { getTrashItems, getTrashStats, restoreTrashItem, permanentDeleteTrashItem, emptyTrash } from '../lib/api'

const trashItems = ref<TrashItem[]>([])
const stats = ref<{ total: number; schedules: number; documents: number; notes: number } | null>(null)
const selectedTab = ref('all')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '日程', value: 'schedule' },
  { label: '文档', value: 'document' },
  { label: '笔记', value: 'note' }
]

const filteredItems = computed(() => {
  if (selectedTab.value === 'all') {
    return trashItems.value
  }
  return trashItems.value.filter(item => item.itemType === selectedTab.value)
})

onMounted(async () => {
  await loadTrashItems()
  await loadStats()
})

async function loadTrashItems() {
  try {
    trashItems.value = await getTrashItems()
  } catch (error) {
    console.error('加载垃圾箱失败:', error)
    alert('加载失败：' + (error as Error).message)
  }
}

async function loadStats() {
  try {
    stats.value = await getTrashStats()
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

async function handleRestore(item: TrashItem) {
  if (!item.id) return
  if (!confirm('确定要恢复这个项目吗？')) return
  
  try {
    await restoreTrashItem(item.id)
    await loadTrashItems()
    await loadStats()
    alert('恢复成功！')
  } catch (error) {
    console.error('恢复失败:', error)
    alert('恢复失败：' + (error as Error).message)
  }
}

async function handlePermanentDelete(item: TrashItem) {
  if (!item.id) return
  if (!confirm('确定要永久删除吗？此操作不可恢复！')) return
  
  try {
    await permanentDeleteTrashItem(item.id)
    await loadTrashItems()
    await loadStats()
    alert('已永久删除')
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败：' + (error as Error).message)
  }
}

async function handleEmptyTrash() {
  if (!confirm('确定要清空整个垃圾箱吗？此操作不可恢复！')) return
  
  try {
    const count = await emptyTrash()
    await loadTrashItems()
    await loadStats()
    alert(`已清空垃圾箱，${count} 个项目已永久删除`)
  } catch (error) {
    console.error('清空失败:', error)
    alert('清空失败：' + (error as Error).message)
  }
}

function getTypeIcon(type?: string) {
  switch (type) {
    case 'schedule': return '📅'
    case 'document': return '📄'
    case 'note': return '📝'
    default: return '📦'
  }
}

function getTypeLabel(type?: string) {
  switch (type) {
    case 'schedule': return '日程'
    case 'document': return '文档'
    case 'note': return '笔记'
    default: return '其他'
  }
}

function getTypeBgClass(type?: string) {
  switch (type) {
    case 'schedule': return 'bg-blue-100'
    case 'document': return 'bg-green-100'
    case 'note': return 'bg-purple-100'
    default: return 'bg-zinc-100'
  }
}

function getTypeBadgeClass(type?: string) {
  switch (type) {
    case 'schedule': return 'bg-blue-100 text-blue-700'
    case 'document': return 'bg-green-100 text-green-700'
    case 'note': return 'bg-purple-100 text-purple-700'
    default: return 'bg-zinc-100 text-zinc-700'
  }
}

function formatDate(date?: string) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getDaysLeft(expireAt?: string) {
  if (!expireAt) return 0
  const expire = new Date(expireAt)
  const now = new Date()
  const diff = expire.getTime() - now.getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}
</script>
