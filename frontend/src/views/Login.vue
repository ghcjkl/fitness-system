<template>
  <div class="auth-shell">
    <section class="auth-visual">
      <div class="glow"></div>
      <div class="grid-dots"></div>

      <div class="visual-brand">
        <div class="brand-mark">
          <app-icon name="sport" :size="19" :stroke-width="2.1" />
        </div>
        <span>FitLife</span>
      </div>

      <div class="visual-copy">
        <span class="pill">RAG · 个性化健康助手</span>
        <h1>
          把训练、饮食<br />
          和身体数据<br />
          <span class="accent">放进同一个节奏里。</span>
        </h1>
        <p>记录每一次运动与每一餐，系统根据你的健康档案，给出可执行的训练与饮食建议。</p>

        <ul class="feature-list">
          <li><span class="dot"></span>运动热量按 MET 模型自动换算</li>
          <li><span class="dot"></span>每日营养目标与餐食推荐</li>
          <li><span class="dot"></span>AI 结合健康档案回答你的问题</li>
        </ul>
      </div>

      <div class="visual-meta">
        <div><strong>16</strong><span>项运动热量模型</span></div>
        <div><strong>40+</strong><span>种食材营养库</span></div>
        <div><strong>7 / 30 / 90</strong><span>天趋势追踪</span></div>
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-card">
        <div class="auth-head">
          <h2>欢迎回来</h2>
          <p>登录后继续记录今天的进度</p>
        </div>

        <el-form :model="form" label-position="top" class="auth-form" @keyup.enter="handleLogin">
          <el-form-item label="用户名">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              show-password
              placeholder="请输入密码"
            />
          </el-form-item>
          <el-button class="submit" :loading="loading" @click="handleLogin">
            登录
            <app-icon name="arrow" :size="17" class="submit-arrow" />
          </el-button>
        </el-form>

        <div class="auth-foot">
          <span>还没有账号？</span>
          <button type="button" class="text-link" @click="showRegister = true">立即注册</button>
        </div>
      </div>
    </section>
  </div>

  <el-dialog v-model="showRegister" title="注册账号" width="420px" append-to-body>
    <el-form :model="regForm" label-position="top" class="auth-form">
      <el-form-item label="用户名">
        <el-input v-model="regForm.username" size="large" placeholder="3-16 位" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="regForm.nickname" size="large" placeholder="选填" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="regForm.password"
          size="large"
          type="password"
          show-password
          placeholder="6-20 位"
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="regForm.rePassword"
          size="large"
          type="password"
          show-password
          placeholder="再次输入密码"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showRegister = false">取消</el-button>
      <el-button type="primary" :loading="regLoading" @click="handleRegister">注册</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/user'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const loading = ref(false)
const regLoading = ref(false)
const showRegister = ref(false)
const form = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', nickname: '', password: '', rePassword: '' })

const handleLogin = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const res = await login(form)
    localStorage.setItem('token', res.data)
    ElMessage.success(res.msg)
    router.push('/home')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!regForm.username || !regForm.password || !regForm.rePassword) {
    return ElMessage.warning('请填写用户名和密码')
  }
  if (regForm.username.length < 3 || regForm.username.length > 16) {
    return ElMessage.warning('用户名需为3-16位')
  }
  if (regForm.password.length < 6 || regForm.password.length > 20) {
    return ElMessage.warning('密码需为6-20位')
  }
  if (regForm.password !== regForm.rePassword) {
    return ElMessage.warning('两次输入的密码不一致')
  }
  regLoading.value = true
  try {
    const res = await register(regForm)
    ElMessage.success(res.msg || '注册成功')
    form.username = regForm.username
    form.password = ''
    showRegister.value = false
    regForm.username = regForm.nickname = regForm.password = regForm.rePassword = ''
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 0.95fr);
  background: #fbfdf8;
}

