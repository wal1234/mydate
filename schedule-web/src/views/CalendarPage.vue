<template>
  <div class="space-y-6">
    <!-- 页面头部 -->
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-zinc-800 flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/30">
            <Calendar class="h-5 w-5 text-white" />
          </div>
          日程管理
        </h1>
        <p class="text-sm text-zinc-500 mt-1 ml-13">📅 选中日期：{{ ui.selectedDate }}</p>
      </div>
      
      <div class="flex items-center gap-3">
        <!-- 视图切换 -->
        <div class="inline-flex rounded-xl bg-zinc-100 p-1">
          <button
            v-for="view in views"
            :key="view.value"
            @click="calendarView = view.value"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
            :class="calendarView === view.value 
              ? 'bg-white text-indigo-600 shadow-sm' 
              : 'text-zinc-600 hover:text-zinc-900'"
          >
            <component :is="view.icon" class="h-4 w-4 inline mr-1.5" />
            {{ view.label }}
          </button>
        </div>
        
        <button
          class="btn-primary flex items-center gap-2 shadow-lg shadow-indigo-500/20"
          @click="showQuickAdd = true"
        >
          <Plus class="h-4 w-4" />
          新建日程
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl p-5 border border-blue-100/50">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-2xl font-bold text-blue-600">{{ todayStats.total }}</div>
            <div class="text-xs text-zinc-500 mt-1">今日日程</div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-blue-100/50 flex items-center justify-center">
            <Calendar class="h-5 w-5 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-gradient-to-br from-green-50 to-emerald-50 rounded-2xl p-5 border border-green-100/50">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-2xl font-bold text-green-600">{{ todayStats.completed }}</div>
            <div class="text-xs text-zinc-500 mt-1">已完成</div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-green-100/50 flex items-center justify-center">
            <CheckCircle class="h-5 w-5 text-green-600" />
          </div>
        </div>
      </div>
      <div class="bg-gradient-to-br from-amber-50 to-orange-50 rounded-2xl p-5 border border-amber-100/50">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-2xl font-bold text-amber-600">{{ todayStats.upcoming }}</div>
            <div class="text-xs text-zinc-500 mt-1">即将到来</div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-amber-100/50 flex items-center justify-center">
            <Clock class="h-5 w-5 text-amber-600" />
          </div>
        </div>
      </div>
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-5 border border-purple-100/50">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-2xl font-bold text-purple-600">{{ weekStats }}</div>
            <div class="text-xs text-zinc-500 mt-1">本周日程</div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-purple-100/50 flex items-center justify-center">
            <BarChart3 class="h-5 w-5 text-purple-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- 日历容器 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="animate-spin w-10 h-10 border-4 border-indigo-200 border-t-indigo-600 rounded-full"></div>
    </div>
    <div v-else-if="errorMsg" class="bg-red-50 border border-red-200 rounded-2xl p-6 text-center">
      <AlertCircle class="h-12 w-12 mx-auto mb-3 text-red-400" />
      <p class="text-red-600">{{ errorMsg }}</p>
    </div>
    <div v-else class="bg-white rounded-2xl border border-zinc-200/50 p-6 shadow-sm">
      <FullCalendar
        ref="calendarRef"
        :options="calendarOptions"
      />
    </div>

    <!-- 新建日程弹窗 -->
    <div v-if="showQuickAdd" class="modal-overlay" @click.self="showQuickAdd = false">
      <div class="modal-card max-w-xl">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/30">
              <Plus class="h-6 w-6 text-white" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-zinc-800">新建日程</h3>
              <p class="text-xs text-zinc-500">安排您的日程安排</p>
            </div>
          </div>
          <button @click="showQuickAdd = false" class="p-2 rounded-xl hover:bg-zinc-100 transition-colors">
            <X class="h-5 w-5 text-zinc-400" />
          </button>
        </div>

        <div class="space-y-5">
          <!-- 标题 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <FileText class="h-4 w-4 text-indigo-500" />
              日程标题
            </label>
            <input
              v-model="newEvent.title"
              type="text"
              class="form-input"
              placeholder="请输入日程标题"
            />
          </div>

          <!-- 描述 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <AlignLeft class="h-4 w-4 text-indigo-500" />
              日程描述
            </label>
            <textarea
              v-model="newEvent.description"
              rows="3"
              class="form-input resize-none"
              placeholder="添加日程描述（可选）"
            />
          </div>

          <!-- 时间选择 -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label flex items-center gap-2">
                <Clock class="h-4 w-4 text-indigo-500" />
                开始时间
              </label>
              <input
                v-model="newEvent.start"
                type="datetime-local"
                class="form-input"
              />
            </div>
            <div>
              <label class="form-label flex items-center gap-2">
                <Clock class="h-4 w-4 text-purple-500" />
                结束时间
              </label>
              <input
                v-model="newEvent.end"
                type="datetime-local"
                class="form-input"
              />
            </div>
          </div>

          <!-- 颜色选择 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <Palette class="h-4 w-4 text-indigo-500" />
              日程颜色
            </label>
            <div class="flex gap-2">
              <button
                v-for="color in colors"
                :key="color.value"
                @click="newEvent.color = color.value"
                class="w-8 h-8 rounded-full transition-transform hover:scale-110 ring-2 ring-offset-2"
                :class="newEvent.color === color.value ? 'ring-indigo-500' : 'ring-transparent'"
                :style="{ backgroundColor: color.value }"
              />
            </div>
          </div>

          <!-- 提醒设置 -->
          <div>
            <label class="form-label flex items-center gap-2">
              <Bell class="h-4 w-4 text-indigo-500" />
              提醒设置
            </label>
            <select v-model="newEvent.reminder" class="form-input">
              <option value="">不提醒</option>
              <option value="5">提前 5 分钟</option>
              <option value="15">提前 15 分钟</option>
              <option value="30">提前 30 分钟</option>
              <option value="60">提前 1 小时</option>
              <option value="1440">提前 1 天</option>
            </select>
          </div>

          <!-- 通知设置 -->
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" v-model="newEvent.notifyWechat" class="w-4 h-4 rounded text-indigo-600" />
              <span class="text-sm text-zinc-600">微信通知</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" v-model="newEvent.notifyQQ" class="w-4 h-4 rounded text-indigo-600" />
              <span class="text-sm text-zinc-600">QQ通知</span>
            </label>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-3 pt-2">
            <button @click="showQuickAdd = false" class="btn-secondary flex-1">
              取消
            </button>
            <button @click="handleQuickAdd" class="btn-primary flex-1 flex items-center justify-center gap-2">
              <Plus class="h-4 w-4" />
              创建日程
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑日程弹窗 -->
    <div v-if="showEditModal && editingEvent" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-card max-w-xl">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center shadow-lg">
              <Edit class="h-6 w-6 text-white" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-zinc-800">编辑日程</h3>
              <p class="text-xs text-zinc-500">修改日程信息</p>
            </div>
          </div>
          <button @click="showEditModal = false" class="p-2 rounded-xl hover:bg-zinc-100 transition-colors">
            <X class="h-5 w-5 text-zinc-400" />
          </button>
        </div>

        <div class="space-y-5">
          <div>
            <label class="form-label">日程标题</label>
            <input
              v-model="editingEvent.title"
              type="text"
              class="form-input"
            />
          </div>

          <div>
            <label class="form-label">日程描述</label>
            <textarea
              v-model="editingEvent.description"
              rows="3"
              class="form-input resize-none"
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label">开始时间</label>
              <input
                v-model="editingEvent.start"
                type="datetime-local"
                class="form-input"
              />
            </div>
            <div>
              <label class="form-label">结束时间</label>
              <input
                v-model="editingEvent.end"
                type="datetime-local"
                class="form-input"
              />
            </div>
          </div>

          <div>
            <label class="form-label">提醒设置</label>
            <select v-model="editingEvent.reminder" class="form-input">
              <option value="">不提醒</option>
              <option value="5">提前 5 分钟</option>
              <option value="15">提前 15 分钟</option>
              <option value="30">提前 30 分钟</option>
              <option value="60">提前 1 小时</option>
            </select>
          </div>

          <div class="flex gap-3 pt-2">
            <button @click="handleDeleteEvent" class="px-4 py-2 bg-red-100 text-red-600 rounded-xl hover:bg-red-200 flex items-center gap-2">
              <Trash2 class="h-4 w-4" />
              删除
            </button>
            <div class="flex-1" />
            <button @click="showEditModal = false" class="btn-secondary">
              取消
            </button>
            <button @click="handleUpdateEvent" class="btn-primary flex items-center gap-2">
              <Check class="h-4 w-4" />
              保存
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
import { Plus, Calendar, Clock, List, X, Palette, Bell, Trash2, FileText, AlignLeft, Check, Edit, CheckCircle, AlertCircle, BarChart3 } from 'lucide-vue-next'
import { createSchedule, getDailyView, getSchedules, updateSchedule } from '../lib/api'
import { useUiStore } from '../stores/ui'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin, { type DateClickArg } from '@fullcalendar/interaction'
import zhCnLocale from '@fullcalendar/core/locales/zh-cn'

