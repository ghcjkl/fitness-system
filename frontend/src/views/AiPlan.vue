<template>
  <div class="ai-page">
    <header class="hero">
      <div class="hero-copy">
        <span class="eyebrow">
          <app-icon name="leaf" :size="14" :stroke-width="2" />
          今天属于你的计划
        </span>
        <h1>今天，<em>按计划来</em>。</h1>
        <p class="hero-sub">
          结合健康档案、忌口与旧伤，排出今天就能执行的饮食与健康安排。
        </p>
      </div>

      <aside class="note-card" aria-hidden="true">
        <span class="note-label">COACH'S NOTE</span>
        <p>先做能坚持的，再做更难的。每周三次，也比空想七次强。</p>
        <span class="note-sign">— 健康手记</span>
      </aside>
    </header>

    <div class="board">
      <section class="plan-panel">
        <div class="panel-head">
          <div class="segmented" role="tablist" aria-label="方案类型">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              type="button"
              role="tab"
              class="seg"
              :class="{ on: type === tab.value }"
              :aria-selected="type === tab.value"
              @click="switchType(tab.value)"
            >
              <app-icon :name="tab.icon" :size="16" />
              {{ tab.label }}
            </button>
          </div>
          <button class="icon-btn" type="button" title="读取最近一次方案" @click="loadLatest">
            <app-icon name="refresh" :size="16" />
          </button>
        </div>

        <div class="composer">
          <div class="tag-row">
            <button
              v-for="tag in quickTags"
              :key="tag"
              type="button"
              class="tag"
              :class="{ on: hasTag(tag) }"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </button>
          </div>
          <textarea
            v-model="remark"
            rows="2"
            maxlength="80"
            placeholder="补充要求（可选），如：不吃辣、每周练 4 次、素食"
          ></textarea>
          <div class="composer-foot">
            <span class="hint">会结合健康档案、过敏食材与运动旧伤</span>
            <button class="generate" type="button" :disabled="loading" @click="generate">
              <app-icon name="leaf" :size="17" />
              <span>{{ loading ? '正在排计划…' : `生成${typeLabel}方案` }}</span>
            </button>
          </div>
        </div>

        <div v-if="!plan" class="empty-state">
          <div class="empty-mark">
            <app-icon name="leaf" :size="32" :stroke-width="1.5" />
          </div>
          <strong>还没有{{ typeLabel }}方案</strong>
          <p>选好类型，写下你的要求，AI会给你一份今天就能执行的计划。</p>
        </div>

        <article v-else class="result">
          <div class="result-top">
            <span class="stamp">{{ typeLabel }}计划</span>
            <time>{{ formatTime(plan.createTime) }}</time>
          </div>

          <h2 v-if="parsed.title" class="plan-title">{{ parsed.title }}</h2>
          <p v-if="parsed.profile" class="profile-line">
            <app-icon name="health" :size="14" />
            <span>{{ parsed.profile }}</span>
          </p>

          <div v-if="parsed.macros.length" class="macro-row">
            <div v-for="m in parsed.macros" :key="m.label" class="macro">
              <span>{{ m.label }}</span>
              <strong>{{ m.value }}</strong>
            </div>
          </div>

          <div class="plan-body">
            <template v-for="(p, i) in parsed.parts" :key="i">
              <div v-if="p.type === 'meal'" class="meal">
                <div class="meal-head">
                  <span class="meal-no">{{ String(p.no).padStart(2, '0') }}</span>
                  <strong>{{ p.label }}</strong>
                  <span class="meal-meta">{{ p.meta }}</span>
                </div>
                <div class="meal-items">
                  <span v-for="(item, j) in splitItems(p.items)" :key="j" class="food">{{ item }}</span>
                </div>
              </div>

              <div v-else-if="p.type === 'day'" class="day-row">
                <span class="day-chip">{{ p.day }}</span>
                <p>{{ p.text }}</p>
              </div>

              <div v-else-if="p.type === 'section'" class="section">
                <h4 v-if="p.title">{{ p.title }}</h4>
                <div v-if="p.title.includes('参考依据')" class="source-list">
                  <span v-for="(item, j) in p.items" :key="j">{{ item }}</span>
                </div>
                <ul v-else-if="p.items.length" class="note-list">
                  <li v-for="(item, j) in p.items" :key="j">{{ item }}</li>
                </ul>
              </div>

              <p v-else class="plain">{{ p.text }}</p>
            </template>
          </div>
        </article>
      </section>

      <aside class="coach-panel">
        <header class="coach-head">
          <div class="coach-mark">
            <app-icon name="leaf" :size="19" :stroke-width="1.9" />
          </div>
          <div class="coach-id">
            <strong>问问它</strong>
            <span>饮食 · 训练 · 养生，都可以问</span>
          </div>
          <button v-if="messages.length" class="clear" type="button" @click="clearChat">清空</button>
        </header>

        <div ref="chatScroll" class="chat-scroll">
          <div v-if="!messages.length" class="chat-empty">
            <p>不知从哪问起？试试这些：</p>
            <button v-for="q in suggestions" :key="q" type="button" class="suggest" @click="ask(q)">
              {{ q }}
            </button>
          </div>

          <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
            <div v-if="m.role === 'coach'" class="msg-label">
              <app-icon name="leaf" :size="12" />
              AI管理
            </div>
            <div class="bubble">
              <p>{{ m.text }}</p>
              <div v-if="m.sources && m.sources.length" class="msg-sources">
                <span v-for="s in m.sources" :key="s.id || s">{{ s.title || s }}</span>
              </div>
            </div>
          </div>

          <div v-if="chatLoading" class="msg coach">
            <div class="msg-label">
              <app-icon name="leaf" :size="12" />
              AI管理
            </div>
            <div class="bubble typing"><i></i><i></i><i></i></div>
          </div>
        </div>

        <form class="ask-box" @submit.prevent="send">
          <input
            v-model="question"
            type="text"
            placeholder="问问他，例如：尿酸高怎么吃？"
            :disabled="chatLoading"
          />
          <button type="submit" :disabled="!question.trim() || chatLoading" aria-label="发送">
            <app-icon name="send" :size="17" />
          </button>
        </form>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { generatePlan, getLatestPlan, aiChat } from '../api/ai'
