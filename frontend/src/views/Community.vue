<template>
  <div class="community-page">
    <header class="feed-head">
      <div>
        <span class="eyebrow">Community</span>
        <h2 class="page-title">社区分享</h2>
        <p class="page-sub">记录训练与饮食，和同伴一起坚持</p>
      </div>
      <span v-if="headCount" class="total-pill">{{ headCount }} 条动态</span>
    </header>

    <!-- 分区切换 -->
    <nav class="tabs" role="tablist">
      <button v-for="t in TABS" :key="t.key" type="button" class="tab-btn" :class="{ active: tab === t.key }"
        @click="switchTab(t.key)">
        <app-icon :name="t.icon" :size="16" class="tab-icon" />
        <span>{{ t.label }}</span>
      </button>
    </nav>

    <!-- 动态分区：首页 / 朋友 / 我的 -->
    <template v-if="tab !== 'messages'">
      <!-- 发布器（仅首页） -->
      <div v-if="tab === 'home'" class="publish-card">
        <el-avatar :size="40" :src="myAvatar || ''" class="avatar">{{ myName.charAt(0) }}</el-avatar>
        <div class="publish-main">
          <el-input ref="contentInput" v-model="content" type="textarea" :rows="2" maxlength="300" show-word-limit
            resize="none" class="publish-input" placeholder="分享今天的训练或饮食，带图更容易被看到..." />
          <div class="publish-bar">
            <div class="publish-tools">
              <div v-for="(src, index) in previews" :key="src" class="preview-wrap">
                <img :src="src" class="preview" />
                <span class="preview-remove" @click="removeImage(index)">×</span>
              </div>
              <el-upload v-if="imgFiles.length < 9" :auto-upload="false" :show-file-list="false" accept="image/*" multiple
                :on-change="onFileChange">
                <button type="button" class="img-tile">
                  <svg viewBox="0 0 24 24" class="tile-icon" aria-hidden="true">
                    <path d="M4 7h3l1.2-2h7.6L17 7h3a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z"
                      fill="none" stroke="currentColor" stroke-width="1.6" />
                    <circle cx="12" cy="13" r="3.2" fill="none" stroke="currentColor" stroke-width="1.6" />
                  </svg>
                  <span>{{ imgFiles.length ? imgFiles.length + '/9' : '添加图片' }}</span>
                </button>
              </el-upload>
            </div>
            <el-button type="primary" round class="publish-btn" :loading="publishing" @click="publish">发布</el-button>
          </div>
        </div>
      </div>

      <!-- 我的：个人信息条 -->
      <section v-if="tab === 'mine'" class="me-card">
        <el-avatar :size="54" :src="myAvatar || ''" class="me-avatar">{{ myName.charAt(0) }}</el-avatar>
        <div class="me-info">
          <h3 class="me-name">{{ myName }}</h3>
          <span class="me-handle">@{{ myUsername || '我' }}</span>
        </div>
        <div class="me-stats">
          <div class="me-stat">
            <b>{{ feedList.length }}</b>
            <span>动态</span>
          </div>
          <div class="me-stat">
            <b>{{ myLikes }}</b>
            <span>获赞</span>
          </div>
        </div>
      </section>

      <p v-if="tab !== 'home'" class="tab-note">
        {{ tab === 'friends' ? '其他训练者的日常，点开看看' : '这里只有你自己发布的动态' }}
      </p>

      <!-- 瀑布流 -->
      <div class="feed" v-loading="feedLoading">
        <article v-for="item in feedList" :key="item.id" class="post-card"
          :class="{ 'text-only': !imgList(item).length }">
          <button v-if="item.userId === myId" type="button" class="del-btn" @click="remove(item)">删除</button>
          <div v-if="imgList(item).length" class="media" :class="'n' + imgList(item).length">
            <el-image v-for="(src, index) in imgList(item)" :key="src" :src="src" :preview-src-list="imgList(item)"
              :initial-index="index" fit="cover" class="media-item" preview-teleported />
          </div>
          <div class="post-body">
            <p v-if="item.content" class="content" :class="{ clamp: imgList(item).length }">{{ item.content }}</p>
            <div class="post-meta">
              <div class="author">
                <el-avatar :size="22" :src="item.avatar || ''">{{ (item.nickname || '用户').charAt(0) }}</el-avatar>
                <div class="author-text">
                  <span class="nickname">{{ item.nickname || ('用户' + item.userId) }}</span>
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                </div>
              </div>
              <div class="acts">
                <button type="button" class="act" @click="like(item)">
                  <svg viewBox="0 0 24 24" class="icon" aria-hidden="true">
                    <path
                      d="M12 20.4l-1.1-1C6.2 15.1 3.1 12.4 3.1 8.9 3.1 6.2 5.3 4 8 4c1.5 0 3 .7 4 1.9C13 4.7 14.5 4 16 4c2.7 0 4.9 2.2 4.9 4.9 0 3.5-3.1 6.2-7.8 10.5l-1.1 1z"
                      fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
                  </svg>
                  <span>{{ item.likeCount || 0 }}</span>
                </button>
                <button type="button" class="act" @click="openComments(item)">
                  <svg viewBox="0 0 24 24" class="icon" aria-hidden="true">
                    <path
                      d="M20.5 11.6c0 4.3-3.8 7.8-8.5 7.8-1 0-2-.2-2.9-.5L4 20.5l1.2-3.7a7.4 7.4 0 0 1-1.7-4.8c0-4.3 3.8-7.8 8.5-7.8s8.5 3.5 8.5 7.4z"
                      fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
                  </svg>
                  <span>{{ item.commentCount || 0 }}</span>
                </button>
              </div>
            </div>
          </div>
        </article>

        <el-empty v-if="!feedList.length && !feedLoading" :description="emptyText" class="empty-block">
          <el-button v-if="tab !== 'home'" type="primary" round class="empty-btn" @click="goHome">去发布动态</el-button>
        </el-empty>
      </div>

      <el-pagination v-if="tab === 'home' && total > pageSize" background layout="prev, pager, next" :total="total"
        :page-size="pageSize" v-model:current-page="pageNum" @current-change="load" class="pager" />
    </template>

    <!-- 消息分区 -->
    <template v-else>
      <div class="msg-summary">
        <div class="msg-stat">
          <b>{{ myLikes }}</b>
          <span>累计获赞</span>
        </div>
        <div class="msg-stat">
          <b>{{ messages.length }}</b>
          <span>收到评论</span>
        </div>
      </div>

      <div class="msg-list" v-loading="tabLoading">
        <button v-for="m in messages" :key="m.id" type="button" class="msg-item" @click="openComments(m.post)">
          <el-avatar :size="38" :src="m.avatar || ''" class="msg-avatar">
            {{ (m.nickname || '用户').charAt(0) }}
          </el-avatar>
          <div class="msg-body">
            <div class="msg-line"><b>{{ m.nickname || '用户' }}</b> 评论了你的动态</div>
            <p class="msg-quote">{{ m.content }}</p>
            <p class="msg-post">你的动态：{{ m.post.content || '[图片动态]' }}</p>
            <span class="msg-time">{{ formatTime(m.createTime) }}</span>
          </div>
        </button>

        <el-empty v-if="!messages.length && !tabLoading" description="还没有收到评论消息" class="msg-empty">
          <el-button type="primary" round class="empty-btn" @click="goHome">去首页逛逛</el-button>
        </el-empty>
      </div>
    </template>

    <!-- 评论弹窗 -->
    <el-dialog v-model="commentVisible" title="评论" width="520px" append-to-body>
      <div v-if="activePost" class="comment-post">
        <div class="comment-post-user">{{ activePost.nickname || '用户' }}</div>
        <div class="comment-post-text">{{ activePost.content || '[图片动态]' }}</div>
      </div>
      <div class="comment-list" v-loading="commentLoading">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :size="32" :src="c.avatar || ''">{{ (c.nickname || '用户').charAt(0) }}</el-avatar>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="comment-name">{{ c.nickname || '用户' }}</span>
              <span class="comment-time">{{ formatTime(c.createTime) }}</span>
              <el-button v-if="c.userId === myId" link type="danger" size="small" class="comment-delete"
                @click="removeComment(c)">删除</el-button>
            </div>
            <div class="comment-text">{{ c.content }}</div>
          </div>
        </div>
        <el-empty v-if="!comments.length && !commentLoading" description="还没有评论，来说两句" :image-size="60" />
      </div>
      <el-pagination v-if="commentTotal > commentPageSize" small background layout="prev, pager, next"
        :total="commentTotal" :page-size="commentPageSize" v-model:current-page="commentPageNum"
        @current-change="loadComments" class="comment-pager" />
      <template #footer>
        <div class="comment-input-row">
          <el-input v-model="commentInput" type="textarea" :rows="2" maxlength="300" show-word-limit resize="none"
            placeholder="写下你的评论..." />
          <el-button type="primary" :loading="commentSubmitting" @click="submitComment">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo } from '../api/user'
