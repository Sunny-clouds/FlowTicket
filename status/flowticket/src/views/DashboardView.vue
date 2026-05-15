<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">后台首页</h2>
        <p class="page-subtitle">按角色聚合工单进度、响应效率和待处理事项。</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="loadDashboard">刷新数据</el-button>
    </div>

    <div class="stat-grid">
      <el-card v-for="item in cards" :key="item.label" shadow="never">
        <div class="stat-card">
          <el-icon :class="item.className"><component :is="item.icon" /></el-icon>
          <div>
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </el-card>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <h3 class="panel-title">近 7 日工单趋势</h3>
        <div ref="trendRef" class="chart"></div>
      </div>
      <div class="panel">
        <h3 class="panel-title">工单分类占比</h3>
        <div ref="categoryRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { fetchDashboardStats } from '@/api/stats'

const trendRef = ref()
const categoryRef = ref()
const stats = ref({})
let trendChart
let categoryChart

const cards = computed(() => [
  { label: '今日新增', value: stats.value.todayNewTickets || 0, icon: 'Plus', className: 'blue' },
  { label: '待受理', value: stats.value.pendingTickets || 0, icon: 'Bell', className: 'orange' },
  { label: '处理中', value: stats.value.processingTickets || 0, icon: 'Loading', className: 'purple' },
  { label: '待确认', value: stats.value.waitConfirmTickets || 0, icon: 'QuestionFilled', className: 'cyan' },
  { label: '已完成', value: stats.value.completedTickets || 0, icon: 'SuccessFilled', className: 'green' },
  { label: '已驳回', value: stats.value.rejectedTickets || 0, icon: 'CircleClose', className: 'red' }
])

function pickValue(item, keys, fallback = 0) {
  const key = keys.find((name) => Object.prototype.hasOwnProperty.call(item, name))
  return key ? item[key] : fallback
}

function renderCharts() {
  const trend = stats.value.sevenDayTrend || []
  const categories = stats.value.categoryDistribution || []
  const trendLabels = trend.map((item) => pickValue(item, ['date', 'day', 'name', 'label'], ''))
  const trendValues = trend.map((item) => pickValue(item, ['count', 'value', 'total'], 0))
  const categoryData = categories.map((item) => ({
    name: pickValue(item, ['categoryName', 'name', 'label'], '未分类'),
    value: pickValue(item, ['count', 'value', 'total'], 0)
  }))

  if (!trendChart) trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 18, top: 24, bottom: 28 },
    xAxis: { type: 'category', data: trendLabels },
    yAxis: { type: 'value' },
    series: [{ name: '工单数', type: 'line', smooth: true, data: trendValues, color: '#2563eb', areaStyle: { opacity: 0.12 } }]
  })

  if (!categoryChart) categoryChart = echarts.init(categoryRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ name: '分类', type: 'pie', radius: ['48%', '70%'], center: ['50%', '42%'], data: categoryData }]
  })
}

async function loadDashboard() {
  try {
    stats.value = await fetchDashboardStats()
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error('统计数据加载失败')
  }
}

function resizeCharts() {
  trendChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-card .el-icon {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  font-size: 22px;
}

.stat-card span,
.stat-card strong {
  display: block;
}

.stat-card span {
  color: #64748b;
  font-size: 13px;
}

.stat-card strong {
  margin-top: 6px;
  font-size: 26px;
}

.blue { color: #2563eb; background: #dbeafe; }
.orange { color: #ea580c; background: #ffedd5; }
.purple { color: #7c3aed; background: #ede9fe; }
.cyan { color: #0891b2; background: #cffafe; }
.green { color: #16a34a; background: #dcfce7; }
.slate { color: #475569; background: #e2e8f0; }
.red { color: #dc2626; background: #fee2e2; }

.chart {
  height: 320px;
}
</style>
