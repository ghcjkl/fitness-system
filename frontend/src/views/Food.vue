<template>
  <div style="max-width:1100px;margin:0 auto;">
    <el-card>
      <template #header>
        <div class="card-head">
          <b>食材百科</b>
          <span style="font-size:12px;color:#7b8f82;">营养数据均为每100g含量，点击行查看详情</span>
        </div>
      </template>

      <div style="display:flex;gap:10px;margin-bottom:16px;flex-wrap:wrap;">
        <el-input v-model="keyword" placeholder="搜索食物名称/别名，如：鸡胸肉" clearable
          class="food-search" @keyup.enter="handleSearch" />
        <el-select v-model="category" placeholder="全部分类" clearable class="food-category" @change="handleSearch">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="list" border stripe style="cursor:pointer;" @row-click="openDetail">
        <el-table-column label="食物名称" min-width="160">
          <template #default="{ row }">
            <span style="font-weight:600;">{{ row.name }}</span>
            <span v-if="row.alias" style="color:#7b8f82;font-size:12px;">（{{ row.alias }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热量(kcal)" width="110">
          <template #default="{ row }">
            <b style="color:#6f8a2f;">{{ row.calorie }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="protein" label="蛋白(g)" width="90" />
        <el-table-column prop="carb" label="碳水(g)" width="90" />
        <el-table-column prop="fat" label="脂肪(g)" width="90" />
        <el-table-column prop="standardPortion" label="常见份量" min-width="120" />
      </el-table>

      <el-pagination v-if="total > 0" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end;" @current-change="load" @size-change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="detail.name || '食材详情'" width="480px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="分类">{{ detail.category }}</el-descriptions-item>
        <el-descriptions-item label="常见份量">{{ detail.standardPortion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="热量">{{ detail.calorie }} kcal</el-descriptions-item>
        <el-descriptions-item label="蛋白质">{{ detail.protein }} g</el-descriptions-item>
        <el-descriptions-item label="碳水">{{ detail.carb }} g</el-descriptions-item>
        <el-descriptions-item label="脂肪">{{ detail.fat }} g</el-descriptions-item>
        <el-descriptions-item label="膳食纤维">{{ detail.fiber ?? '-' }} g</el-descriptions-item>
        <el-descriptions-item label="钠">{{ detail.sodium ?? '-' }} mg</el-descriptions-item>
        <el-descriptions-item label="别名" :span="2">{{ detail.alias || '-' }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ detail.description || '暂无简介' }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:12px;font-size:12px;color:#7b8f82;">以上数值均按每 100g 计算</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { searchFood, getFoodCategories, getFoodDetail } from '../api/food'

const keyword = ref('')
const category = ref('')
const categories = ref([])
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const detail = ref({})

const load = async () => {
  try {
    const res = await searchFood({
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = res.data.records || []
    total.value = Number(res.data.total || 0)
  } catch (e) { /* 已由请求拦截器提示 */ }
}

const handleSearch = () => {
  pageNum.value = 1
  load()
}

const reset = () => {
  keyword.value = ''
  category.value = ''
  handleSearch()
}

const openDetail = async (row) => {
  try {
    const res = await getFoodDetail(row.id)
    detail.value = res.data
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载食材详情失败')
  }
}

onMounted(async () => {
  load()
  try {
    const res = await getFoodCategories()
    categories.value = res.data || []
  } catch (e) { /* 分类加载失败不影响主列表 */ }
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.food-search {
  width: 260px;
}

.food-category {
  width: 150px;
}

@media (max-width: 768px) {
  .food-search,
  .food-category {
    width: 100%;
  }
}
</style>
