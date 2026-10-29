<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <app-icon name="sport" :size="19" :stroke-width="2.1" />
        </div>
        <div class="brand-text">
          <strong>FitLife</strong>
          <span>个性化健康管理</span>
        </div>
      </div>

      <nav class="nav">
        <div v-for="group in menus" :key="group.title" class="nav-group">
          <div class="nav-title">{{ group.title }}</div>
          <button
            v-for="item in group.items"
            :key="item.path"
            type="button"
            class="nav-item"
            :class="{ active: route.path === item.path }"
            :title="item.label"
            @click="router.push(item.path)"
          >
            <app-icon :name="item.icon" :size="19" />
            <span class="nav-label">{{ item.label }}</span>
          </button>
        </div>
      </nav>

      <div class="sidebar-foot">
        <div class="foot-card">
          <app-icon name="flame" :size="18" />
          <div>
            <strong>保持节奏</strong>
            <span>每天记录一点</span>
          </div>
        </div>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-title">
          <h2>{{ currentTitle }}</h2>
          <span>{{ todayText }}</span>
        </div>

        <div class="topbar-right">
          <div class="user">
            <div class="avatar">
              <img v-if="avatar && !avatarError" :src="avatar" alt="avatar" @error="avatarError = true" />
              <span v-else>{{ initial }}</span>
            </div>
            <div class="user-meta">
              <strong>{{ displayName || '用户' }}</strong>
              <span>{{ isAdmin ? '管理员' : '普通用户' }}</span>
            </div>
          </div>
          <button class="logout" type="button" title="退出登录" @click="logout">
            <app-icon name="logout" :size="18" />
          </button>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>

    <nav class="mobile-nav">
      <button
        v-for="item in mobileMenus"
        :key="item.path"
        type="button"
        class="mnav-item"
        :class="{ active: route.path === item.path }"
        @click="router.push(item.path)"
      >
        <app-icon :name="item.icon" :size="20" />
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo } from '../api/user'
import AppIcon from '../components/AppIcon.vue'

const route = useRoute()
const router = useRouter()
const info = ref({})
const isAdmin = ref(false)
const avatarError = ref(false)

const displayName = computed(() => info.value.nickname || info.value.username || '')
const avatar = computed(() => info.value.avatar || '')
const initial = computed(() => (displayName.value || 'U').charAt(0).toUpperCase())

const titles = {
  '/home': '首页',
  '/health': '健康档案',
  '/sport': '运动打卡',
  '/diet': '饮食打卡',
  '/food': '食材百科',
  '/community': '社区分享',
  '/report': '体检报告',
  '/ai': 'AI 方案',
  '/stats': '数据可视化',
  '/admin': '管理后台',
  '/profile': '个人中心'
}
const currentTitle = computed(() => titles[route.path] || '健康管理')

const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const now = new Date()
const todayText = `${now.getMonth() + 1}月${now.getDate()}日 ${week[now.getDay()]}`

const menus = computed(() => {
  const groups = [
    { title: '概览', items: [{ path: '/home', label: '首页', icon: 'home' }] },
    {
      title: '记录',
      items: [
        { path: '/health', label: '健康档案', icon: 'health' },
        { path: '/sport', label: '运动打卡', icon: 'sport' },
        { path: '/diet', label: '饮食打卡', icon: 'diet' },
        { path: '/food', label: '食材百科', icon: 'food' },
        { path: '/report', label: '体检报告', icon: 'report' }
      ]
    },
    {
      title: '洞察',
      items: [
        { path: '/stats', label: '数据可视化', icon: 'stats' },
        { path: '/ai', label: 'AI 方案', icon: 'ai' }
      ]
    },
    { title: '社区', items: [{ path: '/community', label: '社区分享', icon: 'community' }] }
  ]
  if (isAdmin.value) {
    groups.push({ title: '管理', items: [{ path: '/admin', label: '管理后台', icon: 'admin' }] })
  }
  groups.push({ title: '账户', items: [{ path: '/profile', label: '个人中心', icon: 'profile' }] })
  return groups
})

const mobileMenus = [
  { path: '/home', label: '首页', icon: 'home' },
  { path: '/sport', label: '运动', icon: 'sport' },
  { path: '/diet', label: '饮食', icon: 'diet' },
  { path: '/community', label: '社区', icon: 'community' },
  { path: '/profile', label: '我的', icon: 'profile' }
]

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(async () => {
  try {
    const res = await getUserInfo()
    info.value = res.data || {}
    isAdmin.value = res.data.role === 'ADMIN'
  } catch (e) {
    /* 头部信息失败不影响页面 */
  }
})
</script>

<style scoped>
.shell {
  min-height: 100vh;
  padding-left: 268px;
  background:
    radial-gradient(58% 46% at 100% 0%, rgba(200, 241, 105, 0.22), transparent 62%),
    radial-gradient(50% 44% at 0% 100%, rgba(58, 125, 92, 0.12), transparent 60%),
    radial-gradient(42% 38% at 2% 2%, rgba(217, 233, 209, 0.75), transparent 62%),
    var(--app-bg);
  background-attachment: fixed;
}

.sidebar {
  position: fixed;
  top: 16px;
  left: 16px;
  bottom: 16px;
  z-index: 30;
  width: 236px;
  display: flex;
  flex-direction: column;
  padding: 22px 14px 16px;
  overflow: hidden;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.07);
  background: linear-gradient(165deg, #1d4937 0%, #14352a 55%, #0f281e 100%);
  box-shadow: 0 24px 48px rgba(16, 40, 31, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.sidebar::before {
  content: "";
  position: absolute;
  top: -70px;
  right: -70px;
  width: 190px;
  height: 190px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(200, 241, 105, 0.3), transparent 68%);
  pointer-events: none;
}

.brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 4px 8px 22px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: var(--brand);
  background: var(--lime);
  box-shadow: 0 8px 20px rgba(200, 241, 105, 0.28);
}