import AppIcon from '../components/AppIcon.vue'

const tabs = [
  { value: 'diet', label: '饮食方案', icon: 'diet' },
  { value: 'sport', label: '运动方案', icon: 'sport' }
]
const quickTags = ['不吃辣', '每周练 4 次', '素食', '少油少盐']
const suggestions = [
  '尿酸偏高，平时怎么吃？',
  '膝盖有旧伤，还能跑步吗？',
  '减脂期晚餐怎么搭配？'
]

const type = ref('diet')
const typeLabel = computed(() => (type.value === 'diet' ? '饮食' : '运动'))
const remark = ref('')
const plan = ref(null)
const loading = ref(false)

const messages = ref([])
const question = ref('')
const chatLoading = ref(false)
const chatScroll = ref(null)

const hasTag = (tag) => remark.value.includes(tag)

const toggleTag = (tag) => {
  if (hasTag(tag)) {
    remark.value = remark.value
      .replace(tag, '')
      .replace(/^[、，,\s]+/, '')
      .replace(/[、，,\s]+$/, '')
  } else {
    remark.value = remark.value ? `${remark.value}、${tag}` : tag
  }
}

const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

const loadLatest = async () => {
  try {
    const res = await getLatestPlan(type.value)
    plan.value = res.data || null
  } catch (e) {
    plan.value = null
  }
}

const switchType = (value) => {
  if (type.value === value) return
  type.value = value
  plan.value = null
  loadLatest()
}

