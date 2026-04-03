import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite 配置：
 * - Vue 3 + TS
 * - 开发环境将 /api 代理到后端 schedule-service（默认 8081）
 * - 开发环境将 /uploads 代理到后端静态文件目录
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        rewrite: (path) => path,
      },
    },
  },
})
