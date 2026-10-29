import { createRouter, createWebHistory } from 'vue-router'
import { getUserInfo } from '../api/user'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', component: () => import('../views/Home.vue') },
      { path: 'health', component: () => import('../views/Health.vue') },
      { path: 'sport', component: () => import('../views/Sport.vue') },
      { path: 'diet', component: () => import('../views/Diet.vue') },
      { path: 'food', component: () => import('../views/Food.vue') },
      { path: 'community', component: () => import('../views/Community.vue') },
      { path: 'report', component: () => import('../views/Report.vue') },
      { path: 'ai', component: () => import('../views/AiPlan.vue') },
      { path: 'stats', component: () => import('../views/Stats.vue') },
      { path: 'admin', component: () => import('../views/Admin.vue'), meta: { requiresAdmin: true } },
      { path: 'profile', component: () => import('../views/Profile.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) return '/login'
  // 管理后台只对 ADMIN 账号开放，普通用户直接回首页
  if (to.meta.requiresAdmin) {
    try {
      const res = await getUserInfo()
      if (res.data.role !== 'ADMIN') return '/home'
    } catch (e) {
      localStorage.removeItem('token')
      return '/login'
    }
  }
})
export default router
