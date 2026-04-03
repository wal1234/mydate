<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Save, Trash2, Tag, Image, Loader2 } from 'lucide-vue-next'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { getNoteById, createNote, updateNote, deleteNote, getNoteCategories, uploadImage } from '../lib/api'
import type { Note, NoteCategory } from '../lib/types'

const route = useRoute()
const router = useRouter()

const noteId = computed(() => route.params.id as string)
const isNew = computed(() => noteId.value === 'new')

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const errorMsg = ref<string | null>(null)
const okMsg = ref<string | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const note = ref<Partial<Note>>({
  title: '',
  content: '',
  contentType: 'rich',
  tags: ''
})
const categories = ref<NoteCategory[]>([])
const previewHtml = ref('')

function updatePreview() {
  if (note.value.content) {
    const html = marked.lexer(note.value.content)
    const tokens = marked.parser(html)
    previewHtml.value = DOMPurify.sanitize(tokens)
  } else {
    previewHtml.value = ''
  }
}

watch(() => note.value.content, updatePreview, { immediate: true })

async function loadNote() {
  if (isNew.value) {
    note.value = { title: '', content: '', contentType: 'rich', tags: '' }
    return
  }
  
  loading.value = true
  errorMsg.value = null
  try {
    const data = await getNoteById(parseInt(noteId.value))
    note.value = data
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = await getNoteCategories()
  } catch (e) {
    console.error('Failed to load categories:', e)
  }
}

async function save() {
  saving.value = true
  errorMsg.value = null
  okMsg.value = null
  try {
    if (isNew.value) {
      await createNote(note.value)
      okMsg.value = '创建成功'
      setTimeout(() => {
        router.push('/notes')
      }, 500)
    } else {
      await updateNote(parseInt(noteId.value), note.value)
      okMsg.value = '保存成功'
    }
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!confirm('确定删除这篇笔记吗？此操作不可恢复。')) return
  try {
    await deleteNote(parseInt(noteId.value))
    router.push('/notes')
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}

async function handleImageUpload(file: File) {
  if (!file || !file.type.startsWith('image/')) {
    errorMsg.value = '请选择图片文件'
    return
  }
  
  if (file.size > 10 * 1024 * 1024) {
    errorMsg.value = '图片大小不能超过10MB'
    return
  }
  
  uploading.value = true
  errorMsg.value = null
  
  try {
    const url = await uploadImage(file)
    const imageMarkdown = `\n![${file.name}](${url})\n`
    
    const textarea = textareaRef.value
    if (textarea) {
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const content = note.value.content || ''
      note.value.content = content.slice(0, start) + imageMarkdown + content.slice(end)
      
      setTimeout(() => {
        textarea.focus()
        textarea.setSelectionRange(start + imageMarkdown.length, start + imageMarkdown.length)
      }, 0)
    } else {
      note.value.content = (note.value.content || '') + imageMarkdown
    }
    
    okMsg.value = '图片上传成功'
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    uploading.value = false
  }
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    handleImageUpload(file)
  }
  target.value = ''
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handlePaste(event: ClipboardEvent) {
  const items = event.clipboardData?.items
  if (!items) return
  
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      const file = item.getAsFile()
      if (file) {
        handleImageUpload(file)
      }
      break
    }
  }
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  const files = event.dataTransfer?.files
  if (!files) return
  
  for (const file of files) {
    if (file.type.startsWith('image/')) {
      handleImageUpload(file)
    }
  }
}

onMounted(() => {
  loadNote()
  loadCategories()
})
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-center justify-between gap-2">
      <div class="flex items-center gap-3">
        <button
          class="p-2 rounded-lg hover:bg-zinc-100"
          @click="router.push('/notes')"
        >
          <ArrowLeft class="h-5 w-5" />
        </button>
        <div>
          <div class="text-lg font-semibold">{{ isNew ? '新建知识库笔记' : '编辑笔记' }}</div>
          <div v-if="!isNew" class="text-sm text-zinc-500">#{{ noteId }}</div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button
          v-if="!isNew"
          class="p-2 rounded-lg text-red-600 hover:bg-red-50"
          @click="handleDelete"
        >
          <Trash2 class="h-5 w-5" />
        </button>
        <button
          class="flex items-center px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60"
          :disabled="saving"
          @click="save"
        >
          <Save class="inline h-4 w-4 mr-1" />
          保存
        </button>
      </div>
    </div>

    <div v-if="errorMsg" class="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
      {{ errorMsg }}
    </div>
    <div v-if="okMsg" class="rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
      {{ okMsg }}
    </div>

    <div class="grid gap-4 grid-cols-1 lg:grid-cols-2">
      <section class="space-y-3">
        <input
          v-model="note.title"
          class="w-full text-xl font-semibold px-4 py-3 border border-zinc-200 rounded-xl outline-none focus:ring-2 focus:ring-indigo-500/20"
          placeholder="笔记标题..."
        />

        <div class="flex items-center gap-2">
          <Tag class="h-4 w-4 text-zinc-400" />
          <input
            v-model="note.tags"
            type="text"
            class="flex-1 text-sm px-3 py-2 border border-zinc-200 rounded-lg outline-none"
            placeholder="标签（逗号分隔）"
          />
        </div>

        <div class="flex items-center gap-3 p-3 bg-zinc-50 rounded-xl border border-zinc-200">
          <button
            class="flex items-center gap-2 px-4 py-2 text-sm bg-white border border-zinc-300 rounded-lg hover:bg-zinc-100 transition-colors"
            :disabled="uploading"
            @click="triggerFileInput"
          >
            <Loader2 v-if="uploading" class="h-4 w-4 animate-spin text-indigo-600" />
            <Image v-else class="h-4 w-4 text-indigo-600" />
            <span class="font-medium">上传图片</span>
          </button>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            multiple
            class="hidden"
            @change="handleFileSelect"
          />
          <div class="text-xs text-zinc-500">
            支持点击上传、Ctrl+V 粘贴、拖拽图片
          </div>
        </div>

        <textarea
          ref="textareaRef"
          v-model="note.content"
          class="h-[450px] w-full resize-none rounded-xl border border-zinc-200 px-4 py-3 text-sm font-mono outline-none focus:ring-2 focus:ring-indigo-500/20"
          placeholder="使用 Markdown 编写内容..."
          @paste="handlePaste"
          @dragover="handleDragOver"
          @drop="handleDrop"
        />
      </section>

      <section>
        <div class="text-sm font-medium text-zinc-500 mb-2">预览</div>
        <article
          class="prose prose-zinc max-w-none rounded-xl border border-zinc-200 bg-zinc-50 p-6 h-[500px] overflow-auto"
          v-html="previewHtml"
        />
      </section>
    </div>
  </div>
</template>
