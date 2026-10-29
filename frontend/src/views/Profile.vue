<template>
  <div class="profile-page">
    <span class="aura a1" aria-hidden="true"></span>
    <span class="aura a2" aria-hidden="true"></span>
    <span class="aura a3" aria-hidden="true"></span>

    <!-- 身份卡 -->
    <section class="glass glass-hero">
      <div class="hero-left">
        <el-upload class="avatar-upload" :show-file-list="false" :http-request="doUploadAvatar" accept="image/*">
          <div class="avatar-shell">
            <span class="avatar-halo" aria-hidden="true"></span>
            <el-avatar :size="94" :src="avatarUrl" class="avatar">
              {{ username ? username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <span class="avatar-badge">
              <app-icon name="image" :size="14" />
            </span>
          </div>
        </el-upload>

        <div class="hero-id">
          <span class="kicker">Health Profile</span>
          <h1 class="hero-name">{{ pf.nickname || username || '健身用户' }}</h1>
          <div class="hero-chips">
            <span class="chip">@{{ username || '未登录' }}</span>
            <span class="chip soft">绿色健身计划</span>
          </div>
          <span class="hero-tip">点击头像更换</span>
        </div>
      </div>

      <span class="hero-mark" aria-hidden="true">
        <app-icon name="leaf" :size="26" />
      </span>
    </section>

    <!-- 设置区 -->
    <div class="glass-grid">
      <section class="glass glass-panel">
        <header class="panel-head">
          <span class="panel-icon"><app-icon name="profile" :size="17" /></span>
          <div>
            <h2 class="panel-title">个人资料</h2>
            <p class="panel-sub">头像和昵称会展示在社区动态里</p>
          </div>
        </header>

        <el-form :model="pf" label-position="top" class="panel-form">
          <el-form-item label="昵称">
            <el-input v-model="pf.nickname" placeholder="设置一个展示给其他用户的昵称" />
          </el-form-item>
          <el-form-item label="登录账号">
            <el-input :model-value="username" disabled placeholder="-" />
          </el-form-item>
        </el-form>

        <div class="panel-actions">
          <el-button type="primary" round class="act-btn" :loading="pfLoading" @click="saveProfile">
            保存信息
            <app-icon name="arrow" :size="14" class="act-icon" />
          </el-button>
        </div>
      </section>

      <section class="glass glass-panel">
        <header class="panel-head">
          <span class="panel-icon"><app-icon name="lock" :size="17" /></span>
          <div>
            <h2 class="panel-title">账号安全</h2>
            <p class="panel-sub">定期更换密码，保护账号与健康数据</p>
          </div>
        </header>

        <el-form :model="pw" label-position="top" class="panel-form">
          <el-form-item label="原密码">
            <el-input v-model="pw.oldPassword" type="password" show-password placeholder="当前密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pw.newPassword" type="password" show-password placeholder="新密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="pw.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
        </el-form>

        <div class="panel-actions">
          <el-button type="primary" round class="act-btn" :loading="pwLoading" @click="savePassword">
            修改密码
            <app-icon name="arrow" :size="14" class="act-icon" />
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateProfile, updatePassword, uploadAvatar } from '../api/user'

const username = ref('')
const avatarUrl = ref('')
const pfLoading = ref(false)
const pwLoading = ref(false)
const pf = reactive({ nickname: '' })
const pw = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })


const doUploadAvatar = async (opt) => {
  try {
    await uploadAvatar(opt.file)
    await load()
    ElMessage.success('头像更换成功')
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  }
}

const load = async () => {
  try {
    const res = await getUserInfo()
    const u = res.data
    username.value = u.username
    pf.nickname = u.nickname || ''
    avatarUrl.value = u.avatar || ''
  } catch (e) { /* ignore */ }
}

const saveProfile = async () => {
  pfLoading.value = true
  try {
    const res = await updateProfile(pf)
    ElMessage.success(res.msg)
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    pfLoading.value = false
  }
}

