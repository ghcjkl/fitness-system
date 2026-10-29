<template>
  <div class="health-page">
    <!-- 身体概览 -->
    <section class="profile-hero">
      <div class="ring">
        <svg viewBox="0 0 120 120" class="ring-svg" aria-hidden="true">
          <circle cx="60" cy="60" r="52" class="ring-track" />
          <circle cx="60" cy="60" r="52" class="ring-progress" :stroke="status.color"
            :stroke-dasharray="CIRCUM" :stroke-dashoffset="ringOffset" />
        </svg>
        <div class="ring-center">
          <strong>{{ hasBasic ? bmi.toFixed(1) : '--' }}</strong>
          <span class="ring-label">BMI</span>
          <el-tooltip placement="right" effect="dark" :show-after="120">
            <template #content>
              <div class="bmi-tip">
                <div class="bmi-tip-title">BMI · 体质指数</div>
                <p>用体重（kg）除以身高（m）的平方，用来粗略判断体重是否处于健康区间。</p>
                <p>它不区分肌肉与脂肪，健身增肌人群建议结合体脂率一起看。</p>
              </div>
            </template>
            <button type="button" class="info-btn" aria-label="什么是 BMI">i</button>
          </el-tooltip>
        </div>
      </div>

      <div class="hero-body">
        <div class="hero-top">
          <span class="hero-kicker">Body Snapshot</span>
          <span class="status-chip" :style="{
            color: status.color,
            borderColor: status.color + '55',
            background: status.color + '14'
          }">{{ status.label }}</span>
        </div>
        <h1 class="hero-title">身体概览</h1>
        <p class="hero-tip">{{ status.tip }}</p>

        <div class="metrics">
          <div class="metric">
            <span>体脂率</span>
            <strong>{{ form.bodyFat ? form.bodyFat + '%' : '--' }}</strong>
            <em>{{ bodyFatLevel }}</em>
          </div>
          <div class="metric">
            <span>基础代谢</span>
            <strong>{{ bmr ? Math.round(bmr) : '--' }}<i>kcal</i></strong>
            <em>{{ bmr ? '静息状态' : '待填写' }}</em>
          </div>
          <div class="metric">
            <span>建议热量</span>
            <strong>{{ dailyCalories || '--' }}<i>kcal</i></strong>
            <em>{{ form.target || '待选择' }}</em>
          </div>
          <div class="metric">
            <span>理想体重</span>
            <strong>{{ idealWeight }}</strong>
            <em>BMI 18.5–23.9</em>
          </div>
        </div>
      </div>
    </section>

    <!-- 档案表单 -->
    <div class="panel-grid">
      <div class="panel-col">
        <section class="panel" ref="basicPanel">
          <header class="panel-head">
            <span class="panel-icon"><app-icon name="health" :size="16" /></span>
            <div>
              <div class="panel-title">基础信息</div>
              <div class="panel-sub">用于计算 BMI、代谢与营养目标</div>
            </div>
          </header>

          <el-form :model="form" label-position="top" class="panel-form">
            <div class="field-grid">
              <el-form-item label="性别">
                <div class="seg">
                  <button type="button" class="seg-item" :class="{ active: form.gender === 1 }"
                    @click="form.gender = 1">男</button>
                  <button type="button" class="seg-item" :class="{ active: form.gender === 2 }"
                    @click="form.gender = 2">女</button>
                </div>
              </el-form-item>
              <el-form-item label="年龄（岁）">
                <el-input-number v-model="form.age" :min="1" :max="150" :controls="false"
                  placeholder="如 24" style="width:100%" />
              </el-form-item>
              <el-form-item label="身高（cm）">
                <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" :step="0.5"
                  :controls="false" placeholder="如 175.0" style="width:100%" />
              </el-form-item>
              <el-form-item label="体重（kg）">
                <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" :step="0.5"
                  :controls="false" placeholder="如 68.5" style="width:100%" />
              </el-form-item>
              <el-form-item class="span-2">
                <template #label>
                  <div class="label-row">
                    <span>体脂率（%）</span>
                    <button type="button" class="link-btn" @click="estimateBodyFat">
                      按身高体重估算
                    </button>
                  </div>
                </template>
                <el-input-number v-model="form.bodyFat" :min="0" :max="70" :precision="1" :step="0.1"
                  :controls="false" placeholder="如 18.5" style="width:100%" />
                <p class="field-hint">可直接填写，也可点击右上角估算；估算值仅供参考</p>
              </el-form-item>
            </div>
          </el-form>
        </section>

        <section class="panel complete-panel">
          <header class="panel-head">
            <span class="panel-icon"><app-icon name="file" :size="16" /></span>
            <div>
              <div class="panel-title">档案完善度</div>
              <div class="panel-sub">信息越全，建议越贴合</div>
            </div>
          </header>

          <div class="complete-body">
            <div class="mini-ring">
              <svg viewBox="0 0 52 52" aria-hidden="true">
                <circle cx="26" cy="26" r="20" class="mini-track" />
                <circle cx="26" cy="26" r="20" class="mini-progress"
                  :stroke-dasharray="MINI" :stroke-dashoffset="miniOffset" />
              </svg>
              <span>{{ completeRate }}%</span>
            </div>
            <div class="complete-text">
              <strong>{{ completeRate === 100 ? '核心指标已完整' : `核心指标 ${coreDone}/4` }}</strong>
              <p v-if="completeRate < 100" class="complete-missing">
                待补全：
                <button type="button" class="chip chip-sm" @click="goFill('basic')">
                  {{ missingCore.join('、') }}
                </button>
              </p>
              <p v-else>保持记录，趋势会更有参考价值</p>
            </div>
          </div>

          <div class="constraint-block">
            <span class="constraint-title">健康约束 {{ 4 - missingConstraints.length }}/4</span>
            <div class="chips">
              <button v-for="item in missingConstraints" :key="item.label" type="button" class="chip"
                @click="goFill('constraint')">+ {{ item.label }}</button>
              <span v-if="!missingConstraints.length" class="chips-done">已全部记录</span>
            </div>
          </div>
          <p class="complete-note">无相关情况可留空，填写后 AI 方案会规避对应风险</p>
        </section>
      </div>

      <div class="panel-col">
        <section class="panel">
          <header class="panel-head">
            <span class="panel-icon"><app-icon name="flame" :size="16" /></span>
            <div>
              <div class="panel-title">健康目标</div>
              <div class="panel-sub">决定热量缺口与蛋白质配比</div>
            </div>
          </header>

          <div class="target-grid">
            <button v-for="t in targets" :key="t.value" type="button" class="target-item"
              :class="{ active: form.target === t.value }" @click="form.target = t.value">
              <span class="target-name">{{ t.value }}</span>
              <span class="target-desc">{{ t.desc }}</span>
            </button>
          </div>

          <div class="derived">
            <div class="derived-item">
              <span>每日热量</span>
              <strong>{{ dailyCalories || '--' }} <i>kcal</i></strong>
            </div>
            <div class="derived-item">
              <span>蛋白质</span>
              <strong>{{ proteinTarget || '--' }} <i>g</i></strong>
            </div>
          </div>
        </section>

        <section class="panel" ref="constraintPanel">
          <header class="panel-head">
            <span class="panel-icon"><app-icon name="leaf" :size="16" /></span>
            <div>
              <div class="panel-title">健康约束</div>
              <div class="panel-sub">影响 AI 方案的安全建议</div>
            </div>
          </header>

          <el-form :model="form" label-position="top" class="panel-form">
            <div class="field-grid">
              <el-form-item label="基础疾病" class="span-2">
                <el-input v-model="form.disease" placeholder="如：高血压、糖尿病，无则留空" clearable />
              </el-form-item>
              <el-form-item label="饮食禁忌">
                <el-input v-model="form.dietAvoid" placeholder="如：不吃辣" clearable />
              </el-form-item>
              <el-form-item label="过敏食材">
                <el-input v-model="form.allergy" placeholder="如：海鲜、花生" clearable />
              </el-form-item>
              <el-form-item label="运动旧伤" class="span-2">
                <el-input v-model="form.oldInjury" placeholder="如：右膝半月板损伤，无则留空" clearable />
              </el-form-item>
            </div>
          </el-form>
        </section>
      </div>
    </div>

    <!-- 保存 -->
    <div class="save-bar">
      <div class="save-note">
        <app-icon name="refresh" :size="15" />
        <span>保存后立即用于营养目标与 AI 方案生成</span>
      </div>
      <el-button type="primary" round :loading="loading" class="save-btn" @click="handleSave">
        保存档案
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealth, saveHealth } from '../api/health'
import AppIcon from '../components/AppIcon.vue'