import { publishCommunity, communityPage, likeCommunity, deleteCommunity,
  communityComments, addCommunityComment, deleteCommunityComment } from '../api/community'

const TABS = [
  { key: 'home', label: '首页', icon: 'home' },
  { key: 'friends', label: '朋友', icon: 'users' },
  { key: 'messages', label: '消息', icon: 'message' },
  { key: 'mine', label: '我的', icon: 'profile' }
]

const tab = ref('home')
const tabLoading = ref(false)
const contentInput = ref(null)

const content = ref('')
const MAX_IMAGES = 9
const imgFiles = ref([])
const previews = ref([])
const list = ref([])
const friendsPosts = ref([])
const myPosts = ref([])
const messages = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const publishing = ref(false)
const loading = ref(false)
const myId = ref(null)
const myName = ref('我')
const myUsername = ref('')
const myAvatar = ref('')

const feedLoading = computed(() => (tab.value === 'home' ? loading.value : tabLoading.value))
const feedList = computed(() => {
  if (tab.value === 'friends') return friendsPosts.value
  if (tab.value === 'mine') return myPosts.value
  return list.value
})
const myLikes = computed(() => myPosts.value.reduce((sum, item) => sum + (item.likeCount || 0), 0))
const headCount = computed(() => {
  if (tab.value === 'messages') return 0
  if (tab.value === 'home') return total.value
  return feedList.value.length
})
const emptyText = computed(() => {
  if (tab.value === 'friends') return '还没有其他用户的动态'
  if (tab.value === 'mine') return '你还没有发布动态'
  return '还没有动态，来发布第一条吧'
})

