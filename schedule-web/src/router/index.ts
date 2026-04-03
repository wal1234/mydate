import { createRouter, createWebHistory } from 'vue-router'
import CalendarPage from '../views/CalendarPage.vue'
import TodayPage from '../views/TodayPage.vue'
import DocsPage from '../views/DocsPage.vue'
import DocEditorPage from '../views/DocEditorPage.vue'
import NotesPage from '../views/NotesPage.vue'
import NoteEditorPage from '../views/NoteEditorPage.vue'
import SettingsPage from '../views/SettingsPage.vue'
import ChatPage from '../views/ChatPage.vue'
import PromptTemplatesPage from '../views/PromptTemplatesPage.vue'
import SkillsPage from '../views/SkillsPage.vue'
import TrashPage from '../views/TrashPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'calendar', component: CalendarPage },
    { path: '/today', name: 'today', component: TodayPage },
    { path: '/docs', name: 'docs', component: DocsPage },
    { path: '/docs/:date', name: 'docEditor', component: DocEditorPage, props: true },
    { path: '/notes', name: 'notes', component: NotesPage },
    { path: '/notes/:id', name: 'noteEditor', component: NoteEditorPage, props: true },
    { path: '/settings', name: 'settings', component: SettingsPage },
    { path: '/chat', name: 'chat', component: ChatPage },
    { path: '/prompts', name: 'prompts', component: PromptTemplatesPage },
    { path: '/skills', name: 'skills', component: SkillsPage },
    { path: '/trash', name: 'trash', component: TrashPage },
  ],
})

export default router