const generate = async () => {
  loading.value = true
  try {
    const res = await generatePlan(type.value, { remark: remark.value })
    if (res.data && res.data.answer) {
      plan.value = {
        content: res.data.answer,
        createTime: res.data.createTime || formatTime(new Date().toISOString())
      }
      ElMessage.success('计划已生成')
    } else {
      await loadLatest()
    }
  } catch (e) {
    ElMessage.error(e.message || '生成失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const splitItems = (items) =>
  (items || [])
    .flatMap((item) => String(item).split(' + '))
    .map((item) => item.trim())
    .filter(Boolean)

/** 把后端返回的多行文本切成结构化区块，便于排版而不是直接贴一段文字 */
const parsePlan = (raw) => {
  const result = { title: '', profile: '', macros: [], parts: [] }
  if (!raw) return result

  const lines = String(raw).split('\n')
  let part = null
  let mealNo = 0

  lines.forEach((line, index) => {
    const text = line.trim()
    if (!text) return

    if (index === 0) {
      result.title = text
      return
    }
    if (!result.profile && text.startsWith('你的健康档案')) {
      result.profile = text
      return
    }
    if (text.startsWith('每日目标：')) {
      text
        .replace('每日目标：', '')
        .split('｜')
        .map((seg) => seg.trim())
        .forEach((seg) => {
          const cal = seg.match(/([\d,]+)\s*kcal/)
          if (cal) {
            result.macros.push({ label: '热量', value: `${cal[1]} kcal` })
            return
          }
          const macro = seg.match(/^(蛋白质|脂肪|碳水)\s*(\d+)\s*g$/)
          if (macro) {
            result.macros.push({ label: macro[1], value: `${macro[2]} g` })
          }
        })
      return
    }

    const meal = text.match(/^(早餐|午餐|晚餐|加餐)（约\s*(\d+)%，约\s*([\d,]+)\s*kcal）/)
    if (meal) {
      mealNo += 1
      part = {
        type: 'meal',
        no: mealNo,
        label: meal[1],
        meta: `约 ${meal[2]}% · ${meal[3]} kcal`,
        items: []
      }
      result.parts.push(part)
      return
    }

    const day = text.match(/^(周[一二三四五六日])\s+(.+)$/)
    if (day) {
      result.parts.push({ type: 'day', day: day[1], text: day[2] })
      part = null
      return
    }

    if (
      /^(执行要点：|针对你的忌口\/健康状况：|需要特别注意：)$/.test(text) ||
      text.startsWith('每周安排')
    ) {
      part = { type: 'section', title: text, items: [] }
      result.parts.push(part)
      return
    }

    if (text.startsWith('- ')) {
      const item = text.slice(2)
      if (part && (part.type === 'meal' || part.type === 'section')) {
        part.items.push(item)
      } else {
        part = { type: 'section', title: '', items: [item] }
        result.parts.push(part)
      }
      return
    }

    if (part && part.type === 'meal') {
      part.items.push(text)
      return
    }
    result.parts.push({ type: 'text', text })
  })

  return result
}

const parsed = computed(() => parsePlan(plan.value && plan.value.content))

const scrollChat = () =>
  nextTick(() => {
    if (chatScroll.value) {
      chatScroll.value.scrollTop = chatScroll.value.scrollHeight
    }
  })

const ask = (text) => {
  question.value = text
  send()
}

const send = async () => {
  const q = question.value.trim()
  if (!q || chatLoading.value) return
  messages.value.push({ role: 'user', text: q })
  question.value = ''
  chatLoading.value = true
  scrollChat()
  try {
    const res = await aiChat(q)
    const data = res.data || {}
    messages.value.push({
      role: 'coach',
      text: data.answer || 'AI暂时没有找到合适的建议。',
      sources: data.sources || []
    })
  } catch (e) {
    messages.value.push({
      role: 'coach',
      text: e.message || '网络不太顺畅，请稍后再问一次。'
    })
  } finally {
    chatLoading.value = false
    scrollChat()
  }
}

const clearChat = () => {
  messages.value = []
}

onMounted(loadLatest)
</script>

<style scoped>
.ai-page {
  position: relative;
  max-width: 1180px;
  margin: 0 auto;
  padding-bottom: 24px;
  --paper: #fbfdf8;
  --mint: #e9f4e4;
  --mint-deep: #dcecd6;
  --fern: #2f6b4f;
  --leaf: #4f8a68;
  --deep: #1f5c45;
  --line: #dbe6d8;
  --body-ink: #1d2a24;
  --body-muted: #6d7f73;
  --font-display: "Noto Serif SC", "Source Han Serif SC", "Songti SC", "SimSun", serif;
  --font-hand: "KaiTi", "STKaiti", "Kaiti SC", "SimSun", serif;
  color: var(--body-ink);
}

.ai-page::before {
  content: "";
  position: absolute;
  top: -34px;
  left: -60px;
  right: -60px;
  height: 340px;
  background:
    radial-gradient(52% 90% at 16% 0%, rgba(200, 241, 105, 0.26), transparent 70%),
    radial-gradient(46% 80% at 92% 6%, rgba(79, 138, 104, 0.16), transparent 72%);
  pointer-events: none;
}

.ai-page > * {
  position: relative;
}

/* 顶部 */
.hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 28px;
  padding: 6px 4px 26px;
  animation: rise 0.5s ease both;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--mint-deep);
  color: var(--fern);
  font-size: 11.5px;
  letter-spacing: 0.08em;
}

.hero h1 {
  margin: 14px 0 8px;
  font-family: var(--font-display);
  font-size: clamp(30px, 3.6vw, 44px);
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--deep);
  line-height: 1.15;
}