interface ScheduleEvent {
  id?: string | number
  title: string
  description?: string
  start: string
  end?: string
  color?: string
  reminder?: string
  notifyWechat?: boolean
  notifyQQ?: boolean
}

const ui = useUiStore()
const calendarRef = ref<InstanceType<typeof FullCalendar> | null>(null)
const calendarView = ref('dayGridMonth')
const showQuickAdd = ref(false)
const showEditModal = ref(false)
const editingEvent = ref<ScheduleEvent | null>(null)
const loading = ref(false)
const errorMsg = ref<string | null>(null)
const events = ref<any[]>([])
const daily = ref<any>(null)

const newEvent = ref<ScheduleEvent>({
  title: '',
  description: '',
  start: '',
  end: '',
  color: '#3b82f6',
  reminder: '15',
  notifyWechat: false,
  notifyQQ: false
})

const views = [
  { label: '月', value: 'dayGridMonth', icon: Calendar },
  { label: '周', value: 'timeGridWeek', icon: Clock },
  { label: '日', value: 'timeGridDay', icon: List }
]

const colors = [
  { value: '#3b82f6', label: '蓝色' },
  { value: '#10b981', label: '绿色' },
  { value: '#f59e0b', label: '橙色' },
  { value: '#ef4444', label: '红色' },
  { value: '#8b5cf6', label: '紫色' },
  { value: '#ec4899', label: '粉色' }
]

