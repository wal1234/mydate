<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import dayjs from 'dayjs'
import { getDailyView } from '../lib/api'
import type { DailyViewDTO } from '../lib/types'

const loading = ref(false)
const errorMsg = ref<string | null>(null)
const today = dayjs().format('YYYY-MM-DD')
const daily = ref<DailyViewDTO | null>(null)

async function load() {
  loading.value = true
  errorMsg.value = null
  try {
    daily.value = await getDailyView(today)
  } catch (e) {
    console.error('加载今日数据失败:', e)
    errorMsg.value = '加载数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-end justify-between gap-2">
      <div>
        <div class="text-lg font-semibold">今日</div>
        <div class="text-sm text-zinc-500">{{ today }}</div>
      </div>
      <RouterLink
        class="rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm hover:bg-zinc-50"
        :to="`/docs/${today}`"
      >
        编辑今日文档
      </RouterLink>
    </div>

    <div v-if="errorMsg" class="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
      {{ errorMsg }}
    </div>

    <div class="grid gap-4 grid-cols-1 lg:grid-cols-2">
      <section class="rounded-2xl border border-zinc-200 bg-white p-4">
        <div class="text-sm font-semibold">今日日程</div>
        <div class="mt-3 space-y-2">
          <div
            v-for="e in daily?.events ?? []"
            :key="e.id ?? `${e.title}-${e.startTime}`"
            class="rounded-xl border border-zinc-100 bg-zinc-50 p-3"
          >
            <div class="text-sm font-medium">{{ e.title }}</div>
            <div class="mt-1 text-xs text-zinc-600">
              {{ e.startTime.replace('T', ' ') }} - {{ e.endTime.replace('T', ' ') }}
            </div>
          </div>
          <div v-if="(daily?.events?.length ?? 0) === 0" class="text-sm text-zinc-500">
            今日暂无日程
          </div>
        </div>
      </section>

      <section class="rounded-2xl border border-zinc-200 bg-white p-4">
        <div class="flex items-center justify-between">
          <div class="text-sm font-semibold">今日文档</div>
          <RouterLink class="text-sm text-indigo-600 hover:underline" :to="`/docs/${today}`">编辑</RouterLink>
        </div>
        <div class="mt-2 text-sm text-zinc-600">
          <div v-if="daily?.document">
            <div class="font-medium text-zinc-800">{{ daily.document.title || '未命名' }}</div>
            <div class="mt-2 line-clamp-6 whitespace-pre-wrap">
              {{ daily.document.content || '' }}
            </div>
          </div>
          <div v-else class="text-zinc-500">今日尚未创建文档</div>
        </div>
      </section>
    </div>

    <div class="text-xs text-zinc-500" v-if="loading">加载中...</div>
  </div>
</template>

