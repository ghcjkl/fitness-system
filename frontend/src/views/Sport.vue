<template>
  <div style="max-width:900px;margin:0 auto;">
    <el-card v-if="streak" style="margin-bottom:20px;">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <b>打卡连续天数</b>
          <span style="font-size:12px;color:#7b8f82;">本周已运动 {{ streak.weekDone }} 天</span>
        </div>
      </template>
      <div style="display:flex;flex-wrap:wrap;gap:24px;align-items:center;justify-content:space-between;">
        <div style="display:flex;gap:32px;align-items:flex-end;">
          <div>
            <div style="font-size:13px;color:#7b8f82;">连续打卡</div>
            <div style="line-height:1.2;">
              <span style="font-size:34px;font-weight:600;color:#2e7d5b;">{{ streak.currentStreak }}</span>
              <span style="margin-left:4px;color:#4a6154;">天</span>
            </div>
          </div>
          <div style="font-size:13px;color:#4a6154;line-height:1.8;">
            <div>累计打卡 {{ streak.totalDays }} 天</div>
            <div>历史最长 {{ streak.bestStreak }} 天</div>
          </div>
        </div>
        <div style="display:flex;gap:10px;">
          <div v-for="d in streak.week" :key="d.date" style="text-align:center;">
            <div :style="dotStyle(d)">{{ d.done ? '✓' : '' }}</div>
            <div style="font-size:12px;margin-top:4px;"
              :style="{ color: d.today ? '#2e7d5b' : '#7b8f82' }">{{ d.label }}</div>
          </div>
        </div>
      </div>
      <el-alert :title="streak.reminder" :type="streak.todayDone ? 'success' : 'warning'"
        :closable="false" show-icon style="margin-top:16px;" />
    </el-card>

    <el-card style="margin-bottom:20px;">
      <template #header>
        <b>运动打卡</b>
      </template>
      <el-form :model="form" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="运动项目">
              <el-select v-model="form.sportType" filterable allow-create default-first-option
                placeholder="选择或输入运动项目" style="width:100%">
                <el-option v-for="m in metList" :key="m.activity"
                  :label="`${m.activity}（MET ${m.met}）`" :value="m.activity" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="时长(分)">
              <el-input-number v-model="form.duration" :min="1" :max="600" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="消耗热量">
              <div style="display:flex;gap:8px;width:100%;align-items:center;">
                <el-input-number v-model="form.consumeCalorie" :min="0" :precision="1" style="flex:1" />
                <el-button type="primary" plain @click="openCalc">热量换算</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="打卡日期">
              <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-collapse v-model="detailOpen" style="margin-bottom:12px;">
          <el-collapse-item name="detail">
            <template #title>
              <span style="color:#7b8f82;font-size:13px;">力量训练细节（选填，散步、跑步、瑜伽等可跳过）</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="8" :xs="24">
                <el-form-item label="训练动作">
                  <el-input v-model="form.exerciseName" placeholder="如:深蹲/卧推" />
                </el-form-item>
              </el-col>
              <el-col :span="8" :xs="24">
                <el-form-item label="组数">
                  <el-input-number v-model="form.sets" :min="1" :max="50" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8" :xs="24">
                <el-form-item label="负重(kg)">
                  <el-input-number v-model="form.weight" :min="0" :max="500" :precision="1" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="酸痛部位">
                  <el-input v-model="form.soreArea" placeholder="如:肩部/膝部，无则留空" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="handleAdd" style="width:100%">
          提交打卡
        </el-button>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-head">
          <b>我的运动记录</b>
          <div style="display:flex;gap:8px;align-items:center;">
            <el-date-picker v-model="exportMonth" type="month" value-format="YYYY-MM"
              placeholder="选择月份" style="width:140px" />
            <el-button type="success" plain :loading="exporting" @click="handleExport">导出Excel</el-button>
          </div>
        </div>
      </template>
      <el-table :data="list" border stripe>
        <el-table-column prop="recordDate" label="日期" width="110" />
        <el-table-column prop="sportType" label="项目" width="100" />
        <el-table-column label="训练详情" min-width="180">
          <template #default="{ row }">{{ detailText(row) }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分)" width="90" />
        <el-table-column prop="consumeCalorie" label="热量(kcal)" width="100" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @current-change="load"
        @size-change="load"
        v-if="total > 0"
      />
    </el-card>

    <el-dialog v-model="calcVisible" title="热量换算" width="440px">
      <el-form label-width="80px">
        <el-form-item label="运动项目">
          <el-select v-model="calcForm.activity" filterable style="width:100%" @change="doCalc">
            <el-option v-for="m in metList" :key="m.activity"
              :label="`${m.activity}（MET ${m.met}）`" :value="m.activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分)">
          <el-input-number v-model="calcForm.minutes" :min="1" :max="600" style="width:100%" @change="doCalc" />
        </el-form-item>
      </el-form>
      <el-alert v-if="calcResult" type="success" :closable="false" style="margin-bottom:8px;">
        <div style="font-size:16px;">
          {{ calcResult.activity }} {{ calcResult.minutes }} 分钟约消耗
          <b style="font-size:20px;">{{ calcResult.calorie }}</b> kcal
        </div>
        <div style="font-size:12px;color:#7b8f82;margin-top:4px;">
          MET {{ calcResult.met }} × 3.5 × 体重 {{ calcResult.weight }}kg ÷ 200 × {{ calcResult.minutes }} 分钟
        </div>
        <div style="font-size:12px;color:#7b8f82;">{{ calcResult.note }}</div>
      </el-alert>
      <div style="font-size:12px;color:#7b8f82;">
        国际通用公式：kcal = MET × 3.5 × 体重(kg) ÷ 200 × 时长(分钟)
      </div>
      <template #footer>
        <el-button @click="calcVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!calcResult" @click="applyCalc">填入消耗热量</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addSport, sportPage, deleteSport, getMetList, calcSportCalorie, getSportStreak } from '../api/sport'