const CIRCUM = 2 * Math.PI * 52
const MINI = 2 * Math.PI * 20
const loading = ref(false)
const basicPanel = ref(null)
const constraintPanel = ref(null)

const form = reactive({
  age: null, height: null, weight: null, bodyFat: null,
  gender: 1, disease: '', target: '减脂', dietAvoid: '', allergy: '', oldInjury: ''
})

const targets = [
  { value: '减脂', desc: '控制热量缺口' },
  { value: '增肌', desc: '盈余 + 高蛋白' },
  { value: '塑形', desc: '轻度缺口塑线条' },
  { value: '养生', desc: '维持与恢复' }
]

const hasBasic = computed(() => !!(form.age && form.height && form.weight))
const bmi = computed(() => hasBasic.value
  ? form.weight / Math.pow(form.height / 100, 2)
  : 0)

const status = computed(() => {
  if (!hasBasic.value) {
    return {
      label: '待填写',
      color: '#9db3a3',
      tip: '补全身高、体重与年龄后，这里会自动生成你的身体概览。'
    }
  }
  const b = bmi.value
  if (b < 18.5) {
    return { label: '偏瘦', color: '#8fd3a8', tip: '体重略低于健康区间，可适当增加主食与优质蛋白摄入。' }
  }
  if (b < 24) {
    return { label: '标准', color: '#c8f169', tip: '体重处于健康区间，保持当前训练与饮食节奏即可。' }
  }
  if (b < 28) {
    return { label: '偏重', color: '#e0b25a', tip: '体重略高于健康区间，建议提高有氧频率并减少精制碳水。' }
  }
  return { label: '肥胖', color: '#e08170', tip: '体重明显超出健康区间，建议循序减重，必要时咨询专业人士。' }
})

