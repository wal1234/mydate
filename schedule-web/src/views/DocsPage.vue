<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import dayjs from 'dayjs'
import { listDailyDocs } from '../lib/api'
import type { DailyDocument, PageResult } from '../lib/types'

const loading = ref(false)
const errorMsg = ref<string | null>(null)

const from = ref(dayjs().startOf('month').format('YYYY-MM-DD'))
const to = ref(dayjs().endOf('month').format('YYYY-MM-DD'))
const page = ref(1)
const size = ref(10)

const list = ref<DailyDocument[]>([])
const total = ref(0)

const pages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function load() {
  loading.value = true
  errorMsg.value = null
  try {
    const data = await listDailyDocs({ from: from.value, to: to.value, page: page.value, size: size.value })
    if (Array.isArray(data)) {
      list.value = data
      total.value = data.length
    } else {
      const pr = data as PageResult<DailyDocument>
      list.value = pr.list
      total.value = pr.total
    }
  } catch (e) {
    errorMsg.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-end justify-between gap-3">
      <div>
        <div class="text-lg font-semibold">文档</div>
        <div class="text-sm text-zinc-500">按日期区间浏览并进入编辑</div>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <label class="text-xs text-zinc-600">从</label>
        <input v-model="from" type="date" class="rounded-xl border border-zinc-200 px-3 py-2 text-sm" />
        <label class="text-xs text-zinc-600">到</label>
        <input v-model="to" type="date" class="rounded-xl border border-zinc-200 px-3 py-2 text-sm" />
        <button class="rounded-xl bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-700" @click="load">
          查询
        </button>
      </div>
    </div>

    <div v-if="errorMsg" class="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
      {{ errorMsg }}
    </div>

    <div class="overflow-hidden rounded-2xl border border-zinc-200">
      <div class="hidden md:grid grid-cols-12 bg-zinc-50 px-4 py-2 text-xs font-medium text-zinc-600">
        <div class="col-span-3">日期</div>
        <div class="col-span-9">标题</div>
      </div>
      <div v-if="list.length === 0" class="px-4 py-10 text-center text-sm text-zinc-500">
        暂无文档
      </div>
      <RouterLink
        v-for="d in list"
        :key="d.id ?? d.docDate"
        class="grid grid-cols-1 md:grid-cols-12 border-t border-zinc-100 px-4 py-3 text-sm hover:bg-zinc-50 md:gap-0 gap-2"
        :to="`/docs/${d.docDate}`"
      >
        <div class="md:col-span-3 font-mono text-xs text-zinc-600">{{ d.docDate }}</div>
        <div class="md:col-span-9 font-medium text-zinc-800">{{ d.title || '未命名' }}</div>
      </RouterLink>
    </div>

    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-sm text-zinc-600">
      <div>共 {{ total }} 条</div>
      <div class="flex items-center gap-2">
        <button
          class="rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm hover:bg-zinc-50 disabled:opacity-50"
          :disabled="page <= 1"
          @click="page--; load()"
        >
          上一页
        </button>
        <div class="text-xs">第 {{ page }} / {{ pages }} 页</div>
        <button
          class="rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm hover:bg-zinc-50 disabled:opacity-50"
          :disabled="page >= pages"
          @click="page++; load()"
        >
          下一页
        </button>
      </div>
    </div>

    <div class="text-xs text-zinc-500" v-if="loading">加载中...</div>
  </div>
</template>

