import axios from 'axios'
import type { ContextAnalysis, ConversationContext, DailyDocument, DailyViewDTO, Note, NoteCategory, PageResult, PromptTemplate, PromptVersion, Result, ScheduleEvent, Skill, SkillCategory, SkillVersion, TrashItem, UserNotificationConfig } from './types'

/**
 * API 客户端：
 * - 默认使用 Vite dev server 代理（/api → 8081）
 * - 可通过 VITE_API_BASE 覆盖（例如部署到不同域名）
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '',
  timeout: 15000,
})

function unwrap<T>(res: Result<T>): T {
  if (!res) throw new Error('Empty response')
  if (res.code !== 0) {
    const msg = res.message || `请求失败(code=${res.code})`
    throw new Error(msg)
  }
  return res.data
}

export async function getSchedules(params: {
  start: string
  end: string
  view?: string
  page?: number
  size?: number
}): Promise<ScheduleEvent[] | PageResult<ScheduleEvent>> {
  const { data } = await api.get<Result<ScheduleEvent[] | PageResult<ScheduleEvent>>>('/api/schedules', { params })
  return unwrap(data)
}

export async function createSchedule(payload: ScheduleEvent): Promise<ScheduleEvent> {
  const { data } = await api.post<Result<ScheduleEvent>>('/api/schedules', payload)
  return unwrap(data)
}

export async function updateSchedule(id: number, payload: Partial<ScheduleEvent>): Promise<ScheduleEvent> {
  const { data } = await api.put<Result<ScheduleEvent>>(`/api/schedules/${id}`, payload)
  return unwrap(data)
}

export async function deleteSchedule(id: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/schedules/${id}`)
  unwrap(data)
}

export async function getDailyView(date: string): Promise<DailyViewDTO> {
  const { data } = await api.get<Result<DailyViewDTO>>('/api/daily-view', { params: { date } })
  return unwrap(data)
}

export async function getDailyDocByDate(date: string): Promise<DailyDocument> {
  const { data } = await api.get<Result<DailyDocument>>(`/api/daily-docs/${date}`)
  return unwrap(data)
}

export async function createDailyDoc(payload: DailyDocument): Promise<DailyDocument> {
  const { data } = await api.post<Result<DailyDocument>>('/api/daily-docs', payload)
  return unwrap(data)
}

export async function updateDailyDoc(id: number, payload: Partial<DailyDocument>): Promise<DailyDocument> {
  const { data } = await api.put<Result<DailyDocument>>(`/api/daily-docs/${id}`, payload)
  return unwrap(data)
}

export async function listDailyDocs(params: { from?: string; to?: string; date?: string; page?: number; size?: number }) {
  const { data } = await api.get<Result<DailyDocument[] | PageResult<DailyDocument>>>('/api/daily-docs', { params })
  return unwrap(data)
}

export async function deleteDailyDoc(id: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/daily-docs/${id}`)
  unwrap(data)
}

export async function getNotes(params: {
  contentType?: string
  categoryId?: number
  keyword?: string
  page?: number
  size?: number
}): Promise<PageResult<Note>> {
  const { data } = await api.get<Result<PageResult<Note>>>('/api/notes', { params })
  return unwrap(data)
}

export async function getNoteById(id: number): Promise<Note> {
  const { data } = await api.get<Result<Note>>(`/api/notes/${id}`)
  return unwrap(data)
}

export async function createNote(payload: Partial<Note>): Promise<Note> {
  const { data } = await api.post<Result<Note>>('/api/notes', payload)
  return unwrap(data)
}

export async function updateNote(id: number, payload: Partial<Note>): Promise<Note> {
  const { data } = await api.put<Result<Note>>(`/api/notes/${id}`, payload)
  return unwrap(data)
}

export async function deleteNote(id: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/notes/${id}`)
  unwrap(data)
}

export async function toggleNotePin(id: number): Promise<Note> {
  const { data } = await api.post<Result<Note>>(`/api/notes/${id}/pin`, {})
  return unwrap(data)
}

export async function getNoteCategories(): Promise<NoteCategory[]> {
  const { data } = await api.get<Result<NoteCategory[]>>('/api/note-categories')
  return unwrap(data)
}

export async function createNoteCategory(payload: Partial<NoteCategory>): Promise<NoteCategory> {
  const { data } = await api.post<Result<NoteCategory>>('/api/note-categories', payload)
  return unwrap(data)
}

export async function updateNoteCategory(id: number, payload: Partial<NoteCategory>): Promise<NoteCategory> {
  const { data } = await api.put<Result<NoteCategory>>(`/api/note-categories/${id}`, payload)
  return unwrap(data)
}

export async function deleteNoteCategory(id: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/note-categories/${id}`)
  unwrap(data)
}

export async function uploadImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await api.post<Result<string>>('/api/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return unwrap(data)
}

export async function uploadImages(files: File[]): Promise<string[]> {
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  const { data } = await api.post<Result<string[]>>('/api/upload/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return unwrap(data)
}

export async function getNotificationConfig(userId?: number): Promise<UserNotificationConfig> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<UserNotificationConfig>>('/api/notification-config', { params })
  return unwrap(data)
}

export async function saveNotificationConfig(config: Partial<UserNotificationConfig>): Promise<UserNotificationConfig> {
  const { data } = await api.post<Result<UserNotificationConfig>>('/api/notification-config', config)
  return unwrap(data)
}

export interface LlmApiConfig {
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

export interface ChatSession {
  id: number
  userId?: number
  sessionTitle: string
  createdAt: string
  updatedAt?: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
}

export async function getLlmConfigs(userId?: number): Promise<LlmApiConfig[]> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<LlmApiConfig[]>>('/api/llm-config', { params })
  return unwrap(data)
}

export async function getActiveLlmConfig(userId?: number): Promise<LlmApiConfig | null> {
  try {
    const params = userId ? { userId } : {}
    const { data } = await api.get<Result<LlmApiConfig>>('/api/llm-config/active', { params })
    return unwrap(data)
  } catch {
    return null
  }
}

export async function saveLlmConfig(config: Partial<LlmApiConfig>): Promise<LlmApiConfig> {
  const { data } = await api.post<Result<LlmApiConfig>>('/api/llm-config', config)
  return unwrap(data)
}

export async function activateLlmConfig(configId: number, userId?: number): Promise<void> {
  const params = userId ? { userId } : {}
  const { data } = await api.post<Result<null>>(`/api/llm-config/activate/${configId}`, null, { params })
  unwrap(data)
}

export async function deleteLlmConfig(configId: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/llm-config/${configId}`)
  unwrap(data)
}

export async function getChatSessions(userId?: number): Promise<ChatSession[]> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<ChatSession[]>>('/api/chat/sessions', { params })
  return unwrap(data)
}

export async function createChatSession(userId?: number, title?: string): Promise<ChatSession> {
  const { data } = await api.post<Result<ChatSession>>('/api/chat/sessions', { userId, title })
  return unwrap(data)
}

export async function deleteChatSession(sessionId: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/chat/sessions/${sessionId}`)
  unwrap(data)
}

export async function getChatMessages(sessionId: number): Promise<ChatMessage[]> {
  const { data } = await api.get<Result<ChatMessage[]>>(`/api/chat/messages/${sessionId}`)
  return unwrap(data)
}

export async function sendChatMessage(sessionId: number, message: string, userId?: number): Promise<{ reply: string; sessionId: number }> {
  const { data } = await api.post<Result<{ reply: string; sessionId: number }>>('/api/chat/send', {
    sessionId,
    message,
    userId
  })
  return unwrap(data)
}

export async function getPromptTemplates(userId?: number): Promise<PromptTemplate[]> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<PromptTemplate[]>>('/api/prompt-templates', { params })
  return unwrap(data)
}

export async function getPromptTemplate(templateId: number): Promise<PromptTemplate> {
  const { data } = await api.get<Result<PromptTemplate>>(`/api/prompt-templates/${templateId}`)
  return unwrap(data)
}

export async function createPromptTemplate(payload: {
  userId?: number
  name: string
  description?: string
  content: string
}): Promise<PromptTemplate> {
  const { data } = await api.post<Result<PromptTemplate>>('/api/prompt-templates', payload)
  return unwrap(data)
}

export async function updatePromptTemplate(templateId: number, payload: {
  name?: string
  description?: string
  content?: string
}): Promise<PromptTemplate> {
  const { data } = await api.put<Result<PromptTemplate>>(`/api/prompt-templates/${templateId}`, payload)
  return unwrap(data)
}

export async function deletePromptTemplate(templateId: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/prompt-templates/${templateId}`)
  unwrap(data)
}

export async function getPromptVersions(templateId: number): Promise<PromptVersion[]> {
  const { data } = await api.get<Result<PromptVersion[]>>(`/api/prompt-templates/${templateId}/versions`)
  return unwrap(data)
}

export async function getActivePromptVersion(templateId: number): Promise<PromptVersion> {
  const { data } = await api.get<Result<PromptVersion>>(`/api/prompt-templates/${templateId}/versions/active`)
  return unwrap(data)
}

export async function createPromptVersion(templateId: number, payload: {
  content?: string
  releaseNotes?: string
}): Promise<PromptVersion> {
  const { data } = await api.post<Result<PromptVersion>>(`/api/prompt-templates/${templateId}/versions`, payload)
  return unwrap(data)
}

export async function activatePromptVersion(versionId: number): Promise<void> {
  const { data } = await api.post<Result<null>>(`/api/prompt-templates/versions/${versionId}/activate`)
  unwrap(data)
}

export async function rollbackPromptVersion(versionId: number): Promise<void> {
  const { data } = await api.post<Result<null>>(`/api/prompt-templates/versions/${versionId}/rollback`)
  unwrap(data)
}

export async function renderPromptTemplate(payload: {
  template: string
  variables: Record<string, any>
}): Promise<string> {
  const { data } = await api.post<Result<string>>('/api/prompt-templates/render', payload)
  return unwrap(data)
}

export async function getContextConfig(userId: number, sessionId: number): Promise<ConversationContext> {
  const params = { userId, sessionId }
  const { data } = await api.get<Result<ConversationContext>>('/api/context/config', { params })
  return unwrap(data)
}

export async function updateContextConfig(userId: number, sessionId: number, config: Partial<ConversationContext>): Promise<ConversationContext> {
  const params = { userId, sessionId }
  const { data } = await api.put<Result<ConversationContext>>('/api/context/config', config, { params })
  return unwrap(data)
}

export async function analyzeContext(userId: number, sessionId: number, modelName: string): Promise<ContextAnalysis> {
  const params = { userId, sessionId, modelName }
  const { data } = await api.post<Result<ContextAnalysis>>('/api/context/analyze', null, { params })
  return unwrap(data)
}

export async function countTokens(text: string): Promise<{ characterCount: number; estimatedTokens: number }> {
  const { data } = await api.post<Result<{ characterCount: number; estimatedTokens: number }>>('/api/context/tokens/count', { text })
  return unwrap(data)
}

export async function getSupportedModels(): Promise<Record<string, number>> {
  const { data } = await api.get<Result<Record<string, number>>>('/api/context/models')
  return unwrap(data)
}

export async function getSkills(userId?: number, categoryId?: number): Promise<Skill[]> {
  const params: any = {}
  if (userId) params.userId = userId
  if (categoryId) params.categoryId = categoryId
  const { data } = await api.get<Result<Skill[]>>('/api/skills', { params })
  return unwrap(data)
}

export async function getSkillDetails(skillId: number): Promise<any> {
  const { data } = await api.get<Result<any>>(`/api/skills/${skillId}`)
  return unwrap(data)
}

export async function searchSkills(userId: number, keyword: string): Promise<Skill[]> {
  const params = { userId, keyword }
  const { data } = await api.get<Result<Skill[]>>('/api/skills/search', { params })
  return unwrap(data)
}

export async function getSkillByCode(userId: number, skillCode: string): Promise<Skill> {
  const params = { userId, skillCode }
  const { data } = await api.get<Result<Skill>>('/api/skills/by-code', { params })
  return unwrap(data)
}

export async function createSkill(userId: number, skillData: any): Promise<Skill> {
  const { data } = await api.post<Result<Skill>>('/api/skills', skillData, { params: { userId } })
  return unwrap(data)
}

export async function updateSkill(skillId: number, skillData: any): Promise<Skill> {
  const { data } = await api.put<Result<Skill>>(`/api/skills/${skillId}`, skillData)
  return unwrap(data)
}

export async function deleteSkill(skillId: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/skills/${skillId}`)
  unwrap(data)
}

export async function getSkillVersions(skillId: number): Promise<SkillVersion[]> {
  const { data } = await api.get<Result<SkillVersion[]>>(`/api/skills/${skillId}/versions`)
  return unwrap(data)
}

export async function getActiveSkillVersion(skillId: number): Promise<SkillVersion> {
  const { data } = await api.get<Result<SkillVersion>>(`/api/skills/${skillId}/versions/active`)
  return unwrap(data)
}

export async function createSkillVersion(skillId: number, payload: {
  promptTemplate?: string
  releaseNotes?: string
}): Promise<SkillVersion> {
  const { data } = await api.post<Result<SkillVersion>>(`/api/skills/${skillId}/versions`, payload)
  return unwrap(data)
}

export async function activateSkillVersion(versionId: number): Promise<void> {
  const { data } = await api.post<Result<null>>(`/api/skills/versions/${versionId}/activate`)
  unwrap(data)
}

export async function getSkillCategories(userId?: number): Promise<SkillCategory[]> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<SkillCategory[]>>('/api/skills/categories', { params })
  return unwrap(data)
}

export async function createSkillCategory(userId: number, payload: {
  name: string
  code: string
  description?: string
}): Promise<SkillCategory> {
  const { data } = await api.post<Result<SkillCategory>>('/api/skills/categories', payload, { params: { userId } })
  return unwrap(data)
}

export async function deleteSkillCategory(categoryId: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/skills/categories/${categoryId}`)
  unwrap(data)
}

export async function getTrashItems(userId?: number): Promise<TrashItem[]> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<TrashItem[]>>('/api/trash', { params })
  return unwrap(data)
}

export async function getTrashItemsByType(userId: number, itemType: string): Promise<TrashItem[]> {
  const params = { userId, itemType }
  const { data } = await api.get<Result<TrashItem[]>>('/api/trash/by-type', { params })
  return unwrap(data)
}

export async function getTrashItem(trashId: number): Promise<TrashItem> {
  const { data } = await api.get<Result<TrashItem>>(`/api/trash/${trashId}`)
  return unwrap(data)
}

export async function restoreTrashItem(trashId: number): Promise<void> {
  const { data } = await api.post<Result<null>>(`/api/trash/${trashId}/restore`)
  unwrap(data)
}

export async function permanentDeleteTrashItem(trashId: number): Promise<void> {
  const { data } = await api.delete<Result<null>>(`/api/trash/${trashId}`)
  unwrap(data)
}

export async function emptyTrash(userId?: number): Promise<number> {
  const params = userId ? { userId } : {}
  const { data } = await api.delete<Result<{ deletedCount: number }>>('/api/trash/empty', { params })
  return unwrap(data).deletedCount
}

export async function getTrashCount(userId?: number): Promise<number> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<{ count: number }>>('/api/trash/count', { params })
  return unwrap(data).count
}

export async function getTrashStats(userId?: number): Promise<{ total: number; schedules: number; documents: number; notes: number }> {
  const params = userId ? { userId } : {}
  const { data } = await api.get<Result<{ total: number; schedules: number; documents: number; notes: number }>>('/api/trash/stats', { params })
  return unwrap(data)
}


