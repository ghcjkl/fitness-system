<template>
  <div class="admin-page">
    <h2 class="page-title">管理后台</h2>

    <el-tabs v-model="tab">
      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索用户名或昵称" clearable style="width: 240px;"
            @keyup.enter="searchUsers" />
          <el-button type="primary" @click="searchUsers">搜索</el-button>
        </div>
        <el-table :data="users" v-loading="loadingUsers" stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'info' : 'success'" size="small">
                {{ row.status === 0 ? '已禁用' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="165">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link :type="row.status === 0 ? 'success' : 'warning'" @click="toggleStatus(row)">
                {{ row.status === 0 ? '启用' : '禁用' }}
              </el-button>
              <el-button link type="primary" @click="resetPwd(row)">重置密码</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="userTotal > pageSize" background layout="prev, pager, next"
          :total="userTotal" :page-size="pageSize" v-model:current-page="userPage"
          @current-change="loadUsers" class="pager" />
      </el-tab-pane>

      <!-- 动态审核 -->
      <el-tab-pane label="动态审核" name="community">
        <el-table :data="posts" v-loading="loadingPosts" stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="内容" min-width="260" show-overflow-tooltip>
            <template #default="{ row }">{{ row.content || '（图片动态）' }}</template>
          </el-table-column>
          <el-table-column label="发布者" width="130">
            <template #default="{ row }">{{ row.nickname || ('用户' + row.userId) }}</template>
          </el-table-column>
          <el-table-column prop="likeCount" label="点赞" width="80" />
          <el-table-column label="发布时间" width="165">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" @click="deletePost(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="postTotal > pageSize" background layout="prev, pager, next"
          :total="postTotal" :page-size="pageSize" v-model:current-page="postPage"
          @current-change="loadPosts" class="pager" />
      </el-tab-pane>

      <!-- 食材维护 -->
      <el-tab-pane label="食材维护" name="foods">
        <div class="toolbar">
          <el-input v-model="foodKeyword" placeholder="搜索食材名称或别名" clearable style="width: 220px;"
            @keyup.enter="searchFoods" />
          <el-select v-model="foodCategory" placeholder="全部分类" clearable style="width: 140px;" @change="searchFoods">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <el-button type="primary" @click="searchFoods">搜索</el-button>
          <el-button type="success" @click="openFoodDialog()">新增食材</el-button>
        </div>
        <el-table :data="foods" v-loading="loadingFoods" stripe>
          <el-table-column label="名称" min-width="140">
            <template #default="{ row }">
              {{ row.name }}<span v-if="row.alias" class="alias">（{{ row.alias }}）</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="calorie" label="热量(kcal)" width="110" />
          <el-table-column prop="protein" label="蛋白(g)" width="90" />
          <el-table-column prop="carb" label="碳水(g)" width="90" />
          <el-table-column prop="fat" label="脂肪(g)" width="90" />
          <el-table-column prop="standardPortion" label="常见份量" min-width="110" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openFoodDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="removeFood(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="foodTotal > pageSize" background layout="prev, pager, next"
          :total="foodTotal" :page-size="pageSize" v-model:current-page="foodPage"
          @current-change="loadFoods" class="pager" />
      </el-tab-pane>

      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="logs">
        <el-table :data="logs" v-loading="loadingLogs" stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="操作人" width="130" />
          <el-table-column prop="action" label="操作" min-width="240" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP" width="140" />
          <el-table-column prop="status" label="状态码" width="90" />
          <el-table-column label="时间" width="165">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="logTotal > pageSize" background layout="prev, pager, next"
          :total="logTotal" :page-size="pageSize" v-model:current-page="logPage"
          @current-change="loadLogs" class="pager" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="foodDialog" :title="foodForm.id ? '编辑食材' : '新增食材'" width="600px">
      <el-form :model="foodForm" label-width="92px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="foodForm.name" placeholder="如：鸡胸肉" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="别名">
              <el-input v-model="foodForm.alias" placeholder="多个用英文逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" required>
              <el-select v-model="foodForm.category" filterable allow-create default-first-option
                placeholder="选择或输入分类" style="width:100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="常见份量">
              <el-input v-model="foodForm.standardPortion" placeholder="如：1个约50g" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="热量(kcal)" required>
              <el-input-number v-model="foodForm.calorie" :min="0" :precision="1" :controls="false"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="蛋白(g)">
              <el-input-number v-model="foodForm.protein" :min="0" :precision="1" :controls="false"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="碳水(g)">
              <el-input-number v-model="foodForm.carb" :min="0" :precision="1" :controls="false"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="脂肪(g)">
              <el-input-number v-model="foodForm.fat" :min="0" :precision="1" :controls="false"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纤维(g)">
              <el-input-number v-model="foodForm.fiber" :min="0" :precision="1" :controls="false"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="钠(mg)">
              <el-input-number v-model="foodForm.sodium" :min="0" :precision="1" :controls="false"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介">
              <el-input v-model="foodForm.description" type="textarea" :rows="2" placeholder="食用建议，可不填" />
            </el-form-item>
          </el-col>
        </el-row>
        <div style="font-size:12px;color:#7b8f82;padding-left:92px;">营养值均为每 100g 含量</div>
      </el-form>
      <template #footer>
        <el-button @click="foodDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingFood" @click="saveFood">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminUsers, updateUserStatus, resetUserPassword, getAdminLogs, adminDeleteCommunity,
  getAdminFoods, createFood, updateFood, deleteFood
} from '../api/admin'
import { communityPage } from '../api/community'
import { getFoodCategories } from '../api/food'

const tab = ref('users')
const pageSize = 10

const users = ref([])
const userPage = ref(1)
const userTotal = ref(0)
const keyword = ref('')
const loadingUsers = ref(false)

const posts = ref([])
const postPage = ref(1)
const postTotal = ref(0)
const loadingPosts = ref(false)

const logs = ref([])
const logPage = ref(1)
const logTotal = ref(0)
const loadingLogs = ref(false)

// 食材维护
const emptyFood = () => ({
  id: null, name: '', alias: '', category: '', calorie: null, protein: null,
  carb: null, fat: null, fiber: null, sodium: null, standardPortion: '', description: ''
})
const categories = ref([])
const foods = ref([])
const foodPage = ref(1)
const foodTotal = ref(0)
const foodKeyword = ref('')
const foodCategory = ref('')
const loadingFoods = ref(false)
const foodDialog = ref(false)
const savingFood = ref(false)
const foodForm = reactive(emptyFood())

const loadFoods = async () => {
  loadingFoods.value = true
  try {
    const res = await getAdminFoods({
      pageNum: foodPage.value,
      pageSize,
      keyword: foodKeyword.value || undefined,
      category: foodCategory.value || undefined
    })
    foods.value = res.data.records || []
    foodTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loadingFoods.value = false
  }
}

const searchFoods = () => {
  foodPage.value = 1
  loadFoods()
}

const openFoodDialog = (row) => {
  Object.assign(foodForm, emptyFood())
  if (row) Object.assign(foodForm, row)
  foodDialog.value = true
}

const saveFood = async () => {
  if (!foodForm.name.trim()) return ElMessage.warning('请输入食材名称')
  if (!foodForm.category) return ElMessage.warning('请选择食材分类')
  if (foodForm.calorie === null || foodForm.calorie === undefined) return ElMessage.warning('请填写热量')
  savingFood.value = true
  try {
    const payload = { ...foodForm }
    const res = foodForm.id ? await updateFood(foodForm.id, payload) : await createFood(payload)
    ElMessage.success(res.msg)
    foodDialog.value = false
    loadFoods()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    savingFood.value = false
  }
}

const removeFood = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除食材「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const res = await deleteFood(row.id)
    ElMessage.success(res.msg)
    loadFoods()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await getAdminUsers({ pageNum: userPage.value, pageSize, keyword: keyword.value })
    users.value = res.data.records || []
    userTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loadingUsers.value = false
  }
}

