/**
 * 与后端 book-common 的 Result / PageResult 对齐的前端类型定义。
 */
export type Result<T> = {
  code: number
  message: string
  data: T
}

export type PageResult<T> = {
  list: T[]
  total: number
  page: number
  size: number
}

export type ScheduleEvent = {
  id?: number
  userId?: number | null
  title: string
  description?: string | null
  startTime: string
  endTime: string
  isAllDay?: number | null
  color?: string | null
  repeatRule?: string | null
  repeatUntil?: string | null
  reminderMinutes?: number | null
  notifyWechat?: boolean | null
  notifyQQ?: boolean | null
}

export type DailyDocument = {
  id?: number
  userId?: number | null
  docDate: string
  title?: string | null
  content?: string | null
  contentType?: 'plain' | 'markdown' | string | null
}

export type DailyViewDTO = {
  events: ScheduleEvent[]
  document: DailyDocument | null
}

export type Note = {
  id?: number
  userId?: number | null
  categoryId?: number | null
  title?: string | null
  content?: string | null
  contentType?: 'quick' | 'rich' | string | null
  color?: string | null
  isPinned?: number | null
  tags?: string | null
  createdAt?: string
  updatedAt?: string
}

export type NoteCategory = {
  id?: number
  userId?: number | null
  name?: string | null
  parentId?: number | null
  sortOrder?: number | null
}

export type UserNotificationConfig = {
  id?: number
  userId?: number | null
  wechatServerChanKey?: string | null
  qqNumber?: string | null
  qmsgKey?: string | null
  wechatEnabled?: boolean | null
  qqEnabled?: boolean | null
}

export type PromptTemplate = {
  id?: number
  userId?: number
  templateName: string
  description?: string
  templateContent: string
  variables?: string
  createdAt?: string
  updatedAt?: string
}

export type PromptVersion = {
  id?: number
  templateId?: number
  versionNumber?: number
  content?: string
  releaseNotes?: string
  isActive?: number
  createdAt?: string
  updatedAt?: string
}

export type ConversationContext = {
  id?: number
  userId?: number
  sessionId?: number
  maxTokens?: number
  contextWindow?: number
  preserveSystemMessages?: number
  summarizeEnabled?: number
  summarizeThreshold?: number
  currentSummary?: string
  createdAt?: string
  updatedAt?: string
}

export type ContextAnalysis = {
  contextWindow?: number
  maxTokens?: number
  availableTokens?: number
  preserveSystem?: number
  summarizeEnabled?: number
  summarizeThreshold?: number
}

export type LlmApiConfig = {
  id?: number
  userId?: number
  configName: string
  apiUrl: string
  apiKey: string
  modelName: string
  providerType?: string
  streamEnabled?: number
  temperature?: number
  maxTokens?: number
  headers?: string
  isActive?: number
  createdAt?: string
  updatedAt?: string
}

export type Skill = {
  id?: number
  userId?: number
  categoryId?: number
  skillName: string
  skillCode?: string
  description?: string
  skillType?: string
  icon?: string
  color?: string
  status?: number
  isDefault?: number
  priority?: number
  parameters?: string
  promptTemplate?: string
  maxTokens?: number
  temperature?: number
  modelName?: string
  createdAt?: string
  updatedAt?: string
}

export type SkillCategory = {
  id?: number
  userId?: number
  categoryName: string
  categoryCode?: string
  description?: string
  icon?: string
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

export type TrashItem = {
  id?: number
  userId?: number
  itemType?: string
  originalId?: number
  itemTitle?: string
  itemContent?: string
  originalData?: string
  deletedAt?: string
  expireAt?: string
  status?: number
  createdAt?: string
  updatedAt?: string
}

export type SkillVersion = {
  id?: number
  skillId?: number
  versionNumber?: number
  versionName?: string
  promptTemplate?: string
  parameters?: string
  releaseNotes?: string
  isActive?: number
  usageCount?: number
  createdAt?: string
  updatedAt?: string
}