.hero h1 em {
  position: relative;
  font-style: normal;
  white-space: nowrap;
}

.hero h1 em::after {
  content: "";
  position: absolute;
  left: -3px;
  right: -3px;
  bottom: 5px;
  height: 11px;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(200, 241, 105, 0.85), rgba(200, 241, 105, 0.12));
  z-index: -1;
}

.hero-sub {
  max-width: 520px;
  color: var(--body-muted);
  font-size: 13.5px;
  line-height: 1.8;
}

.note-card {
  flex-shrink: 0;
  width: 236px;
  padding: 16px 18px 14px;
  transform: rotate(1.6deg);
  border-radius: 14px;
  background: #fffdf6;
  border: 1px dashed #cfdcc7;
  box-shadow: 0 16px 30px rgba(31, 92, 69, 0.1);
}

.note-label {
  font-size: 9.5px;
  letter-spacing: 0.22em;
  color: var(--leaf);
}

.note-card p {
  margin: 8px 0 10px;
  font-family: var(--font-hand);
  font-size: 15.5px;
  line-height: 1.75;
  color: #35493d;
}

.note-sign {
  display: block;
  text-align: right;
  font-size: 11.5px;
  color: var(--body-muted);
}

/* 布局 */
.board {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.92fr);
  gap: 20px;
  align-items: start;
}

/* 方案面板 */
.plan-panel {
  position: relative;
  padding: 20px 22px 24px;
  border-radius: 24px;
  border: 1px solid var(--line);
  background-color: var(--paper);
  background-image: radial-gradient(rgba(47, 107, 79, 0.055) 1px, transparent 1px);
  background-size: 18px 18px;
  box-shadow: 0 18px 40px rgba(31, 92, 69, 0.08);
  animation: rise 0.55s 0.06s ease both;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.segmented {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border-radius: 14px;
  background: var(--mint);
  border: 1px solid var(--mint-deep);
}

.seg {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border: none;
  border-radius: 11px;
  background: transparent;
  color: #48604f;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.seg:hover {
  color: var(--deep);
}

.seg.on {
  background: #ffffff;
  color: var(--deep);
  box-shadow: 0 6px 14px rgba(31, 92, 69, 0.12);
}

.seg.on :deep(svg) {
  color: var(--fern);
}

.icon-btn {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--body-muted);
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.icon-btn:hover {
  color: var(--fern);
  border-color: #bcd4c2;
  transform: rotate(-16deg);
}

/* 输入区 */
.composer {
  margin-top: 16px;
  padding: 14px 14px 12px;
  border-radius: 18px;
  background: #ffffff;
  border: 1px solid #e3ece0;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 10px;
}

.tag {
  padding: 5px 11px;
  border-radius: 999px;
  border: 1px solid #d9e7d3;
  background: #f7fbf3;
  color: #546a5b;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.tag:hover {
  border-color: #b6d2b9;
  color: var(--fern);
}

.tag.on {
  background: var(--deep);
  border-color: var(--deep);
  color: #f2f9ee;
}

.composer textarea {
  width: 100%;
  min-height: 64px;
  padding: 10px 12px;
  resize: vertical;
  border: 1px solid #e3ece0;
  border-radius: 12px;
  background: #fbfdf8;
  color: var(--body-ink);
  font-family: inherit;
  font-size: 13.5px;
  line-height: 1.7;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.composer textarea::placeholder {
  color: #9aab9f;
}

.composer textarea:focus {
  border-color: #9dc5a8;
  box-shadow: 0 0 0 3px rgba(79, 138, 104, 0.12);
}

.composer-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.hint {
  color: var(--body-muted);
  font-size: 11.5px;
}

.generate {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 11px 20px;
  border: none;
  border-radius: 13px;
  background: linear-gradient(135deg, #2c6d51, var(--deep));
  color: #f4f9f0;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.01em;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(31, 92, 69, 0.22);
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;
}

.generate :deep(svg) {
  color: var(--lime);
}

.generate:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(31, 92, 69, 0.26);
}

.generate:disabled {
  opacity: 0.76;
  cursor: progress;
}

/* 空状态 */
.empty-state {
  margin-top: 18px;
  padding: 44px 24px 46px;
  text-align: center;
  border-radius: 18px;
  border: 1px dashed #d8e5d3;
  background: rgba(255, 255, 255, 0.55);
}

.empty-mark {
  width: 66px;
  height: 66px;
  margin: 0 auto 14px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--fern);
  background: var(--mint);
  border: 1px solid var(--mint-deep);
}

.empty-state strong {
  display: block;
  font-family: var(--font-display);
  font-size: 18px;
  color: var(--deep);
}

.empty-state p {
  margin-top: 8px;
  color: var(--body-muted);
  font-size: 12.5px;
}

/* 结果 */
.result {
  margin-top: 18px;
  animation: fade 0.35s ease both;
}

.result-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stamp {
  display: inline-block;
  padding: 4px 10px;
  border: 1px dashed #9fbfa7;
  border-radius: 6px;
  color: var(--fern);
  font-size: 10.5px;
  letter-spacing: 0.14em;
  transform: rotate(-2deg);
}

.result-top time {
  color: #93a396;
  font-size: 11.5px;
  font-variant-numeric: tabular-nums;
}

.plan-title {
  margin-top: 12px;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--body-ink);
}