const searchUsers = () => {
  userPage.value = 1
  loadUsers()
}

const toggleStatus = async (row) => {
  const next = row.status === 0 ? 1 : 0
  if (next === 0) {
    try {
      await ElMessageBox.confirm(`确定禁用用户「${row.username}」吗？禁用后该账号无法登录。`, '提示', { type: 'warning' })
    } catch (e) {
      return
    }
  }
  try {
    const res = await updateUserStatus(row.id, next)
    ElMessage.success(res.msg)
    row.status = next
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const resetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定将「${row.username}」的密码重置为 123456 吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const res = await resetUserPassword(row.id)
    ElMessage.success(res.msg)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const loadPosts = async () => {
  loadingPosts.value = true
  try {
    const res = await communityPage({ pageNum: postPage.value, pageSize })
    posts.value = res.data.records || []
    postTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loadingPosts.value = false
  }
}

const deletePost = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条动态吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const res = await adminDeleteCommunity(row.id)
    ElMessage.success(res.msg)
    loadPosts()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const loadLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await getAdminLogs({ pageNum: logPage.value, pageSize })
    logs.value = res.data.records || []
    logTotal.value = res.data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loadingLogs.value = false
  }
}

watch(tab, (value) => {
  if (value === 'community' && !posts.value.length) loadPosts()
  if (value === 'logs' && !logs.value.length) loadLogs()
  if (value === 'foods' && !foods.value.length) loadFoods()
})

onMounted(async () => {
  loadUsers()
  try {
    const res = await getFoodCategories()
    categories.value = res.data || []
  } catch (e) { /* 分类加载失败不影响页面 */ }
})
</script>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; padding: 0; }
.page-title { margin: 0 0 12px; font-size: 22px; font-weight: 700; letter-spacing: -0.02em; color: var(--ink); }
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.pager { justify-content: center; margin-top: 18px; }
.alias { color: #7b8f82; font-size: 12px; }

@media (max-width: 768px) {
  .admin-page {
    padding: 0;
  }

  .page-title {
    font-size: 20px;
  }

  .toolbar :deep(.el-input) {
    width: 100% !important;
  }
}
</style>
