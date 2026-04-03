<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { CalendarDays, NotebookPen, Sun, Menu, X, StickyNote, Settings, Bot, FileText, Layers, Trash2, Sparkles } from 'lucide-vue-next'

const route = useRoute()

// 导航分组
const navGroups = computed(() => [
  {
    title: '核心功能',
    items: [
      { to: '/', label: '日历', icon: CalendarDays, description: '日程管理' },
      { to: '/today', label: '今日', icon: Sun, description: '今日任务' },
      { to: '/docs', label: '文档', icon: NotebookPen, description: '每日记录' },
      { to: '/notes', label: '笔记', icon: StickyNote, description: '笔记管理' },
    ]
  },
  {
    title: 'AI 助手',
    items: [
      { to: '/chat', label: 'AI 对话', icon: Bot, description: '智能助手' },
      { to: '/prompts', label: '提示词库', icon: FileText, description: '模板管理' },
      { to: '/skills', label: 'AI Skills', icon: Layers, description: '技能配置' },
    ]
  },
  {
    title: '系统',
    items: [
      { to: '/trash', label: '回收站', icon: Trash2, description: '已删除项目' },
      { to: '/settings', label: '设置', icon: Settings, description: '偏好设置' },
    ]
  }
])

const showMobileMenu = ref(false)
</script>