const savePassword = async () => {
  if (!pw.oldPassword || !pw.newPassword || !pw.confirmPassword) return ElMessage.warning('请填写完整')
  pwLoading.value = true
  try {
    const res = await updatePassword(pw)
    ElMessage.success(res.msg)
    pw.oldPassword = pw.newPassword = pw.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    pwLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile-page {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 18px;
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 28px 34px;
  border-radius: 30px;
  background:
    radial-gradient(58% 52% at 8% 4%, #ddf2c4 0%, transparent 62%),
    radial-gradient(48% 44% at 96% 10%, #cdeade 0%, transparent 64%),
    radial-gradient(62% 56% at 80% 96%, #e9f7d3 0%, transparent 62%),
    linear-gradient(158deg, #f6fbf2 0%, #ecf6e6 52%, #e3f0e2 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.6);
}

/* 背景光斑 */
.aura {
  position: absolute;
  z-index: 0;
  border-radius: 50%;
  filter: blur(58px);
  opacity: 0.62;
  pointer-events: none;
  animation: drift 19s ease-in-out infinite alternate;
}

.a1 {
  top: -110px;
  left: -90px;
  width: 320px;
  height: 320px;
  background: #d5f2a2;
}

.a2 {
  top: 30px;
  right: -80px;
  width: 280px;
  height: 280px;
  background: #bfe8d8;
  animation-delay: -6s;
}

.a3 {
  right: 80px;
  bottom: -150px;
  width: 360px;
  height: 360px;
  background: #e4f6c1;
  animation-delay: -12s;
}

/* 毛玻璃基底 */
.glass {
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.68);
  background: linear-gradient(148deg, rgba(255, 255, 255, 0.62), rgba(255, 255, 255, 0.4));
  backdrop-filter: blur(14px) saturate(150%);
  -webkit-backdrop-filter: blur(14px) saturate(150%);
  box-shadow:
    0 16px 36px rgba(20, 53, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.86);
}

/* 身份卡 */
.glass-hero {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 24px 26px;
  border-radius: 26px;
  animation: rise 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 22px;
  min-width: 0;
}

.avatar-upload {
  flex: 0 0 auto;
}

.avatar-shell {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

/* 头像柔光 */
.avatar-halo {
  position: absolute;
  inset: -12px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(200, 241, 105, 0.45), transparent 68%);
  filter: blur(8px);
  animation: breathe 5.5s ease-in-out infinite;
  pointer-events: none;
}

.avatar {
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.55);
  color: var(--brand-2);
  font-size: 34px;
  font-weight: 600;
  box-shadow:
    0 10px 24px rgba(20, 53, 42, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.85);
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.avatar-shell:hover .avatar {
  transform: scale(1.03);
}

.avatar-badge {
  position: absolute;
  right: -4px;
  bottom: -4px;
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  background: linear-gradient(140deg, #e9f9c4, #c8f169);
  color: var(--brand);
  box-shadow: 0 6px 14px rgba(20, 53, 42, 0.2);
  transition: transform 0.22s ease;
}

.avatar-shell:hover .avatar-badge {
  transform: rotate(-8deg) scale(1.06);
}

.hero-id {
  min-width: 0;
}

.kicker {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: var(--brand-3);
  opacity: 0.72;
}

.hero-name {
  margin: 7px 0 0;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.02em;
  color: var(--ink);
  word-break: break-word;
}

.hero-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 11px;
}

.chip {
  padding: 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  font-size: 11.5px;
  color: var(--brand-2);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.chip.soft {
  background: rgba(200, 241, 105, 0.18);
  border-color: rgba(200, 241, 105, 0.45);
  color: #3f6d38;
}

.hero-tip {
  display: inline-block;
  margin-top: 9px;
  font-size: 10.5px;
  color: var(--muted);
}

.hero-mark {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 62px;
  height: 62px;
  margin-left: auto;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.6), rgba(200, 241, 105, 0.28));
  color: var(--brand-3);
  box-shadow:
    0 12px 26px rgba(20, 53, 42, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

/* 设置卡 */
.glass-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.glass-panel {
  display: flex;
  flex-direction: column;
  padding: 22px 24px 20px;
  border-radius: 24px;
  animation: rise 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.glass-panel:nth-child(1) {
  animation-delay: 0.08s;
}

.glass-panel:nth-child(2) {
  animation-delay: 0.16s;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.panel-icon {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.85), rgba(233, 244, 228, 0.55));
  color: var(--brand-3);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.95);
}

.panel-title {
  margin: 1px 0 0;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--ink);
}

.panel-sub {
  margin: 4px 0 0;
  font-size: 11.5px;
  line-height: 1.5;
  color: var(--muted);
}

.panel-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.panel-form :deep(.el-form-item__label) {
  width: 100%;
  padding-bottom: 6px;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--muted);
}

.panel-form :deep(.el-input__wrapper) {
  padding: 4px 13px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  box-shadow:
    0 0 0 1px rgba(20, 53, 42, 0.07) inset,
    0 4px 12px rgba(20, 53, 42, 0.04);
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.panel-form :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.58);
}

.panel-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.72);
  box-shadow:
    0 0 0 1px var(--brand-3) inset,
    0 0 0 4px rgba(200, 241, 105, 0.28);
}

.panel-form :deep(.el-input__inner) {
  height: 30px;
  font-size: 13.5px;
  color: var(--ink);
}

.panel-form :deep(.el-input__inner::placeholder) {
  color: #9caaa0;
}

.panel-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.3);
  box-shadow: 0 0 0 1px rgba(20, 53, 42, 0.05) inset;
}

.panel-form :deep(.el-input.is-disabled .el-input__inner) {
  color: var(--muted);
  -webkit-text-fill-color: var(--muted);
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
  padding-top: 4px;
}

.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: linear-gradient(140deg, #23654c, #14352a);
  font-size: 12.5px;
  letter-spacing: 0.02em;
  box-shadow:
    0 12px 24px rgba(20, 53, 42, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.act-btn:hover {
  border-color: rgba(255, 255, 255, 0.34);
  background: linear-gradient(140deg, #2a7458, #1a4436);
  transform: translateY(-1px);
  box-shadow:
    0 16px 28px rgba(20, 53, 42, 0.26),
    inset 0 1px 0 rgba(255, 255, 255, 0.24);
}

.act-icon {
  margin-left: 2px;
  color: var(--lime);
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes drift {
  from {
    transform: translate3d(0, 0, 0) scale(1);
  }
  to {
    transform: translate3d(18px, -22px, 0) scale(1.08);
  }
}

@keyframes breathe {
  0%,
  100% {
    opacity: 0.55;
    transform: scale(0.96);
  }
  50% {
    opacity: 0.9;
    transform: scale(1.05);
  }
}

@media (max-width: 900px) {
  .profile-page {
    padding: 18px 16px 22px;
    border-radius: 24px;
  }

  .glass-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .glass-hero {
    gap: 16px;
    padding: 20px;
  }

  .hero-left {
    gap: 16px;
  }

  .hero-name {
    font-size: 23px;
  }

  .hero-mark {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .aura,
  .avatar-halo,
  .glass-hero,
  .glass-panel {
    animation: none;
  }
}
</style>
