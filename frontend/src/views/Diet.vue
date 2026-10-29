<template>
  <div style="max-width:900px;margin:0 auto;">
    <el-card style="margin-bottom:20px;">
      <template #header>
        <b>日常饮食管理</b>
      </template>

      <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;">
        <span>选择日期：</span>
        <el-date-picker v-model="curDate" type="date" value-format="YYYY-MM-DD" @change="refresh" />
      </div>

      <!-- 每日营养目标 -->
      <div style="margin-bottom:18px;">
        <div style="display:flex;justify-content:space-between;align-items:baseline;margin-bottom:10px;">
          <b>每日营养目标</b>
          <span style="font-size:12px;color:#7b8f82;">{{ goal.basis }}</span>
        </div>
        <div v-for="item in goalItems" :key="item.label" class="goal-row">
          <span class="goal-name">{{ item.label }}</span>
          <el-progress :percentage="Math.min(item.percent || 0, 100)" :show-text="false"
            :stroke-width="10" :color="item.color" style="flex:1;" />
          <span class="goal-value">
            {{ item.intake }} / {{ item.target }} {{ item.unit }}（
            <span :style="{ color: item.percent > 100 ? '#c0564a' : '#4a6154' }">{{ item.percent }}%</span>）
          </span>
        </div>
      </div>

      <el-alert v-if="goal.warnings && goal.warnings.length" type="warning" :closable="false"
        :title="goal.warnings.join('；')" style="margin-bottom:20px;" />

      <!-- 每日营养统计 -->
      <el-row :gutter="12" style="margin-bottom:20px;">
        <el-col :xs="12" :sm="6"><el-card class="stat-card"><div class="stat-label">总热量</div><div class="stat-value">{{ stats.calorie }} kcal</div></el-card></el-col>
        <el-col :xs="12" :sm="6"><el-card class="stat-card"><div class="stat-label">蛋白质</div><div class="stat-value">{{ stats.protein }} g</div></el-card></el-col>
        <el-col :xs="12" :sm="6"><el-card class="stat-card"><div class="stat-label">碳水</div><div class="stat-value">{{ stats.carb }} g</div></el-card></el-col>
        <el-col :xs="12" :sm="6"><el-card class="stat-card"><div class="stat-label">脂肪</div><div class="stat-value">{{ stats.fat }} g</div></el-card></el-col>
      </el-row>

      <el-alert v-if="stats.warnings && stats.warnings.length" type="warning" :closable="false"
        :title="stats.warnings.join('；')" style="margin-bottom:20px;" />
      <el-alert v-else type="success" :closable="false" title="今日营养摄入均衡，无失衡项" style="margin-bottom:20px;" />
    </el-card>

    <!-- 菜谱推荐 -->
    <el-card style="margin-bottom:20px;">
      <template #header>
        <div class="card-head">
          <b>按营养目标推荐菜谱</b>
          <div style="display:flex;gap:8px;">
            <el-select v-model="recommendMeal" size="small" style="width:90px;">
              <el-option label="早餐" value="早餐" />
              <el-option label="午餐" value="午餐" />
              <el-option label="晚餐" value="晚餐" />
              <el-option label="加餐" value="加餐" />
            </el-select>
            <el-button size="small" type="primary" :loading="recommending" @click="loadRecommend(false)">
              生成推荐
            </el-button>
            <el-button size="small" :loading="recommending" @click="loadRecommend(true)">换一换</el-button>
          </div>
        </div>
      </template>

      <template v-if="recipe && recipe.items && recipe.items.length">
        <div style="font-size:12px;color:#7b8f82;margin-bottom:10px;">
          {{ recipe.mealType }}建议 · 本餐目标 {{ recipe.targetCalorie }} kcal / 蛋白 {{ recipe.targetProtein }}g
        </div>
        <div class="recipe-item" v-for="item in recipe.items" :key="item.foodId">
          <span class="recipe-name">{{ item.name }}</span>
          <span class="recipe-tag">{{ item.category }}</span>
          <span class="recipe-grams">{{ item.grams }} g</span>
          <span class="recipe-macro">{{ item.calorie }} kcal · 蛋白 {{ item.protein }}g · 碳水 {{ item.carb }}g · 脂肪 {{ item.fat }}g</span>
        </div>
        <div class="recipe-total">
          合计：{{ recipe.totalCalorie }} kcal / 蛋白 {{ recipe.totalProtein }}g / 碳水 {{ recipe.totalCarb }}g / 脂肪 {{ recipe.totalFat }}g
        </div>
        <div class="recipe-tip">{{ recipe.tip }}</div>
      </template>
      <el-empty v-else :image-size="60" description="点击「生成推荐」，按今日剩余营养目标搭配一餐" />
    </el-card>
    <el-card style="margin-bottom:20px;">
      <template #header><b>三餐打卡</b></template>
      <el-form :model="form" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="8" :xs="24">
            <el-form-item label="餐次">
              <el-select v-model="form.mealType" style="width:100%">
                <el-option label="早餐" value="早餐" />
                <el-option label="午餐" value="午餐" />
                <el-option label="晚餐" value="晚餐" />
                <el-option label="加餐" value="加餐" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" :xs="24">
            <el-form-item label="食物名称">
              <el-autocomplete v-model="form.foodName" :fetch-suggestions="searchFoods"
                placeholder="输入食物名称搜索" style="width:100%" clearable
                @select="onFoodSelect" @input="onFoodInput">
                <template #default="{ item }">
                  <div style="display:flex;justify-content:space-between;gap:12px;">
                    <span>{{ item.name }}</span>
                    <span style="color:#7b8f82;font-size:12px;">{{ item.calorie }} kcal/100g</span>
                  </div>
                </template>
              </el-autocomplete>
              <div v-if="selectedFood" style="font-size:12px;color:#7b8f82;line-height:1.6;margin-top:4px;">
                每100g：{{ selectedFood.calorie }}kcal / 蛋白{{ selectedFood.protein }}g / 碳水{{ selectedFood.carb }}g / 脂肪{{ selectedFood.fat }}g
                <span v-if="selectedFood.standardPortion">（{{ selectedFood.standardPortion }}）</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8" :xs="24">
            <el-form-item label="食用量(g)">
              <el-input-number v-model="form.intake" :min="1" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6" :xs="12">
            <el-form-item label="热量(kcal)">
              <el-input-number v-model="form.calorie" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6" :xs="12">
            <el-form-item label="蛋白质(g)">
              <el-input-number v-model="form.protein" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6" :xs="12">
            <el-form-item label="碳水(g)">
              <el-input-number v-model="form.carb" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6" :xs="12">
            <el-form-item label="脂肪(g)">
              <el-input-number v-model="form.fat" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="餐食照片">
              <el-upload :show-file-list="false" :http-request="doUpload" accept="image/*">
                <img v-if="form.imageUrl && previewUrl" :src="previewUrl" class="diet-img" />
                <el-button v-else type="primary" plain>上传照片</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" :loading="loading" @click="handleAdd" style="width:100%">提交打卡</el-button>
      </el-form>
    </el-card>

    <!-- 记录列表 -->
    <el-card>
      <template #header>
        <div class="card-head">
          <b>我的饮食记录</b>
          <div style="display:flex;gap:8px;align-items:center;">
            <el-date-picker v-model="exportMonth" type="month" value-format="YYYY-MM"
              placeholder="选择月份" style="width:140px" />
            <el-button type="success" plain :loading="exporting" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" border stripe>
        <el-table-column prop="recordDate" label="日期" width="110" />
        <el-table-column prop="mealType" label="餐次" width="80" />
        <el-table-column prop="foodName" label="食物" />
        <el-table-column prop="intake" label="量g" width="80" />
        <el-table-column prop="calorie" label="热量" width="80" />
        <el-table-column prop="protein" label="蛋白g" width="80" />
        <el-table-column prop="carb" label="碳水g" width="80" />
        <el-table-column prop="fat" label="脂肪g" width="80" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[5,10,20]" layout="total, sizes, prev, pager, next" style="margin-top:16px;justify-content:flex-end"
        @current-change="load" @size-change="load" v-if="total > 0" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDiet, dietPage, deleteDiet, dietStats, dietGoal, recommendDiet, uploadDiet } from '../api/diet'