.profile-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 10px;
  padding: 9px 12px;
  border-radius: 12px;
  background: #f1f8ec;
  border-left: 3px solid #a9cd94;
  color: #4d6456;
  font-size: 12.5px;
  line-height: 1.7;
}

.profile-line :deep(svg) {
  margin-top: 2px;
  color: var(--leaf);
}

.macro-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(104px, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.macro {
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e4ecdf;
}

.macro span {
  display: block;
  color: #8b9b8e;
  font-size: 10.5px;
  letter-spacing: 0.08em;
}

.macro strong {
  display: block;
  margin-top: 2px;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  color: var(--deep);
  font-variant-numeric: tabular-nums;
}

.plan-body {
  margin-top: 18px;
}

.meal {
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #e6eee2;
  box-shadow: 0 6px 16px rgba(31, 92, 69, 0.04);
}

.meal + .meal,
.meal + .section,
.section + .section {
  margin-top: 12px;
}

.meal-head {
  display: flex;
  align-items: center;
  gap: 9px;
}

.meal-no {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--mint);
  color: var(--fern);
  font-size: 10.5px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.meal-head strong {
  font-size: 14.5px;
  color: var(--body-ink);
}

.meal-meta {
  margin-left: auto;
  color: #8fa092;
  font-size: 11.5px;
  font-variant-numeric: tabular-nums;
}

.meal-items {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 10px;
  padding-left: 35px;
}

.food {
  padding: 5px 10px;
  border-radius: 8px;
  background: #f4faf0;
  border: 1px solid #e3efdb;
  color: #45604e;
  font-size: 12.5px;
}

.day-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 11px 4px;
  border-bottom: 1px dashed #e2ebdd;
}

.day-row:last-child {
  border-bottom: none;
}

.day-chip {
  flex-shrink: 0;
  min-width: 46px;
  padding: 3px 0;
  text-align: center;
  border-radius: 7px;
  background: var(--deep);
  color: #f3faee;
  font-size: 11.5px;
  font-weight: 600;
}

.day-row p {
  color: #3c5145;
  font-size: 13px;
  line-height: 1.75;
}

.section {
  margin-top: 14px;
}

.section h4 {
  margin-bottom: 8px;
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--deep);
}

.note-list {
  margin: 0;
  padding-left: 4px;
  list-style: none;
}

.note-list li {
  position: relative;
  padding-left: 16px;
  margin-bottom: 6px;
  color: #4a5d50;
  font-size: 12.5px;
  line-height: 1.75;
}

.note-list li::before {
  content: "";
  position: absolute;
  top: 9px;
  left: 2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #b9d8a5;
}

.source-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.source-list span {
  padding: 4px 9px;
  border-radius: 6px;
  background: #f2f6ee;
  border: 1px dashed #d6e3cf;
  color: #7c8f81;
  font-size: 11px;
}

.plain {
  margin-top: 10px;
  color: #4a5d50;
  font-size: 13px;
  line-height: 1.8;
}

/* 问答 */
.coach-panel {
  position: sticky;
  top: 94px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid #dfe9d9;
  background: linear-gradient(180deg, #f7fcf3 0%, #eef6e8 100%);
  box-shadow: 0 18px 40px rgba(31, 92, 69, 0.08);
  animation: rise 0.55s 0.12s ease both;
}

.coach-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 16px 14px;
  border-bottom: 1px solid #e2ecd9;
}

.coach-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  color: var(--deep);
  background: var(--lime);
  box-shadow: 0 8px 16px rgba(200, 241, 105, 0.32);
}