const todayStats = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  const todayEvents = events.value.filter(e => dayjs(e.start).format('YYYY-MM-DD') === today)
  return {
    total: todayEvents.length,
    completed: 0,
    upcoming: todayEvents.filter(e => dayjs(e.start).isAfter(dayjs())).length
  }
})

const weekStats = computed(() => {
  const weekStart = dayjs().startOf('week')
  const weekEnd = dayjs().endOf('week')
  return events.value.filter(e => {
    const eventDate = dayjs(e.start)
    return eventDate.isAfter(weekStart) && eventDate.isBefore(weekEnd)
  }).length
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: calendarView.value,
  locale: zhCnLocale,
  headerToolbar: false,
  height: 'auto',
  events: events.value,
  editable: true,
  selectable: true,
  selectMirror: true,
  dayMaxEvents: true,
  weekends: true,
  dateClick: handleDateClick,
  eventClick: handleEventClick,
  eventDrop: handleEventDrop,
  eventResize: handleEventResize,
  datesSet: handleDatesSet
} as any))

onMounted(async () => {
  await loadEvents()
  await loadDaily()
})

async function loadEvents() {
  loading.value = true
  errorMsg.value = null
  try {
    // 加载3个月的日程用于日历展示
    const monthStart = dayjs().startOf('month').subtract(1, 'month').format('YYYY-MM-DD') + 'T00:00:00'
    const monthEnd = dayjs().endOf('month').add(2, 'month').format('YYYY-MM-DD') + 'T23:59:59'
    
    const rawEvents = await getSchedules({ start: monthStart, end: monthEnd, page: 1, size: 500 })
    
    // 简化处理：直接使用返回的数组
    let eventList = []
    if (Array.isArray(rawEvents)) {
      eventList = rawEvents
    } else if (rawEvents && 'list' in rawEvents) {
      eventList = (rawEvents as any).list || []
    } else if (rawEvents && Array.isArray((rawEvents as any).records)) {
      eventList = (rawEvents as any).records || []
    } else {
      console.warn('Unexpected data format:', rawEvents)
      eventList = []
    }
    
    events.value = eventList.map((e: any) => ({
      id: String(e.id),
      title: e.title,
      start: e.startTime,
      end: e.endTime,
      color: e.color,
      extendedProps: {
        description: e.description,
        reminder: e.reminderMinutes?.toString() || ''
      }
    }))
    
    console.log('日程加载成功:', events.value.length, '条')
  } catch (e) {
    console.error('加载日程失败:', e)
    errorMsg.value = '加载日程失败，请刷新重试'
  } finally {
    loading.value = false
  }
}

async function loadDaily() {
  try {
    const result = await getDailyView(ui.selectedDate)
    daily.value = result
  } catch (e) {
    console.error('加载每日视图失败:', e)
  }
}