import { exportDietExcel } from '../api/export'
import { searchFood } from '../api/food'

const loading = ref(false)
const exporting = ref(false)
const exportMonth = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const localDate = () => {
  const d = new Date()
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
}
const curDate = ref(localDate())
const stats = ref({ calorie: 0, protein: 0, carb: 0, fat: 0, warnings: [] })
const goal = ref({
  targetCalorie: 0, targetProtein: 0, targetCarb: 0, targetFat: 0,
  intakeCalorie: 0, intakeProtein: 0, intakeCarb: 0, intakeFat: 0,
  caloriePercent: 0, proteinPercent: 0, carbPercent: 0, fatPercent: 0,
  warnings: [], basis: ''
})

const goalItems = computed(() => {
  const g = goal.value
  return [
    { label: '热量', unit: 'kcal', intake: g.intakeCalorie, target: g.targetCalorie, percent: g.caloriePercent, color: g.caloriePercent > 100 ? '#c0564a' : '#2e7d5b' },
    { label: '蛋白质', unit: 'g', intake: g.intakeProtein, target: g.targetProtein, percent: g.proteinPercent, color: g.proteinPercent > 100 ? '#c0564a' : '#7fae56' },
    { label: '碳水', unit: 'g', intake: g.intakeCarb, target: g.targetCarb, percent: g.carbPercent, color: g.carbPercent > 100 ? '#c0564a' : '#6f8a2f' },
    { label: '脂肪', unit: 'g', intake: g.intakeFat, target: g.targetFat, percent: g.fatPercent, color: g.fatPercent > 100 ? '#c0564a' : '#93a497' }
  ]
})
const previewUrl = ref('')
const selectedFood = ref(null)