.brand-text strong {
  display: block;
  font-size: 15.5px;
  color: #f4f8ee;
  letter-spacing: -0.01em;
}

.brand-text span {
  font-size: 11px;
  color: rgba(244, 248, 238, 0.52);
}

.nav {
  position: relative;
  flex: 1;
  width: 100%;
  overflow-y: auto;
  padding: 0 2px 4px;
}

.nav::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.16);
}

.nav-group + .nav-group {
  margin-top: 18px;
}

.nav-title {
  padding: 0 12px 8px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: rgba(244, 248, 238, 0.36);
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 2px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: rgba(244, 248, 238, 0.74);
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease,
    transform 0.18s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.nav-item.active {
  background: var(--lime);
  color: var(--brand);
  font-weight: 600;
  box-shadow: 0 10px 22px rgba(200, 241, 105, 0.26);
}

.nav-item.active :deep(svg) {
  color: var(--brand);
}

.sidebar-foot {
  position: relative;
  padding: 14px 4px 0;
}

.foot-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: #f4f8ee;
}

.foot-card :deep(svg) {
  color: var(--lime);
}

.foot-card strong {
  display: block;
  font-size: 12.5px;
}

.foot-card span {
  font-size: 11px;
  color: rgba(244, 248, 238, 0.5);
}

.main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  position: sticky;
  top: 16px;
  z-index: 20;
  height: 62px;
  margin: 16px 20px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px 0 24px;
  border-radius: 18px;
  background: rgba(252, 253, 249, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 14px 34px rgba(20, 53, 42, 0.08);
  backdrop-filter: blur(16px) saturate(1.4);
}

.topbar-title h2 {
  font-size: 17px;
  letter-spacing: -0.01em;
}

.topbar-title span {
  font-size: 12px;
  color: var(--muted);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  overflow: hidden;
  display: grid;
  place-items: center;
  background: var(--brand);
  color: var(--lime);
  font-size: 14px;
  font-weight: 700;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-meta strong {
  display: block;
  font-size: 13px;
}

.user-meta span {
  font-size: 11px;
  color: var(--muted);
}

.logout {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  border: 1px solid transparent;
  background: #f2f5f1;
  color: #5b6b63;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: color 0.18s ease, background 0.18s ease;
}

.logout:hover {
  color: #c8452d;
  background: #fdf3f0;
}

.content {
  padding: 22px 20px 48px;
}

.mobile-nav {
  display: none;
}

@media (max-width: 960px) {
  .shell {
    padding-left: 104px;
  }

  .sidebar {
    width: 72px;
    padding: 18px 10px 14px;
    border-radius: 22px;
    align-items: center;
  }

  .brand {
    justify-content: center;
    padding: 4px 0 18px;
  }

  .brand-text,
  .nav-title,
  .nav-label,
  .sidebar-foot {
    display: none;
  }

  .nav-item {
    justify-content: center;
    padding: 11px 0;
  }

  .topbar {
    height: 58px;
    margin: 12px 12px 0;
    padding: 0 10px 0 16px;
  }

  .user-meta {
    display: none;
  }

  .topbar-title span {
    display: none;
  }

  .content {
    padding: 16px 12px 40px;
  }
}

/* 手机端：隐藏侧栏，改用底部导航 */
@media (max-width: 768px) {
  .shell {
    padding-left: 0;
  }

  .sidebar {
    display: none;
  }

  .topbar {
    top: 8px;
    height: 54px;
    margin: 8px 10px 0;
    padding: 0 10px 0 14px;
    border-radius: 14px;
  }

  .topbar-title h2 {
    font-size: 16px;
  }

  .topbar-right {
    gap: 8px;
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 10px;
  }

  .logout {
    width: 32px;
    height: 32px;
    border-radius: 10px;
  }

  .content {
    padding: 14px 12px calc(84px + env(safe-area-inset-bottom));
  }

  .mobile-nav {
    position: fixed;
    left: 10px;
    right: 10px;
    bottom: calc(8px + env(safe-area-inset-bottom));
    z-index: 40;
    display: flex;
    padding: 6px 4px;
    border-radius: 20px;
    background: rgba(252, 253, 249, 0.92);
    border: 1px solid rgba(255, 255, 255, 0.9);
    box-shadow: 0 16px 34px rgba(20, 53, 42, 0.16);
    backdrop-filter: blur(18px) saturate(1.4);
  }

  .mnav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 3px;
    padding: 5px 0 4px;
    border: none;
    border-radius: 14px;
    background: transparent;
    color: #7b8f82;
    font-family: inherit;
    font-size: 10.5px;
    cursor: pointer;
    transition: color 0.18s ease, background 0.18s ease;
  }

  .mnav-item.active {
    color: var(--brand-2);
    background: #eef7e6;
    font-weight: 600;
  }

  .mnav-item.active :deep(svg) {
    color: var(--brand-2);
  }
}

@media (max-width: 380px) {
  .topbar {
    margin: 8px 8px 0;
  }

  .content {
    padding-left: 10px;
    padding-right: 10px;
  }

  .mnav-item {
    font-size: 10px;
  }
}
</style>