.auth-visual {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 44px 56px 48px;
  color: #f4f8ee;
  background: radial-gradient(120% 90% at 85% 8%, #2e7d5b 0%, #1c4a38 46%, #10281f 100%);
}

.glow {
  position: absolute;
  top: -180px;
  right: -160px;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(200, 241, 105, 0.5), transparent 66%);
  pointer-events: none;
}

.grid-dots {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.55;
  background-image: radial-gradient(rgba(255, 255, 255, 0.16) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: linear-gradient(160deg, transparent 20%, #000 70%);
}

.visual-brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: var(--brand);
  background: var(--lime);
  box-shadow: 0 10px 22px rgba(200, 241, 105, 0.28);
}

.visual-copy {
  position: relative;
  margin: auto 0;
  padding: 40px 0;
  max-width: 560px;
}

.pill {
  display: inline-block;
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid rgba(200, 241, 105, 0.35);
  background: rgba(200, 241, 105, 0.12);
  color: var(--lime);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.visual-copy h1 {
  margin: 22px 0 18px;
  font-size: clamp(32px, 3.6vw, 50px);
  font-weight: 800;
  line-height: 1.22;
  letter-spacing: -0.025em;
}

.visual-copy h1 .accent {
  color: var(--lime);
}

.visual-copy p {
  max-width: 460px;
  font-size: 14px;
  line-height: 1.85;
  color: rgba(244, 248, 238, 0.72);
}

.feature-list {
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13.5px;
  color: rgba(244, 248, 238, 0.9);
}

.feature-list .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--lime);
  box-shadow: 0 0 0 4px rgba(200, 241, 105, 0.16);
}

.visual-meta {
  position: relative;
  display: flex;
  gap: 34px;
  padding-top: 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
}

.visual-meta div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.visual-meta strong {
  font-size: 20px;
  letter-spacing: -0.02em;
}

.visual-meta span {
  font-size: 11.5px;
  color: rgba(244, 248, 238, 0.58);
}

.auth-panel {
  display: grid;
  place-items: center;
  padding: 40px 32px;
  background: linear-gradient(160deg, #f7fbf3 0%, #fcfdf9 52%, #ffffff 100%);
}

.auth-card {
  width: 100%;
  max-width: 380px;
}

.auth-head h2 {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.auth-head p {
  margin-top: 8px;
  font-size: 13.5px;
  color: var(--muted);
}

.auth-form {
  margin-top: 28px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-form-item__label) {
  padding-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-soft);
}

.auth-form :deep(.el-input__wrapper) {
  height: 46px;
  padding: 0 14px;
  background: #f6f8f5;
  box-shadow: inset 0 0 0 1px #e6ebe6;
  border-radius: 12px;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: inset 0 0 0 2px var(--brand-2);
}

.submit {
  width: 100%;
  height: 48px;
  margin-top: 6px;
  border: none;
  border-radius: 12px;
  color: #ffffff;
  font-size: 15px;
  letter-spacing: 0.02em;
  background: linear-gradient(135deg, #2e7d5b, #14352a);
  box-shadow: 0 12px 24px rgba(20, 53, 42, 0.22);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.submit:hover {
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 16px 28px rgba(20, 53, 42, 0.28);
  background: linear-gradient(135deg, #2e7d5b, #14352a);
}

.submit-arrow {
  margin-left: 8px;
  display: inline-block;
  vertical-align: -3px;
}

.auth-foot {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 22px;
  font-size: 13px;
  color: var(--muted);
}

.text-link {
  border: none;
  background: none;
  padding: 0;
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  color: var(--brand-2);
  cursor: pointer;
}

.text-link:hover {
  color: var(--brand);
  text-decoration: underline;
}

@media (max-width: 960px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-visual {
    display: none;
  }

  .auth-panel {
    padding: 32px 22px;
    min-height: 100vh;
    background: linear-gradient(180deg, #f2f7ec 0%, #ffffff 42%);
  }
}
</style>