const bmr = computed(() => {
  if (!hasBasic.value) return 0
  const base = 10 * form.weight + 6.25 * form.height - 5 * form.age
  return form.gender === 1 ? base + 5 : base - 161
})

const dailyCalories = computed(() => {
  if (!bmr.value) return 0
  const adjust = { 减脂: -350, 增肌: 300, 塑形: -150, 养生: 0 }[form.target] ?? 0
  return Math.max(1200, Math.round(bmr.value * 1.375 + adjust))
})

const proteinTarget = computed(() => {
  if (!hasBasic.value) return 0
  const ratio = { 减脂: 1.6, 增肌: 1.8, 塑形: 1.5, 养生: 1.2 }[form.target] ?? 1.4
  return Math.round(form.weight * ratio)
})

const bodyFatLevel = computed(() => {
  if (!form.bodyFat) return '未填写'
  const male = [
    [10, '偏低'], [20, '标准'], [25, '偏高'], [Infinity, '过高']
  ]
  const female = [
    [18, '偏低'], [28, '标准'], [35, '偏高'], [Infinity, '过高']
  ]
  const table = form.gender === 1 ? male : female
  return table.find(([max]) => form.bodyFat < max)[1]
})

const idealWeight = computed(() => {
  if (!form.height) return '--'
  const h = Math.pow(form.height / 100, 2)
  return `${(18.5 * h).toFixed(1)}–${(23.9 * h).toFixed(1)} kg`
})