<template>
  <div class="min-h-full bg-gradient-to-br from-slate-50 via-indigo-50/20 to-purple-50/20 text-zinc-900 pb-20 md:pb-6">
    <!-- 桌面端侧边栏 -->
    <aside
      class="hidden md:block w-72 shrink-0 bg-white/90 backdrop-blur-xl rounded-2xl border border-zinc-200/50 shadow-xl shadow-indigo-500/5 p-5 fixed left-4 top-4 bottom-4 overflow-hidden"
    >
      <!-- Logo 区域 -->
      <div class="flex items-center gap-3 px-3 py-3 mb-6">
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg shadow-indigo-500/30">
          <CalendarDays class="h-6 w-6 text-white" />
        </div>
        <div>
          <div class="font-bold text-lg text-zinc-800">智能日程</div>
          <div class="text-xs text-zinc-500 font-medium">Schedule AI</div>
        </div>
      </div>

      <!-- 导航分组 -->
      <nav class="space-y-6 overflow-y-auto max-h-[calc(100vh-16rem)]">
        <div v-for="group in navGroups" :key="group.title" class="space-y-1">
          <!-- 分组标题 -->
          <div class="px-3 py-2 text-xs font-semibold text-zinc-400 uppercase tracking-wider">
            {{ group.title }}
          </div>
          
          <!-- 分组项 -->
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="group flex items-center gap-3 rounded-xl px-4 py-3 text-sm transition-all duration-200 relative overflow-hidden"
            :class="route.path === item.to 
              ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg shadow-indigo-500/25' 
              : 'text-zinc-600 hover:bg-gradient-to-r hover:from-indigo-50 hover:to-purple-50 hover:text-indigo-700'"
            @click="showMobileMenu = false"
          >
            <!-- 选中指示器 -->
            <div 
              v-if="route.path === item.to" 
              class="absolute left-0 top-0 bottom-0 w-1 bg-white/50 rounded-r-full"
            />
            
            <component 
              :is="item.icon" 
              class="h-5 w-5 transition-transform group-hover:scale-110" 
            />
            <div class="flex-1">
              <div class="font-medium">{{ item.label }}</div>
              <div 
                v-if="route.path !== item.to" 
                class="text-xs opacity-0 group-hover:opacity-100 transition-opacity"
              >
                {{ item.description }}
              </div>
            </div>
            
            <!-- 快捷操作指示 -->
            <Sparkles 
              v-if="item.to === '/chat'" 
              class="h-4 w-4 opacity-50" 
            />
          </RouterLink>
        </div>
      </nav>

      <!-- 底部信息 -->
      <div class="absolute bottom-5 left-5 right-5 rounded-2xl bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50 p-4 border border-indigo-100/50">
        <div class="flex items-center gap-2 mb-2">
          <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
          <span class="text-xs font-medium text-indigo-600">系统运行中</span>
        </div>
        <div class="text-xs text-zinc-500 space-y-1">
          <div>版本：<span class="font-mono text-zinc-600">v2.0.0</span></div>
          <div>端口：<span class="font-mono text-zinc-600">8081</span></div>
        </div>
      </div>
    </aside>

    <!-- 移动端顶部导航 -->
    <header class="md:hidden fixed top-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-xl border-b border-zinc-200/50 px-4 py-3">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg">
            <CalendarDays class="h-5 w-5 text-white" />
          </div>
          <span class="font-bold text-zinc-800">智能日程</span>
        </div>
        <button 
          class="p-2 rounded-xl hover:bg-zinc-100 transition-colors"
          @click="showMobileMenu = !showMobileMenu"
        >
          <Menu v-if="!showMobileMenu" class="h-5 w-5 text-zinc-600" />
          <X v-else class="h-5 w-5 text-zinc-600" />
        </button>
      </div>
    </header>

    <!-- 移动端菜单下拉 -->
    <div 
      v-if="showMobileMenu"
      class="md:hidden fixed top-16 left-0 right-0 z-40 bg-white/98 backdrop-blur-xl border-b border-zinc-200 shadow-2xl"
      style="animation: slideDown 0.2s ease"
    >
      <nav class="p-4 space-y-6 max-h-[70vh] overflow-y-auto">
        <div v-for="group in navGroups" :key="group.title" class="space-y-1">
          <div class="px-3 py-2 text-xs font-semibold text-zinc-400 uppercase tracking-wider">
            {{ group.title }}
          </div>
          
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="group flex items-center gap-3 rounded-xl px-4 py-3 text-sm transition-all"
            :class="route.path === item.to 
              ? 'bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-lg' 
              : 'text-zinc-600 hover:bg-indigo-50'"
            @click="showMobileMenu = false"
          >
            <component :is="item.icon" class="h-5 w-5" />
            <div class="flex-1">
              <div class="font-medium">{{ item.label }}</div>
              <div class="text-xs opacity-70">{{ item.description }}</div>
            </div>
          </RouterLink>
        </div>
      </nav>
    </div>

    <!-- 主内容区 -->
    <main class="md:ml-76 p-4 pt-20 md:pt-6">
      <div
        class="rounded-2xl bg-white/90 backdrop-blur-sm border border-zinc-200/50 shadow-xl shadow-indigo-500/5 p-5 md:p-7 min-h-[calc(100vh-10rem)] md:min-h-[calc(100vh-4rem)]"
        style="animation: fadeIn 0.3s ease"
      >
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </div>
    </main>

    <!-- 移动端底部导航栏 - 仅显示核心功能 -->
    <nav class="md:hidden fixed bottom-0 left-0 right-0 bg-white/95 backdrop-blur-xl border-t border-zinc-200/50 z-40">
      <div class="flex justify-around py-1.5 px-1">
        <RouterLink
          v-for="item in navGroups[0].items.slice(0, 4)"
          :key="item.to"
          :to="item.to"
          class="flex flex-col items-center gap-0.5 px-2 py-1.5 rounded-xl transition-all duration-200"
          :class="route.path === item.to 
            ? 'text-indigo-600' 
            : 'text-zinc-500'"
          @click="showMobileMenu = false"
        >
          <div class="relative">
            <component :is="item.icon" class="h-5 w-5" />
            <div 
              v-if="route.path === item.to" 
              class="absolute -top-1 -right-1 w-1.5 h-1.5 rounded-full bg-indigo-600 animate-pulse"
            />
          </div>
          <span class="text-[10px] font-medium">{{ item.label }}</span>
        </RouterLink>
      </div>
    </nav>
  </div>
</template>

<style scoped>
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