const switchTab = (key) => {
  if (tab.value === key) return
  tab.value = key
  if (key === 'home') load()
  else if (key === 'friends') loadFriends()
  else if (key === 'mine') loadMine()
  else loadMessages()
}

const goHome = () => {
  tab.value = 'home'
  load()
  nextTick(() => contentInput.value?.focus())
}

const load = async () => {
  loading.value = true
  try {
    const res = await communityPage({ pageNum: pageNum.value, pageSize })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadFriends = async () => {
  tabLoading.value = true
  try {
    const res = await communityPage({ pageNum: 1, pageSize: 50 })
    friendsPosts.value = (res.data.records || []).filter(item => item.userId !== myId.value)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    tabLoading.value = false
  }
}

const loadMine = async () => {
  tabLoading.value = true
  try {
    const res = await communityPage({ pageNum: 1, pageSize: 50 })
    myPosts.value = (res.data.records || []).filter(item => item.userId === myId.value)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    tabLoading.value = false
  }
}

const loadMessages = async () => {
  tabLoading.value = true
  try {
    const res = await communityPage({ pageNum: 1, pageSize: 50 })
    const mine = (res.data.records || []).filter(item => item.userId === myId.value)
    myPosts.value = mine
    // 拉取自己最近动态下的评论，聚合成消息流
    const groups = await Promise.all(mine.slice(0, 8).map(async post => {
      const r = await communityComments({ dynamicId: post.id, pageNum: 1, pageSize: 20 })
      return (r.data.records || [])
        .filter(c => c.userId !== myId.value)
        .map(c => ({ ...c, post }))
    }))
    messages.value = groups.flat().sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (e) {
    ElMessage.error(e.message || '消息加载失败')
  } finally {
    tabLoading.value = false
  }
}

/* 评论 */
const commentVisible = ref(false)
const activePost = ref(null)
const comments = ref([])
const commentTotal = ref(0)
const commentPageNum = ref(1)
const commentPageSize = 10
const commentLoading = ref(false)
const commentInput = ref('')
const commentSubmitting = ref(false)

const openComments = (item) => {
  activePost.value = item
  commentVisible.value = true
  commentPageNum.value = 1
  commentInput.value = ''
  comments.value = []
  loadComments()
}

const loadComments = async () => {
  if (!activePost.value) return
  commentLoading.value = true
  try {
    const res = await communityComments({
      dynamicId: activePost.value.id,
      pageNum: commentPageNum.value,
      pageSize: commentPageSize
    })
    comments.value = res.data.records || []
    commentTotal.value = Number(res.data.total || 0)
  } catch (e) {
    ElMessage.error(e.message || '评论加载失败')
  } finally {
    commentLoading.value = false
  }
}

const submitComment = async () => {
  const text = commentInput.value.trim()
  if (!text) return ElMessage.warning('请输入评论内容')
  commentSubmitting.value = true
  try {
    const res = await addCommunityComment(activePost.value.id, text)
    ElMessage.success(res.msg)
    commentInput.value = ''
    activePost.value.commentCount = (activePost.value.commentCount || 0) + 1
    commentPageNum.value = 1
    loadComments()
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  } finally {
    commentSubmitting.value = false
  }
}

const removeComment = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const res = await deleteCommunityComment(item.id)
    ElMessage.success(res.msg)
    if (activePost.value) {
      activePost.value.commentCount = Math.max((activePost.value.commentCount || 1) - 1, 0)
    }
    loadComments()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const onFileChange = (file) => {
  if (!file.raw) return
  if (file.raw.type && !file.raw.type.startsWith('image/')) {
    return ElMessage.warning('只能选择图片文件')
  }
  if (imgFiles.value.length >= MAX_IMAGES) {
    return ElMessage.warning('最多上传 9 张图片')
  }
  imgFiles.value.push(file.raw)
  previews.value.push(URL.createObjectURL(file.raw))
}

const removeImage = (index) => {
  if (previews.value[index]) URL.revokeObjectURL(previews.value[index])
  imgFiles.value.splice(index, 1)
  previews.value.splice(index, 1)
}

const clearImages = () => {
  previews.value.forEach(url => URL.revokeObjectURL(url))
  imgFiles.value = []
  previews.value = []
}

// 后端把多张图片以逗号分隔存入 imgUrl，单图老数据同样兼容
const imgList = (item) => (item.imgUrl ? item.imgUrl.split(',').filter(Boolean) : [])

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  const diff = Date.now() - date.getTime()
  const minute = Math.floor(diff / 60000)
  if (minute < 1) return '刚刚'
  if (minute < 60) return `${minute}分钟前`
  const hour = Math.floor(minute / 60)
  if (hour < 24) return `${hour}小时前`
  const day = Math.floor(hour / 24)
  if (day === 1) return '昨天'
  if (day < 7) return `${day}天前`
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getMonth() + 1}月${date.getDate()}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const publish = async () => {
  if (!content.value.trim() && !imgFiles.value.length) {
    return ElMessage.warning('请输入内容或选择图片')
  }
  publishing.value = true
  try {
    const res = await publishCommunity(content.value.trim(), imgFiles.value)
    ElMessage.success(res.msg)
    content.value = ''
    clearImages()
    pageNum.value = 1
    load()
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const like = async (item) => {
  try {
    await likeCommunity(item.id)
    item.likeCount = (item.likeCount || 0) + 1
  } catch (e) {
    ElMessage.error(e.message || '点赞失败')
  }
}

const remove = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除这条动态吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const res = await deleteCommunity(item.id)
    ElMessage.success(res.msg)
    if (tab.value === 'home') load()
    else if (tab.value === 'mine') loadMine()
    else if (tab.value === 'friends') loadFriends()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  try {
    const res = await getUserInfo()
    myId.value = res.data.id
    myName.value = res.data.nickname || res.data.username || '我'
    myUsername.value = res.data.username || ''
    myAvatar.value = res.data.avatar || ''
  } catch (e) { /* 未登录由路由守卫处理 */ }
  load()
})
</script>

<style scoped>
.community-page { max-width: 980px; margin: 0 auto; padding: 0; }

/* 页头 */
.feed-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.eyebrow {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--brand-3);
}
.page-title { margin: 6px 0 0; font-size: 26px; font-weight: 700; letter-spacing: -0.02em; color: var(--ink); }
.page-sub { margin: 6px 0 0; font-size: 13px; color: var(--muted); }
.total-pill {
  flex: 0 0 auto;
  padding: 5px 12px;
  border: 1px solid #e2ecdf;
  border-radius: 999px;
  background: #eef4ea;
  font-size: 11.5px;
  color: #5d7565;
}

/* 分区切换 */
.tabs {
  display: inline-flex;
  gap: 4px;
  max-width: 100%;
  margin-bottom: 18px;
  padding: 4px;
  overflow-x: auto;
  border: 1px solid #e3ecdf;
  border-radius: 16px;
  background: #eef3ea;
  box-shadow: inset 0 1px 2px rgba(20, 53, 42, 0.04);
  scrollbar-width: none;
}
.tabs::-webkit-scrollbar { display: none; }

.tab-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex: 0 0 auto;
  padding: 8px 18px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #6d7f72;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}
.tab-btn:hover { color: var(--brand-2); }
.tab-btn.active {
  background: linear-gradient(140deg, #23654c, #14352a);
  color: #f4f8ee;
  box-shadow: 0 6px 14px rgba(20, 53, 42, 0.18);
}
.tab-btn.active .tab-icon { color: var(--lime); }

.tab-note { margin: 0 0 12px; font-size: 12px; color: var(--muted); }

/* 发布器与卡片 */
.publish-card,
.post-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.publish-card { display: flex; gap: 12px; padding: 14px 16px; margin-bottom: 18px; }
.publish-main { flex: 1; min-width: 0; }

.publish-input :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 2px 0;
  font-size: 14px;
  line-height: 1.7;
  background: transparent;
}
.publish-input :deep(.el-input__count) { background: transparent; color: #a9baae; }

.publish-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
.publish-tools { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }

.img-tile {
  width: 72px;
  height: 72px;
  border: 1px dashed #cfdcd2;
  border-radius: 10px;
  background: #f7faf7;
  color: #7d9184;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.15s;
}
.img-tile:hover { color: var(--brand-2); border-color: var(--brand-2); background: #eef4ef; }
.tile-icon { width: 20px; height: 20px; }

.publish-btn { background: var(--brand-2); border-color: var(--brand-2); padding: 8px 22px; }
.publish-btn:hover { background: #17452f; border-color: #17452f; }

.preview-wrap { position: relative; width: 72px; height: 72px; }
.preview { display: block; width: 72px; height: 72px; object-fit: cover; border-radius: 10px; }
.preview-remove {
  position: absolute;
  top: -7px;
  right: -7px;
  width: 20px;
  height: 20px;
  line-height: 18px;
  text-align: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

/* 我的信息条 */
.me-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
  padding: 16px 18px;
  border: 1px solid #e3efe0;
  border-radius: 16px;
  background: linear-gradient(120deg, #f7fbf4, #eaf4e7);
}
.me-avatar {
  flex: 0 0 auto;
  border: 2px solid #fff;
  background: var(--mint);
  color: var(--brand-2);
  font-weight: 600;
  box-shadow: 0 6px 14px rgba(20, 53, 42, 0.12);
}
.me-info { min-width: 0; }
.me-name { margin: 0; font-size: 16px; font-weight: 700; color: var(--ink); }
.me-handle { display: block; margin-top: 3px; font-size: 11.5px; color: var(--muted); }
.me-stats { display: flex; gap: 22px; margin-left: auto; }
.me-stat { text-align: center; }
.me-stat b { display: block; font-size: 19px; font-weight: 700; color: var(--brand-2); letter-spacing: -0.02em; }
.me-stat span { display: block; margin-top: 2px; font-size: 10.5px; color: var(--muted); }

/* 双列瀑布流 */
.feed { column-count: 2; column-gap: 14px; }
.post-card {
  position: relative;
  break-inside: avoid;
  -webkit-column-break-inside: avoid;
  margin: 0 0 14px;
  border-radius: 14px;
  overflow: hidden;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.post-card:hover { box-shadow: 0 8px 22px rgba(20, 53, 42, 0.1); transform: translateY(-1px); }
.post-card.text-only { background: linear-gradient(150deg, #f4faf3, #e8f2e9); }

/* 图片区：按张数自适应列数，不满三张不留空 */
.media { display: grid; gap: 2px; width: 100%; background: #f2f8ee; }
.media.n1 { grid-template-columns: 1fr; }
.media.n2 { grid-template-columns: repeat(2, 1fr); }
.media.n4 { grid-template-columns: repeat(2, 1fr); }
.media.n3,
.media.n5,
.media.n6,
.media.n7,
.media.n8,
.media.n9 { grid-template-columns: repeat(3, 1fr); }
.media-item { display: block; width: 100%; background: #eef4ef; }
.media.n1 .media-item :deep(.el-image__inner) { display: block; width: 100%; height: auto; }
.media:not(.n1) .media-item { aspect-ratio: 1; }
.media:not(.n1) .media-item :deep(.el-image__inner) { width: 100%; height: 100%; object-fit: cover; }

.post-body { padding: 10px 12px 12px; }
.post-card.text-only .post-body { padding: 16px 14px; }

.content {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.55;
  color: #1d3026;
  white-space: pre-wrap;
  word-break: break-word;
}
.content.clamp {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.post-card.text-only .content { font-size: 15px; line-height: 1.8; }

.post-meta { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-top: 10px; }
.author { display: flex; align-items: center; gap: 7px; min-width: 0; }
.author-text { display: flex; flex-direction: column; min-width: 0; line-height: 1.2; }
.nickname {
  font-size: 12px;
  font-weight: 500;
  color: #4a6154;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.time { margin-top: 2px; font-size: 11px; color: #9aab9e; }

.acts { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.act {
  display: flex;
  align-items: center;
  gap: 3px;
  border: none;
  background: transparent;
  padding: 0;
  color: #77897c;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.15s;
}
.act:hover { color: var(--brand-2); }
.act:active { transform: scale(0.94); }
.icon { width: 17px; height: 17px; }

.del-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
  border: none;
  border-radius: 12px;
  padding: 3px 9px;
  background: rgba(255, 255, 255, 0.85);
  color: #6b8074;
  font-size: 11px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.15s;
}
.post-card:hover .del-btn { opacity: 1; }
@media (hover: none) { .del-btn { opacity: 1; } }

.empty-block { column-span: all; }
.empty-btn { padding: 9px 20px; background: var(--brand-2); border-color: var(--brand-2); }

/* 消息分区 */
.msg-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  max-width: 660px;
  margin-bottom: 14px;
}
.msg-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 16px;
  background: #fff;
  box-shadow: var(--shadow-sm);
}
.msg-stat b { font-size: 24px; font-weight: 700; letter-spacing: -0.02em; color: var(--brand-2); }
.msg-stat span { font-size: 11.5px; color: var(--muted); }

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 660px;
  min-height: 160px;
}
.msg-item {
  display: flex;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease, border-color 0.2s ease;
}
.msg-item:hover {
  border-color: #dfeadd;
  box-shadow: 0 10px 24px rgba(20, 53, 42, 0.09);
  transform: translateY(-1px);
}
.msg-avatar { flex: 0 0 auto; background: var(--mint); color: var(--brand-2); font-weight: 600; }
.msg-body { flex: 1; min-width: 0; }
.msg-line { font-size: 12.5px; color: #3c5749; }
.msg-line b { color: var(--brand-2); }
.msg-quote {
  margin: 6px 0 0;
  padding: 7px 10px;
  border-radius: 9px;
  background: #f4f8f1;
  font-size: 12.5px;
  line-height: 1.55;
  color: #31473a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}
.msg-post {
  margin: 5px 0 0;
  overflow: hidden;
  font-size: 11px;
  color: #93a597;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-time { display: block; margin-top: 5px; font-size: 10.5px; color: #a2b2a6; }
.msg-empty { margin-top: 10px; }

/* 评论样式 */
.comment-post {
  padding: 12px;
  background: #f2f8ee;
  border-radius: 8px;
  margin-bottom: 16px;
}
.comment-post-user {
  font-size: 13px;
  font-weight: 600;
  color: var(--brand-2);
  margin-bottom: 4px;
}
.comment-post-text {
  font-size: 13px;
  color: #4a6154;
}
.comment-list {
  max-height: 380px;
  overflow-y: auto;
  margin-bottom: 8px;
}
.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f2f8ee;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}
.comment-name {
  font-size: 13px;
  font-weight: 500;
  color: #22342b;
}
.comment-time {
  font-size: 12px;
  color: #7b8f82;
}
.comment-delete {
  font-size: 12px;
  padding: 0;
}
.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #22342b;
  word-break: break-word;
}
.comment-pager {
  justify-content: flex-end;
  margin: 8px 0 12px;
}
.comment-input-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}
.comment-input-row .el-input {
  flex: 1;
}

.pager { justify-content: center; margin-top: 20px; }

@media (max-width: 768px) {
  .community-page {
    padding: 0;
  }

  .feed-head {
    align-items: flex-start;
  }

  .page-title {
    font-size: 21px;
  }

  .total-pill {
    margin-top: 2px;
    padding: 4px 10px;
  }

  .tabs {
    display: flex;
    width: 100%;
    margin-bottom: 14px;
  }

  .tab-btn {
    flex: 1;
    padding: 8px 6px;
    font-size: 12px;
  }

  .tab-icon {
    width: 15px;
    height: 15px;
  }

  .publish-card {
    padding: 12px;
    border-radius: 14px;
  }

  .publish-bar {
    align-items: flex-end;
  }

  .img-tile {
    width: 60px;
    height: 60px;
    font-size: 10px;
  }

  .preview-wrap,
  .preview {
    width: 60px;
    height: 60px;
  }

  .me-card {
    padding: 14px;
  }

  .me-stats {
    gap: 16px;
  }

  .feed {
    column-gap: 10px;
  }

  .post-card {
    margin-bottom: 10px;
    border-radius: 12px;
  }

  .post-body {
    padding: 8px 10px 10px;
  }

  .post-card.text-only .post-body {
    padding: 14px 12px;
  }

  .content {
    font-size: 12.5px;
  }

  .post-card.text-only .content {
    font-size: 14px;
  }

  .nickname {
    font-size: 11px;
  }

  .time {
    font-size: 10px;
  }

  .post-meta {
    margin-top: 8px;
  }

  .author {
    gap: 5px;
  }

  .acts {
    gap: 9px;
  }

  .act {
    gap: 2px;
    font-size: 11px;
  }

  .icon {
    width: 15px;
    height: 15px;
  }

  .msg-item {
    padding: 12px 13px;
  }

  .comment-input-row {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .comment-list {
    max-height: 46vh;
  }

  .comment-pager {
    justify-content: center;
  }
}
</style>