const ringOffset = computed(() => {
  const progress = hasBasic.value
    ? Math.min(1, Math.max(0.04, bmi.value / 32))
    : 0.015
  return CIRCUM * (1 - progress)
})

// 完善度：核心指标决定百分比，健康约束作为可补充项
const coreFields = computed(() => [
  { label: '年龄', done: !!form.age },
  { label: '身高', done: !!form.height },
  { label: '体重', done: !!form.weight },
  { label: '体脂率', done: !!form.bodyFat }
])
const coreDone = computed(() => coreFields.value.filter(item => item.done).length)
const completeRate = computed(() => Math.round((coreDone.value / 4) * 100))
const miniOffset = computed(() => MINI * (1 - completeRate.value / 100))
const missingCore = computed(() => coreFields.value
  .filter(item => !item.done)
  .map(item => item.label))

const constraintFields = computed(() => [
  { label: '基础疾病', done: !!form.disease },
  { label: '饮食禁忌', done: !!form.dietAvoid },
  { label: '过敏食材', done: !!form.allergy },
  { label: '运动旧伤', done: !!form.oldInjury }
])
const missingConstraints = computed(() => constraintFields.value.filter(item => !item.done))

const goFill = (panel = 'constraint') => {
  const el = panel === 'basic' ? basicPanel.value : constraintPanel.value
  el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// Deurenberg 公式：体脂率 ≈ 1.2×BMI + 0.23×年龄 − 10.8×性别(男 1 / 女 0) − 5.4
const estimateBodyFat = () => {
  if (!hasBasic.value) return ElMessage.warning('请先填写年龄、身高、体重')
  const value = 1.2 * bmi.value + 0.23 * form.age - (form.gender === 1 ? 10.8 : 0) - 5.4
  form.bodyFat = Number(Math.min(60, Math.max(3, value)).toFixed(1))
  ElMessage.success('已按身高体重估算，可手动微调')
}

const load = async () => {
  try {
    const res = await getHealth()
    if (res.data) Object.assign(form, res.data)
  } catch (e) { /* 无档案属正常 */ }
}

const handleSave = async () => {
  if (!form.age || !form.height || !form.weight) return ElMessage.warning('请填写年龄、身高、体重')
  loading.value = true
  try {
    const res = await saveHealth(form)
    ElMessage.success(res.msg)
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.health-page {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 身体概览 */
.profile-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 168px minmax(0, 1fr);
  gap: 26px;
  padding: 26px 30px;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.07);
  background: linear-gradient(152deg, #1d4937 0%, #163c2e 54%, #0f281e 100%);
  box-shadow: 0 22px 44px rgba(16, 40, 31, 0.26);
  color: #f4f8ee;
  animation: rise-in 0.55s cubic-bezier(0.22, 0.9, 0.28, 1) both;
}

.profile-hero::before {
  content: "";
  position: absolute;
  top: -110px;
  right: -60px;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(200, 241, 105, 0.22), transparent 66%);
  pointer-events: none;
}

.ring {
  position: relative;
  width: 150px;
  height: 150px;
  align-self: center;
  justify-self: center;
  z-index: 1;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-track {
  fill: none;
  stroke: rgba(255, 255, 255, 0.1);
  stroke-width: 9;
}

.ring-progress {
  fill: none;
  stroke-width: 9;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.9s cubic-bezier(0.22, 0.9, 0.28, 1), stroke 0.4s ease;
}

.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.ring-center strong {
  font-size: 34px;
  line-height: 1;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
  color: #f8fcf2;
}

.ring-label {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.2em;
  color: rgba(244, 248, 238, 0.52);
}

.info-btn {
  margin-top: 3px;
  width: 15px;
  height: 15px;
  padding: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid rgba(200, 241, 105, 0.5);
  background: rgba(200, 241, 105, 0.12);
  color: rgba(200, 241, 105, 0.9);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 9.5px;
  font-style: italic;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 0;
  cursor: help;
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.info-btn:hover {
  background: var(--lime);
  color: var(--brand);
  transform: scale(1.1);
}

.bmi-tip {
  width: 226px;
  text-align: left;
}

.bmi-tip-title {
  margin-bottom: 5px;
  font-size: 12.5px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.bmi-tip p {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: rgba(244, 248, 238, 0.74);
}

.bmi-tip p + p {
  margin-top: 5px;
}

.hero-body {
  min-width: 0;
  z-index: 1;
}

.hero-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hero-kicker {
  font-size: 10.5px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(200, 241, 105, 0.72);
}

.status-chip {
  padding: 3px 11px;
  border-radius: 999px;
  border: 1px solid;
  font-size: 11.5px;
  font-weight: 600;
  white-space: nowrap;
}

.hero-title {
  margin-top: 7px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.hero-tip {
  margin-top: 7px;
  max-width: 54ch;
  font-size: 12.5px;
  line-height: 1.65;
  color: rgba(244, 248, 238, 0.62);
}

.metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.metric {
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.07);
}

.metric > span {
  font-size: 11px;
  letter-spacing: 0.04em;
  color: rgba(244, 248, 238, 0.55);
}

.metric strong {
  display: block;
  margin-top: 4px;
  font-size: 19px;
  font-weight: 700;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.metric strong i {
  margin-left: 3px;
  font-style: normal;
  font-size: 10.5px;
  font-weight: 500;
  color: rgba(244, 248, 238, 0.45);
}

.metric em {
  display: block;
  margin-top: 2px;
  font-style: normal;
  font-size: 10.5px;
  color: rgba(244, 248, 238, 0.42);
}

/* 分段面板 */
.panel-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  gap: 16px;
}

.panel-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

/* 左列最后一张卡撑满剩余高度，与右列底部对齐 */
.panel-col > .panel:last-child {
  flex: 1 1 auto;
}

.panel {
  padding: 18px 20px 8px;
  border-radius: 20px;
  border: 1px solid rgba(31, 92, 69, 0.08);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  animation: rise-in 0.55s cubic-bezier(0.22, 0.9, 0.28, 1) both;
  animation-delay: 0.06s;
}

.panel-col .panel:nth-child(1) {
  animation-delay: 0.11s;
}

.panel-col .panel:nth-child(2) {
  animation-delay: 0.16s;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.panel-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: var(--brand-2);
  background: var(--mint);
}

.panel-title {
  font-size: 13.5px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--ink);
}

.panel-sub {
  font-size: 11.5px;
  color: var(--muted);
}

.panel-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.panel-form :deep(.el-form-item__label) {
  width: 100%;
  padding-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--muted);
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 14px;
}

.span-2 {
  grid-column: span 2;
}

.field-hint {
  margin-top: 6px;
  font-size: 11px;
  line-height: 1.5;
  color: var(--muted);
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
}

.link-btn {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--brand-3);
  font-family: inherit;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0;
  cursor: pointer;
  transition: color 0.18s ease;
}

.link-btn:hover {
  color: var(--brand-2);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.seg {
  display: flex;
  gap: 8px;
  width: 100%;
}

.seg-item {
  flex: 1;
  padding: 7px 0;
  border-radius: 11px;
  border: 1px solid var(--line);
  background: var(--paper);
  color: var(--ink-soft);
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.seg-item.active {
  border-color: var(--brand-2);
  background: var(--brand-2);
  color: #f4f8ee;
  box-shadow: 0 6px 14px rgba(31, 92, 69, 0.2);
}

/* 目标选择 */
.target-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.target-item {
  padding: 10px 13px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: var(--paper);
  text-align: left;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.target-item:hover {
  transform: translateY(-1px);
  border-color: var(--mint-deep);
}

.target-item.active {
  border-color: var(--brand-2);
  background: linear-gradient(158deg, #eef7e6, #e2f0d6);
  box-shadow: 0 8px 18px rgba(31, 92, 69, 0.1);
}

.target-name {
  display: block;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--ink);
}

.target-desc {
  display: block;
  margin-top: 2px;
  font-size: 11px;
  color: var(--muted);
}

.derived {
  display: flex;
  gap: 10px;
  margin: 14px 0 12px;
  padding-top: 14px;
  border-top: 1px dashed var(--line);
}

.derived-item {
  flex: 1;
  padding: 10px 12px;
  border-radius: 14px;
  background: var(--mint);
}

.derived-item span {
  font-size: 11px;
  color: var(--sage);
}

.derived-item strong {
  display: block;
  margin-top: 2px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--brand);
  font-variant-numeric: tabular-nums;
}

.derived-item strong i {
  margin-left: 2px;
  font-style: normal;
  font-size: 10.5px;
  font-weight: 500;
  color: var(--sage);
}

/* 档案完善度 */
.complete-body {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mini-ring {
  position: relative;
  width: 58px;
  height: 58px;
  flex: 0 0 auto;
}

.mini-ring svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.mini-track {
  fill: none;
  stroke: #e9f2e5;
  stroke-width: 5;
}

.mini-progress {
  fill: none;
  stroke: var(--brand-3);
  stroke-width: 5;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.8s cubic-bezier(0.22, 0.9, 0.28, 1);
}

.mini-ring span {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--brand);
  font-variant-numeric: tabular-nums;
}

.complete-text strong {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
}

.complete-text p {
  margin-top: 3px;
  font-size: 11.5px;
  line-height: 1.6;
  color: var(--muted);
}

.complete-missing {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.constraint-block {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--line);
}

.constraint-title {
  font-size: 11.5px;
  color: var(--muted);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  padding: 3px 9px;
  border-radius: 999px;
  border: 1px dashed var(--mint-deep);
  background: var(--paper);
  color: var(--brand-3);
  font-family: inherit;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.chip:hover {
  border-style: solid;
  border-color: var(--brand-3);
  background: var(--mint);
  color: var(--brand-2);
}

.chip-sm {
  padding: 1px 8px;
  font-size: 10.5px;
}

.chips-done {
  font-size: 11px;
  color: var(--sage);
}

.complete-panel {
  display: flex;
  flex-direction: column;
}

.complete-note {
  margin: 9px 0 12px;
  font-size: 10.5px;
  line-height: 1.5;
  color: var(--muted);
}

/* 卡片被拉伸时，说明文字贴住底部，避免底部出现突兀留白 */
.complete-panel .complete-note {
  margin-top: auto;
}

/* 保存栏 */
.save-bar {
  position: sticky;
  bottom: 14px;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 13px 20px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: rgba(252, 253, 249, 0.88);
  box-shadow: 0 14px 34px rgba(20, 53, 42, 0.1);
  backdrop-filter: blur(16px) saturate(1.3);
}

.save-note {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--muted);
}

.save-note :deep(svg) {
  color: var(--sage);
}

.save-btn {
  min-width: 148px;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .profile-hero,
  .panel {
    animation: none;
  }

  .ring-progress {
    transition: none;
  }
}

@media (max-width: 1024px) {
  .panel-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .profile-hero {
    grid-template-columns: minmax(0, 1fr);
    gap: 16px;
    padding: 20px 18px;
  }

  .ring {
    width: 118px;
    height: 118px;
  }

  .ring-center strong {
    font-size: 28px;
  }

  .hero-title {
    font-size: 19px;
  }

  .hero-top {
    flex-wrap: wrap;
    gap: 8px;
  }

  .metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
    margin-top: 14px;
  }

  .metric strong {
    font-size: 17px;
  }

  .field-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .span-2 {
    grid-column: span 1;
  }

  .save-bar {
    position: static;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 13px 16px;
  }

  .save-btn {
    width: 100%;
  }
}
</style>
