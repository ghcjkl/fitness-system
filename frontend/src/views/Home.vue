<template>
  <div class="dashboard">
    <section class="welcome">
      <div class="welcome-text">
        <div class="eyebrow">{{ greeting }}，{{ displayName }}</div>
        <h1>今天也把节奏握在手里</h1>
        <p>
          {{
            health && health.target
              ? '当前目标 · ' + health.target
              : '还没有健康档案，先补上基础数据，后面的建议才更准。'
          }}
        </p>
      </div>
      <div class="welcome-actions">
        <el-button size="large" class="ghost-btn" @click="go('/health')">编辑档案</el-button>
        <el-button size="large" type="primary" @click="go('/sport')">去打卡</el-button>
      </div>
    </section>

    <section class="kpis">
      <article v-for="k in kpis" :key="k.label" class="kpi">
        <div class="kpi-head">
          <span class="kpi-icon" :style="{ background: k.tint, color: k.color }">
            <app-icon :name="k.icon" :size="18" />
          </span>
          <span class="kpi-label">{{ k.label }}</span>
        </div>
        <div class="kpi-value">
          {{ k.value }}<small>{{ k.unit }}</small>
        </div>
        <div class="kpi-note">{{ k.note }}</div>
      </article>
    </section>

    <section v-if="warningsText" class="notice-card">
      <span class="notice-icon"><app-icon name="flame" :size="18" /></span>
      <div>
        <strong>今日提醒</strong>
        <p>{{ warningsText }}</p>
      </div>
    </section>

    <section class="section">
      <div class="section-head">
        <h3>快捷入口</h3>
        <span>常用功能</span>
      </div>
      <div class="entries">
        <button
          v-for="entry in entries"
          :key="entry.path"
          type="button"
          class="entry"
          @click="go(entry.path)"
        >
          <span class="entry-icon" :style="{ background: entry.tint, color: entry.color }">
            <app-icon :name="entry.icon" :size="20" />
          </span>
          <span class="entry-text">
            <strong>{{ entry.title }}</strong>
            <span>{{ entry.desc }}</span>
          </span>
          <app-icon name="arrow" :size="16" class="entry-arrow" />
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo } from '../api/user'
import { getHealth } from '../api/health'
import { dietStats } from '../api/diet'
import { getTrend } from '../api/stats'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const info = ref({})
const health = ref(null)
const todayStats = ref({ calorie: 0, warnings: [] })
const todaySport = ref(0)

const localDate = () => {
  const d = new Date()
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
}
const today = localDate()

const displayName = computed(() => info.value.nickname || info.value.username || '用户')
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
const warningsText = computed(() => {
  const w = todayStats.value.warnings
  return Array.isArray(w) && w.length ? w.join('；') : ''
})

const kpis = computed(() => [
  {
    label: '今日摄入',
    value: todayStats.value.calorie || 0,
    unit: 'kcal',
    note: '饮食记录合计',
    icon: 'diet',
    tint: '#eef6e2',
    color: '#55752f'
  },
  {
    label: '今日运动',
    value: todaySport.value,
    unit: '分钟',
    note: '今天累计训练时长',
    icon: 'sport',
    tint: '#e5f3e5',
    color: '#2f7a4f'
  },
  {
    label: '体重',
    value: health.value && health.value.weight != null ? health.value.weight : '--',
    unit: 'kg',
    note: '来自健康档案',
    icon: 'scale',
    tint: '#e8f1ea',
    color: '#1f5c45'
  },
  {
    label: '体脂率',
    value: health.value && health.value.bodyFat != null ? health.value.bodyFat : '--',
    unit: '%',
    note: '来自健康档案',
    icon: 'drop',
    tint: '#e3f2ea',
    color: '#2b7a64'
  }
])

