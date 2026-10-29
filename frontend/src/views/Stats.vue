<template>
  <el-card style="max-width:1000px;margin:0 auto;">
    <template #header>
      <div class="chart-head">
        <b>健康数据可视化</b>
        <el-radio-group v-model="days" size="small" class="range-group" @change="load">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
          <el-radio-button :value="90">近90天</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <div ref="weightRef" class="chart chart-main"></div>
    <div ref="habitRef" class="chart chart-sub"></div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getTrend } from '../api/stats'

const days = ref(30)
const weightRef = ref(null)
const habitRef = ref(null)
let weightChart = null
let habitChart = null


const render = (data) => {
  weightChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['体重(kg)', '体脂率(%)'], bottom: 0, itemGap: 24 },
    grid: { left: 55, right: 55, top: 30, bottom: 55 },
    xAxis: { type: 'category', data: data.dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: 'kg', scale: true },
      { type: 'value', name: '%', scale: true }
    ],
    series: [
      {
        name: '体重(kg)', type: 'line', smooth: true, connectNulls: true,
        data: data.weight, itemStyle: { color: '#1F5C45' }
      },
      {
        name: '体脂率(%)', type: 'line', smooth: true, connectNulls: true,
        yAxisIndex: 1, data: data.bodyFat, itemStyle: { color: '#7fae56' }
      }
    ]
  }, true)

  habitChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['运动时长(分钟)', '饮食摄入(kcal)'], bottom: 0, itemGap: 24 },
    grid: { left: 55, right: 55, top: 30, bottom: 55 },
    xAxis: { type: 'category', data: data.dates },
    yAxis: [
      { type: 'value', name: '分钟' },
      { type: 'value', name: 'kcal', scale: true }
    ],
    series: [
      {
        name: '运动时长(分钟)', type: 'bar',
        data: data.sportDuration, itemStyle: { color: '#2E7D5B', borderRadius: [6, 6, 0, 0] }
      },
      {
        name: '饮食摄入(kcal)', type: 'line', smooth: true, yAxisIndex: 1,
        data: data.calorie, itemStyle: { color: '#a3c868' }
      }
    ]
  }, true)
}

const load = async () => {
  try {
    const res = await getTrend(days.value)
    render(res.data)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

const resize = () => {
  weightChart && weightChart.resize()
  habitChart && habitChart.resize()
}

onMounted(() => {
  weightChart = echarts.init(weightRef.value)
  habitChart = echarts.init(habitRef.value)
  window.addEventListener('resize', resize)
  load()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  weightChart && weightChart.dispose()
  habitChart && habitChart.dispose()
})
</script>

<style scoped>
.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.chart {
  width: 100%;
}

.chart-main {
  height: 340px;
}

.chart-sub {
  height: 320px;
  margin-top: 18px;
}

@media (max-width: 768px) {
  .chart-main {
    height: 280px;
  }

  .chart-sub {
    height: 260px;
    margin-top: 12px;
  }

  .range-group {
    margin-right: 0;
  }

  .chart-head {
    gap: 8px;
  }
}
</style>