const form = reactive({
  mealType: '早餐', foodName: '', intake: 100, calorie: 0,
  protein: 0, carb: 0, fat: 0, imageUrl: '', recordDate: curDate.value
})


const round1 = (v) => Math.round((Number(v) || 0) * 10) / 10

// 输入食物名称时联想食材百科
const searchFoods = async (query, cb) => {
  if (!query || !query.trim()) return cb([])
  try {
    const res = await searchFood({ keyword: query.trim(), pageNum: 1, pageSize: 8 })
    cb((res.data.records || []).map(f => ({ ...f, value: f.name })))
  } catch (e) {
    cb([])
  }
}

// 按食用量换算营养值
const applyNutrition = () => {
  if (!selectedFood.value) return
  const ratio = (Number(form.intake) || 0) / 100
  form.calorie = round1(selectedFood.value.calorie * ratio)
  form.protein = round1(selectedFood.value.protein * ratio)
  form.carb = round1(selectedFood.value.carb * ratio)
  form.fat = round1(selectedFood.value.fat * ratio)
}

const onFoodSelect = (item) => {
  selectedFood.value = item
  form.foodName = item.name
  applyNutrition()
}

// 手动改写食物名后，不再套用原食材的营养值
const onFoodInput = (value) => {
  if (selectedFood.value && value !== selectedFood.value.name) selectedFood.value = null
}

watch(() => form.intake, applyNutrition)

const resetForm = () => {
  form.foodName = ''
  form.intake = 100
  form.calorie = 0
  form.protein = 0
  form.carb = 0
  form.fat = 0
  form.imageUrl = ''
  previewUrl.value = ''
  selectedFood.value = null
}