const entries = [
  {
    path: '/health',
    title: '健康档案',
    desc: '身高、体重、体脂与身体状态',
    icon: 'health',
    tint: '#e7f0ea',
    color: '#1f5c45'
  },
  {
    path: '/sport',
    title: '运动打卡',
    desc: '今天动了多久，消耗多少热量',
    icon: 'sport',
    tint: '#e5f3e5',
    color: '#2f7a4f'
  },
  {
    path: '/diet',
    title: '饮食打卡',
    desc: '三餐记录与每日营养目标',
    icon: 'diet',
    tint: '#eef6e2',
    color: '#55752f'
  },
  {
    path: '/stats',
    title: '数据可视化',
    desc: '体重与体脂的长期曲线',
    icon: 'stats',
    tint: '#e6f2ea',
    color: '#2f6f57'
  },
  {
    path: '/ai',
    title: 'AI 方案',
    desc: '结合健康档案生成训练与饮食建议',
    icon: 'ai',
    tint: '#f1f5e6',
    color: '#6b8f2e'
  },
  {
    path: '/community',
    title: '社区分享',
    desc: '和同伴一起把坚持写下去',
    icon: 'community',
    tint: '#e9f3e5',
    color: '#3c7355'
  }
]

const go = (path) => router.push(path)

const load = async () => {
  const [u, h, d, t] = await Promise.allSettled([
    getUserInfo(),
    getHealth(),
    dietStats(today),
    getTrend(1)
  ])
  if (u.status === 'fulfilled') info.value = u.value.data || {}
  if (h.status === 'fulfilled') health.value = h.value.data
  if (d.status === 'fulfilled') todayStats.value = d.value.data || { calorie: 0, warnings: [] }
  if (t.status === 'fulfilled' && t.value.data && Array.isArray(t.value.data.sportDuration)) {
    todaySport.value = t.value.data.sportDuration[0] || 0
  }
}

onMounted(load)
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.welcome {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--brand-3);
}

.welcome h1 {
  margin: 8px 0 6px;
  font-size: clamp(24px, 2.4vw, 32px);
  letter-spacing: -0.02em;
}

.welcome p {
  font-size: 13.5px;
  color: var(--muted);
}

.welcome-actions {
  display: flex;
  gap: 10px;
}

.welcome-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 0 20px;
}

.ghost-btn {
  background: #ffffff;
  border: 1px solid var(--line);
  color: var(--ink-soft);
}

.kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.kpi {
  padding: 18px 20px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid rgba(20, 53, 42, 0.04);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.kpi:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.kpi-head {
  display: flex;
  align-items: center;
  gap: 9px;
}

.kpi-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
}

.kpi-label {
  font-size: 12.5px;
  color: var(--muted);
}

.kpi-value {
  margin: 14px 0 6px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.kpi-value small {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--muted);
}

.kpi-note {
  font-size: 11.5px;
  color: #9aa6a0;
}

.notice-card {
  display: flex;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 16px;
  border: 1px solid #dfeacb;
  background: linear-gradient(120deg, #f8fcec, #fdfef9);
}

.notice-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: #eef7d9;
  color: #6b8f2e;
}

.notice-card strong {
  font-size: 13px;
}

.notice-card p {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: #5c6b4f;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}

.section-head h3 {
  font-size: 15px;
}

.section-head span {
  font-size: 12px;
  color: var(--muted);
}

.entries {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(268px, 1fr));
  gap: 14px;
}

.entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(20, 53, 42, 0.04);
  border-radius: 16px;
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  text-align: left;
  font-family: inherit;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.entry:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.entry-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  display: grid;
  place-items: center;
}

.entry-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.entry-text strong {
  font-size: 13.5px;
  color: var(--ink);
}

.entry-text span {
  font-size: 11.5px;
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.entry-arrow {
  margin-left: auto;
  color: #b9c9bb;
}

@media (max-width: 1180px) {
  .kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard {
    gap: 16px;
  }

  .kpi {
    padding: 15px 16px;
  }

  .kpi-value {
    font-size: 26px;
    margin: 10px 0 5px;
  }

  .notice-card {
    padding: 13px 14px;
  }

  .entries {
    gap: 10px;
  }

  .entry {
    padding: 12px 14px;
  }
}

@media (max-width: 380px) {
  .kpis {
    grid-template-columns: 1fr;
  }

  .welcome-actions {
    width: 100%;
  }

  .welcome-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
