import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

import './style.css'

/**
 * 应用入口：
 * - Vue3 + TS
 * - Pinia 状态管理
 * - Vue Router 路由
 * - Element Plus UI 组件库
 */
createApp(App).use(createPinia()).use(router).use(ElementPlus).mount('#app')
