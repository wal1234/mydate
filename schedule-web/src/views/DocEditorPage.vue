<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import dayjs from 'dayjs'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { createDailyDoc, getDailyDocByDate, updateDailyDoc } from '../lib/api'
import type { DailyDocument } from '../lib/types'

type Props = {
  date?: string
}

const props = defineProps<Props>()

const date = ref(props.date || dayjs().format('YYYY-MM-DD'))
const loading = ref(false)
const saving = ref(false)
const errorMsg = ref<string | null>(null)
const okMsg = ref<string | null>(null)

const doc = ref<DailyDocument | null>(null)
const title = ref('')
const content = ref('')
const contentType = ref<'plain' | 'markdown' | 'word'>('markdown')
const quillEditor = ref<any>(null)
const quillInstance = ref<Quill | null>(null)

const previewHtml = ref('')

function updatePreview() {
  if (contentType.value === 'markdown') {
    // 使用marked的同步解析方法
    const html = marked.lexer(content.value || '')
    const tokens = marked.parser(html)
    previewHtml.value = DOMPurify.sanitize(tokens)
  } else if (contentType.value === 'word') {
    previewHtml.value = DOMPurify.sanitize(quillInstance.value?.root.innerHTML || '')
  } else {
    // 纯文本格式
    previewHtml.value = DOMPurify.sanitize(`<pre>${(content.value || '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#39;')}</pre>`)
  }
}

// 监听内容和类型变化，更新预览
watch([content, contentType], updatePreview, { deep: true })

async function load() {
  loading.value = true
  errorMsg.value = null
  okMsg.value = null
  try {
    const d = await getDailyDocByDate(date.value)
    doc.value = d
    title.value = d.title ?? ''
    content.value = d.content ?? ''
    if ((d.contentType as any) === 'plain') {
      contentType.value = 'plain'
    } else if ((d.contentType as any) === 'word') {
      contentType.value = 'word'
      // 延迟初始化Quill编辑器，确保DOM已经更新
      nextTick(() => {
        initQuillEditor(content.value)
      })
    } else {
      contentType.value = 'markdown'
    }
  } catch (e) {
    // 后端可能返回 404（Result code!=0），这里当作“尚未创建”
    doc.value = null
    title.value = ''
    content.value = ''
  } finally {
    loading.value = false
    // 加载完成后更新预览
    updatePreview()
  }
}

function initQuillEditor(initialContent: string) {
  if (quillEditor.value) {
    quillInstance.value = new Quill(quillEditor.value, {
      theme: 'snow',
      modules: {
        toolbar: [
          ['bold', 'italic', 'underline', 'strike'],
          ['blockquote', 'code-block'],
          [{ 'header': 1 }, { 'header': 2 }],
          [{ 'list': 'ordered' }, { 'list': 'bullet' }],
          [{ 'script': 'sub' }, { 'script': 'super' }],
          [{ 'indent': '-1' }, { 'indent': '+1' }],
          [{ 'direction': 'rtl' }],
          [{ 'size': ['small', false, 'large', 'huge'] }],
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'font': [] }],
          [{ 'align': [] }],
          ['clean']
        ]
      }
    })
    quillInstance.value.root.innerHTML = initialContent
  }
}

async function save() {
  saving.value = true
  errorMsg.value = null
  okMsg.value = null
  try {
    // 如果是Word格式，从Quill编辑器获取内容
    let contentToSave = content.value
    if (contentType.value === 'word' && quillInstance.value) {
      contentToSave = quillInstance.value.root.innerHTML
    }
    
    const payload: DailyDocument = {
      docDate: date.value,
      title: title.value,
      content: contentToSave,
      contentType: contentType.value,
    }
    if (doc.value?.id) {
      const updated = await updateDailyDoc(doc.value.id, payload)
      doc.value = updated
    } else {
      const created = await createDailyDoc(payload)
      doc.value = created
    }
    okMsg.value = '已保存'
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    saving.value = false
  }
}

watch(
  () => props.date,
  (v) => {
    if (v) date.value = v
  },
)

watch(date, load)

watch(contentType, (newType) => {
  if (newType === 'word') {
    nextTick(() => {
      initQuillEditor(content.value)
    })
  }
})

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-end justify-between gap-2">
      <div>
        <div class="text-lg font-semibold">文档编辑</div>
        <div class="text-sm text-zinc-500">
          {{ date }} <span v-if="doc?.id" class="ml-2 text-xs text-zinc-400">#{{ doc.id }}</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <input v-model="date" type="date" class="rounded-xl border border-zinc-200 px-3 py-2 text-sm" />
        <button
          class="rounded-xl bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
          :disabled="saving"
          @click="save"
        >
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
      <section class="space-y-2">
        <input
          v-model="title"
          class="w-full rounded-xl border border-zinc-200 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500/20"
          placeholder="标题（可选）"
        />

        <div class="flex items-center justify-between">
          <div class="text-xs text-zinc-500">内容</div>
          <select v-model="contentType" class="rounded-xl border border-zinc-200 px-2 py-1 text-xs">
            <option value="markdown">Markdown</option>
            <option value="plain">纯文本</option>
            <option value="word">Word</option>
          </select>
        </div>

        <!-- Markdown 和纯文本编辑器 -->
        <textarea
          v-if="contentType !== 'word'"
          v-model="content"
          class="h-[300px] md:h-[520px] w-full resize-none rounded-2xl border border-zinc-200 px-3 py-2 text-sm font-mono outline-none focus:ring-2 focus:ring-indigo-500/20"
          placeholder="写点什么…"
        />

        <!-- Word 编辑器 -->
        <div
          v-else
          ref="quillEditor"
          class="h-[300px] md:h-[520px] w-full rounded-2xl border border-zinc-200 overflow-hidden"
        />
      </section>

      <section>
        <div class="text-xs text-zinc-500">预览</div>
        <article
          class="prose prose-zinc mt-2 max-w-none rounded-2xl border border-zinc-200 bg-zinc-50 p-4 h-[300px] md:h-[520px] overflow-auto"
          v-html="previewHtml"
        />
      </section>
    </div>

    <div class="text-xs text-zinc-500" v-if="loading">加载中...</div>
  </div>
</template>