const doUpload = async (opt) => {
  try {
    const res = await uploadDiet(opt.file)
    form.imageUrl = res.data
    previewUrl.value = URL.createObjectURL(opt.file)
    ElMessage.success('照片上传成功')
  } catch (e) {
    ElMessage.error('照片上传失败')
  }
}

const handleAdd = async () => {
  if (!form.foodName) return ElMessage.warning('请填写食物名称')
  form.recordDate = curDate.value
  loading.value = true
  try {
    const res = await addDiet(form)
    ElMessage.success(res.msg)
    resetForm()
    load(); refreshStats(); refreshGoal()
  } catch (e) {
    ElMessage.error(e.message || '打卡失败')
  } finally {
    loading.value = false
  }
}

const load = async () => {
  try {
    const res = await dietPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = Number(res.data.total || 0)
  } catch (e) { /* ignore */ }
}

const refreshStats = async () => {
  try {
    const res = await dietStats(curDate.value)
    stats.value = res.data
  } catch (e) { /* ignore */ }
}

const refreshGoal = async () => {
  try {
    const res = await dietGoal(curDate.value)
    goal.value = res.data
  } catch (e) { /* ignore */ }
}

const refresh = () => { load(); refreshStats(); refreshGoal() }

// 菜谱推荐
const recommendMeal = ref('午餐')
const recommendVariant = ref(0)
const recommending = ref(false)
const recipe = ref(null)

const loadRecommend = async (next) => {
  recommending.value = true
  try {
    const variant = next ? recommendVariant.value + 1 : 0
    const res = await recommendDiet({
      date: curDate.value,
      mealType: recommendMeal.value,
      variant
    })
    recipe.value = res.data
    recommendVariant.value = variant
  } catch (e) {
    ElMessage.error(e.message || '生成推荐失败')
  } finally {
    recommending.value = false
  }
}

const handleExport = async () => {
  exporting.value = true
  try {
    await exportDietExcel(exportMonth.value || localDate().slice(0, 7))
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error(e.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除这条记录？', '提示', { type: 'warning' })
  try {
    const res = await deleteDiet(id)
    ElMessage.success(res.msg)
    load(); refreshStats(); refreshGoal()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  exportMonth.value = localDate().slice(0, 7)
  load(); refreshStats(); refreshGoal()
})
</script>

<style scoped>
.stat-card :deep(.el-card__body) { padding: 14px 16px; }
.stat-label { font-size: 12.5px; color: #7a8a82; }
.stat-value { font-size: 20px; font-weight: 700; margin-top: 6px; font-variant-numeric: tabular-nums; white-space: nowrap; }
.goal-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.goal-name { width: 52px; font-size: 13px; color: #4a6154; }
.goal-value { width: 200px; text-align: right; font-size: 12px; color: #4a6154; }
.recipe-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px dashed #e3ecdc; }
.recipe-name { width: 110px; font-weight: 600; font-size: 14px; }
.recipe-tag { font-size: 12px; color: #2e7d5b; background: #eaf4e4; border-radius: 4px; padding: 1px 6px; }
.recipe-grams { width: 70px; font-size: 13px; color: #22342b; }
.recipe-macro { font-size: 12px; color: #7b8f82; }
.recipe-total { margin-top: 12px; font-size: 13px; font-weight: 600; color: #22342b; }
.recipe-tip { margin-top: 6px; font-size: 12px; color: #6f8a2f; }
.diet-img { width: 100px; height: 100px; object-fit: cover; border-radius: 4px; display: block; }
.card-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; flex-wrap: wrap; }

@media (max-width: 768px) {
  .goal-row {
    flex-wrap: wrap;
    row-gap: 4px;
  }

  .goal-value {
    width: 100%;
    text-align: right;
  }

  .recipe-item {
    flex-wrap: wrap;
    row-gap: 4px;
  }

  .recipe-name {
    width: auto;
    flex: 1;
    min-width: 0;
  }

  .recipe-macro {
    width: 100%;
    padding-left: 0;
  }

  .recipe-total {
    line-height: 1.7;
  }
}
</style>