.coach-id strong {
  display: block;
  font-family: var(--font-display);
  font-size: 15.5px;
  color: var(--deep);
}

.coach-id span {
  font-size: 11px;
  color: #7c8f81;
}

.clear {
  margin-left: auto;
  padding: 4px 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #8a9c8e;
  font-family: inherit;
  font-size: 11px;
  cursor: pointer;
}

.clear:hover {
  color: var(--fern);
  background: rgba(255, 255, 255, 0.7);
}

.chat-scroll {
  flex: 1;
  max-height: 46vh;
  min-height: 260px;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-empty p {
  margin-bottom: 10px;
  color: #7c8f81;
  font-size: 12px;
}

.suggest {
  display: block;
  width: 100%;
  margin-bottom: 8px;
  padding: 10px 12px;
  text-align: left;
  border: 1px dashed #cfe0c6;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.72);
  color: #4c6555;
  font-family: inherit;
  font-size: 12.5px;
  cursor: pointer;
  transition: border-color 0.16s ease, background 0.16s ease, transform 0.16s ease;
}

.suggest:hover {
  border-color: #a9c79c;
  background: #fff;
  transform: translateX(2px);
}

.msg {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 92%;
}

.msg.user {
  align-self: flex-end;
  align-items: flex-end;
}

.msg.coach {
  align-self: flex-start;
}

.msg-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #7a907d;
  font-size: 10.5px;
}

.msg-label :deep(svg) {
  color: var(--leaf);
}

.bubble {
  padding: 10px 12px;
  border-radius: 14px;
  font-size: 12.5px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  border-radius: 14px 14px 4px 14px;
  background: var(--deep);
  color: #f3f9ef;
  box-shadow: 0 8px 18px rgba(31, 92, 69, 0.18);
}

.msg.coach .bubble {
  border-radius: 14px 14px 14px 4px;
  background: #ffffff;
  border: 1px solid #e3ecdc;
  color: #3f5447;
}

.msg-sources {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 8px;
}

.msg-sources span {
  padding: 2px 7px;
  border-radius: 5px;
  background: #f3f8ee;
  border: 1px dashed #d5e3cd;
  color: #7f9483;
  font-size: 10px;
}

.typing {
  display: inline-flex;
  gap: 4px;
  padding: 14px 16px;
}

.typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #a8c19b;
  animation: blink 1.2s infinite ease-in-out;
}

.typing i:nth-child(2) {
  animation-delay: 0.15s;
}

.typing i:nth-child(3) {
  animation-delay: 0.3s;
}

.ask-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e2ecd9;
  background: rgba(255, 255, 255, 0.55);
}

.ask-box input {
  flex: 1;
  min-width: 0;
  padding: 10px 13px;
  border: 1px solid #dbe8d3;
  border-radius: 999px;
  background: #fff;
  color: var(--body-ink);
  font-family: inherit;
  font-size: 12.5px;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.ask-box input::placeholder {
  color: #9cad9f;
}

.ask-box input:focus {
  border-color: #9dc5a8;
  box-shadow: 0 0 0 3px rgba(79, 138, 104, 0.12);
}

.ask-box button {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 50%;
  background: var(--deep);
  color: var(--lime);
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.ask-box button:hover:not(:disabled) {
  transform: translateY(-1px) rotate(-6deg);
}

.ask-box button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* 动效 */
@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero,
  .plan-panel,
  .coach-panel,
  .result,
  .typing i {
    animation: none !important;
  }
}

/* 响应式 */
@media (max-width: 1080px) {
  .board {
    grid-template-columns: 1fr;
  }

  .coach-panel {
    position: static;
  }

  .chat-scroll {
    max-height: 360px;
  }

  .note-card {
    display: none;
  }
}

@media (max-width: 640px) {
  .ai-page {
    overflow-x: clip;
  }

  .ai-page::before {
    left: 0;
    right: 0;
  }

  .hero {
    padding-bottom: 18px;
  }

  .hero h1 {
    font-size: 30px;
  }

  .plan-panel {
    padding: 16px 14px 20px;
    border-radius: 20px;
  }

  .segmented {
    width: 100%;
  }

  .seg {
    flex: 1;
    justify-content: center;
    padding: 9px 8px;
  }

  .composer-foot {
    flex-direction: column;
    align-items: stretch;
  }

  .generate {
    justify-content: center;
  }

  .meal-items {
    padding-left: 0;
  }

  .chat-scroll {
    min-height: 220px;
  }
}
</style>
