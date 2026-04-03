import { defineStore } from 'pinia'
import dayjs from 'dayjs'

/**
 * UI 状态：
 * - 当前选中的日期（用于日历页与文档页联动）
 */
export const useUiStore = defineStore('ui', {
  state: () => ({
    selectedDate: dayjs().format('YYYY-MM-DD'),
  }),
  actions: {
    setSelectedDate(date: string) {
      this.selectedDate = date
    },
  },
})

