<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据统计</h2>
        <p class="page-subtitle">管理员查看工单规模、分类分布和客服处理排行。</p>
      </div>
      <el-button type="primary" @click="loadStats">刷新统计</el-button>
    </div>

    <div class="stat-grid">
      <el-card v-for="item in cards" :key="item.label" shadow="never">
        <div class="stat-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <em>{{ item.desc }}</em>
        </div>
      </el-card>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <h3 class="panel-title">分类分布</h3>
        <div ref="categoryRef" class="chart"></div>
      </div>
      <div class="panel">
        <h3 class="panel-title">客服处理排行</h3>
        <div ref="handlerRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardStats } from '@/api/stats'

const categoryRef = ref()
const handlerRef = ref()
const stats = ref({})
let categoryChart
let handlerChart

const cards = computed(() => [
  { label: '今日新增', value: stats.value.todayNewTickets || 0, desc: '今日提交工单' },
  { label: '待受理', value: stats.value.pendingTickets || 0, desc: '需要管理员分配' },
  { label: '处理中', value: stats.value.processingTickets || 0, desc: '客服处理中' },
  { label: '已关闭', value: stats.value.closedTickets || 0, desc: '用户确认关闭' }
])

function pickValue(item, keys, fallback = 0) {
  const key = keys.find((name) => Object.prototype.hasOwnProperty.call(item, name))
  return key ? item[key] : fallback
}

function renderCharts() {
  const categories = stats.value.categoryDistribution || []
  const handlers = stats.value.handlerRanking || []
  const categoryData = categories.map((item) => ({
    name: pickValue(item, ['categoryName', 'name', 'label'], '未分类'),
    value: pickValue(item, ['count', 'value', 'total'], 0)
  }))
  const handlerNames = handlers.map((item) => pickValue(item, ['handlerName', 'realName', 'name', 'label'], '未命名'))
  const handlerValues = handlers.map((item) => pickValue(item, ['count', 'value', 'total'], 0))

  if (!categoryChart) categoryChart = echarts.init(categoryRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['42%', '68%'], center: ['50%', '42%'], data: categoryData }]
  })

  if (!handlerChart) handlerChart = echarts.init(handlerRef.value)
  handlerChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 18, top: 24, bottom: 38 },
    xAxis: { type: 'category', data: handlerNames },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: handlerValues, color: '#2563eb', barWidth: 32 }]
  })
}

async function loadStats() {
  stats.value = await fetchDashboardStats()
  await nextTick()
  renderCharts()
}

function resizeCharts() {
  categoryChart?.resize()
  handlerChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  categoryChart?.dispose()
  handlerChart?.dispose()
})
</script>

<style scoped>
.stat-card span,
.stat-card strong,
.stat-card em {
  display: block;
}

.stat-card span {
  color: #64748b;
}

.stat-card strong {
  margin-top: 8px;
  font-size: 28px;
  color: #111827;
}

.stat-card em {
  margin-top: 6px;
  color: #64748b;
  font-style: normal;
  font-size: 13px;
}

.chart {
  height: 340px;
}
</style>