import { exportSportExcel } from '../api/export'

const loading = ref(false)
const exporting = ref(false)
const exportMonth = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const metList = ref([])
const detailOpen = ref([])
const streak = ref(null)

const dotStyle = (d) => ({
  width: '26px',
  height: '26px',
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '13px',
  background: d.done ? '#3a7d5c' : '#eef4ea',
  color: d.done ? '#fff' : '#a9baae',
  border: d.today ? '2px solid #2e7d5b' : '2px solid transparent',
  boxSizing: 'border-box'
})

const localDate = () => {
  const d = new Date()
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
}

const form = reactive({
  sportType: '',
  exerciseName: '',
  sets: null,
  weight: null,
  duration: 30,
  consumeCalorie: 0,
  soreArea: '',
  remark: '',
  recordDate: localDate()
})

const detailText = (row) => {
  const parts = []
  if (row.exerciseName) parts.push(row.exerciseName)
  if (row.sets) parts.push(row.sets + '组')
  if (row.weight) parts.push(row.weight + 'kg')
  if (row.soreArea) parts.push('酸痛：' + row.soreArea)
  return parts.length ? parts.join(' · ') : '—'
}

const handleAdd = async () => {
  if (!form.sportType || !form.duration) return ElMessage.warning('请填写运动项目和时长')
  loading.value = true
  try {
    const res = await addSport(form)
    ElMessage.success(res.msg)
    form.exerciseName = ''
    form.sets = null
    form.weight = null
    form.soreArea = ''
    form.remark = ''
    form.consumeCalorie = 0
    load()
    loadStreak()
  } catch (e) {
    ElMessage.error(e.message || '打卡失败')
  } finally {
    loading.value = false
  }
}

const load = async () => {
  try {
    const res = await sportPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.records || []
    total.value = Number(res.data.total || 0)
  } catch (e) { /* ignore */ }
}

const handleExport = async () => {
  exporting.value = true
  try {
    await exportSportExcel(exportMonth.value || localDate().slice(0, 7))
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
    const res = await deleteSport(id)
    ElMessage.success(res.msg)
    load()
    loadStreak()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

/* 热量换算 */
const calcVisible = ref(false)
const calcForm = reactive({ activity: '', minutes: 30 })
const calcResult = ref(null)

const openCalc = () => {
  const supported = metList.value.some((m) => m.activity === form.sportType)
  calcForm.activity = supported ? form.sportType : (metList.value[0]?.activity || '')
  calcForm.minutes = form.duration || 30
  calcResult.value = null
  calcVisible.value = true
  if (calcForm.activity) doCalc()
}

const doCalc = async () => {
  if (!calcForm.activity || !calcForm.minutes) return
  try {
    const res = await calcSportCalorie({ activity: calcForm.activity, minutes: calcForm.minutes })
    calcResult.value = res.data
  } catch (e) {
    calcResult.value = null
    ElMessage.error(e.message || '换算失败')
  }
}

const applyCalc = () => {
  if (!calcResult.value) return
  form.consumeCalorie = calcResult.value.calorie
  if (!form.sportType) form.sportType = calcResult.value.activity
  calcVisible.value = false
  ElMessage.success('已填入消耗热量')
}

const loadMetList = async () => {
  try {
    const res = await getMetList()
    metList.value = res.data || []
  } catch (e) { /* ignore */ }
}

const loadStreak = async () => {
  try {
    const res = await getSportStreak()
    streak.value = res.data
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  exportMonth.value = localDate().slice(0, 7)
  loadMetList()
  loadStreak()
  load()
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

@media (max-width: 768px) {
  .card-head > div {
    flex-wrap: wrap;
  }

  .card-head :deep(.el-date-editor) {
    width: 132px !important;
  }
}
</style>
