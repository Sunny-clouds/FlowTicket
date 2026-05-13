<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">工作台概览</h2>
        <p class="page-subtitle">聚合工单处理进度、分类趋势和客服处理排行。</p>
      </div>
    </div>

    <div class="stat-grid">
      <el-card v-for="item in stats" :key="item.label" shadow="never">
        <div class="stat-card">
          <el-icon :class="item.type"><component :is="item.icon" /></el-icon>
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

    <div class="panel">
      <h3 class="panel-title">处理排行</h3>
      <el-table :data="dashboard.handlerRanking || []" stripe>
        <el-table-column prop="handlerName" label="处理人" min-width="160">
          <template #default="{ row }">{{ row.handlerName || row.name || row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="count" label="处理数量" width="140">
          <template #default="{ row }">{{ row.count || row.total || row.value || 0 }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardStats } from '@/api/stats'

const trendRef = ref()
const categoryRef = ref()
const dashboard = ref({})
let trendChart
let categoryChart

const stats = computed(() => [
  { label: '今日新增', value: dashboard.value.todayNewTickets || 0, icon: 'Plus', type: 'blue' },
  { label: '待处理', value: dashboard.value.pendingTickets || 0, icon: 'Warning', type: 'orange' },
  { label: '处理中', value: dashboard.value.processingTickets || 0, icon: 'Loading', type: 'blue' },
  { label: '已关闭', value: dashboard.value.closedTickets || 0, icon: 'CircleCheck', type: 'green' }
])

function pickLabel(row) {
  return row.date || row.day || row.categoryName || row.name || row.label || '-'
}

function pickValue(row) {
  return row.count || row.total || row.value || row.num || 0
}

function renderCharts() {
  const trend = dashboard.value.sevenDayTrend || []
  const categories = dashboard.value.categoryDistribution || []

  trendChart = trendChart || echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 18, top: 24, bottom: 28 },
    xAxis: { type: 'category', data: trend.map(pickLabel) },
    yAxis: { type: 'value' },
    series: [{ name: '新增', type: 'line', smooth: true, data: trend.map(pickValue), areaStyle: {}, color: '#2563eb' }]
  })

  categoryChart = categoryChart || echarts.init(categoryRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: '分类',
        type: 'pie',
        radius: ['48%', '70%'],
        center: ['50%', '42%'],
        data: categories.map((item) => ({ value: pickValue(item), name: pickLabel(item) }))
      }
    ]
  })
}

async function loadDashboard() {
  dashboard.value = await fetchDashboardStats()
  await nextTick()
  renderCharts()
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

.blue {
  color: #2563eb;
  background: #dbeafe;
}

.orange {
  color: #ea580c;
  background: #ffedd5;
}

.green {
  color: #16a34a;
  background: #dcfce7;
}

.chart {
  height: 320px;
}
</style>