function handleDateClick(info: DateClickArg) {
  ui.selectedDate = dayjs(info.dateStr).format('YYYY-MM-DD')
  newEvent.value.start = dayjs(info.dateStr).format('YYYY-MM-DDTHH:mm')
  newEvent.value.end = dayjs(info.dateStr).add(1, 'hour').format('YYYY-MM-DDTHH:mm')
  showQuickAdd.value = true
}

function handleEventClick(info: any) {
  const event = info.event
  editingEvent.value = {
    id: event.id ? String(event.id) : undefined,
    title: event.title,
    description: event.extendedProps?.description || '',
    start: event.start ? dayjs(event.start).format('YYYY-MM-DDTHH:mm') : '',
    end: event.end ? dayjs(event.end).format('YYYY-MM-DDTHH:mm') : '',
    color: event.backgroundColor || '#3b82f6',
    reminder: event.extendedProps?.reminder || ''
  }
  showEditModal.value = true
}

async function handleEventDrop(info: any) {
  try {
    const event = info.event
    await updateSchedule(
      parseInt(event.id as string),
      {
        title: event.title,
        startTime: dayjs(event.start).format('YYYY-MM-DD HH:mm:ss'),
        endTime: event.end ? dayjs(event.end).format('YYYY-MM-DD HH:mm:ss') : undefined,
        color: event.backgroundColor
      } as any
    )
    await loadEvents()
  } catch (e) {
    errorMsg.value = (e as Error).message
    info.revert()
  }
}

async function handleEventResize(info: any) {
  try {
    const event = info.event
    await updateSchedule(
      parseInt(event.id as string),
      {
        title: event.title,
        startTime: dayjs(event.start).format('YYYY-MM-DD HH:mm:ss'),
        endTime: event.end ? dayjs(event.end).format('YYYY-MM-DD HH:mm:ss') : undefined,
        color: event.backgroundColor
      } as any
    )
    await loadEvents()
  } catch (e) {
    errorMsg.value = (e as Error).message
    info.revert()
  }
}

function handleDatesSet(info: any) {
  ui.selectedDate = dayjs(info.start).format('YYYY-MM-DD')
  loadDaily()
}

async function handleQuickAdd() {
  if (!newEvent.value.title || !newEvent.value.start) {
    errorMsg.value = '请填写标题和开始时间'
    return
  }
  try {
    await createSchedule({
      title: newEvent.value.title,
      description: newEvent.value.description || undefined,
      startTime: newEvent.value.start ? dayjs(newEvent.value.start).format('YYYY-MM-DD HH:mm:ss') : '',
      endTime: (newEvent.value.end || newEvent.value.start) ? dayjs(newEvent.value.end || newEvent.value.start).format('YYYY-MM-DD HH:mm:ss') : '',
      color: newEvent.value.color,
      reminderMinutes: newEvent.value.reminder ? parseInt(newEvent.value.reminder) : undefined,
      notifyWechat: Boolean(newEvent.value.notifyWechat),
      notifyQQ: Boolean(newEvent.value.notifyQQ)
    } as any)
    showQuickAdd.value = false
    newEvent.value = {
      title: '',
      description: '',
      start: '',
      end: '',
      color: '#3b82f6',
      reminder: '15',
      notifyWechat: false,
      notifyQQ: false
    }
    await loadEvents()
    errorMsg.value = null
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}

async function handleUpdateEvent() {
  if (!editingEvent.value?.id) return
  try {
    await updateSchedule(
      Number(editingEvent.value.id),
      {
        title: editingEvent.value.title,
        description: editingEvent.value.description || undefined,
        startTime: editingEvent.value.start ? dayjs(editingEvent.value.start).format('YYYY-MM-DD HH:mm:ss') : '',
        endTime: editingEvent.value.end ? dayjs(editingEvent.value.end).format('YYYY-MM-DD HH:mm:ss') : '',
        color: editingEvent.value.color,
        reminderMinutes: editingEvent.value.reminder ? parseInt(editingEvent.value.reminder) : undefined
      } as any
    )
    showEditModal.value = false
    editingEvent.value = null
    await loadEvents()
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}

async function handleDeleteEvent() {
  if (!editingEvent.value?.id || !confirm('确定要删除这个日程吗？')) return
  try {
    await updateSchedule(Number(editingEvent.value.id), { deleted: true } as any)
    showEditModal.value = false
    editingEvent.value = null
    await loadEvents()
  } catch (e) {
    errorMsg.value = (e as Error).message
  }
}

watch(calendarView, (newView) => {
  if (calendarRef.value) {
    const api = calendarRef.value.getApi()
    api.changeView(newView)
  }
})
</script>
