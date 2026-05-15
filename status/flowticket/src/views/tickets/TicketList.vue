<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">工单列表</h2>
        <p class="page-subtitle">{{ subtitle }}</p>
      </div>
      <el-button v-if="userStore.role === 'user'" type="primary" :icon="Plus" @click="$router.push('/tickets/create')">提交工单</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索工单号、标题或提交人" clearable style="width: 280px" @keyup.enter="loadTickets" />
        <div class="filters">
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="search">
            <el-option v-for="(item, key) in TICKET_STATUS" :key="key" :label="item.label" :value="Number(key)" />
          </el-select>
          <el-select v-model="query.priority" placeholder="优先级" clearable style="width: 140px" @change="search">
            <el-option v-for="(item, key) in PRIORITY" :key="key" :label="item.label" :value="Number(key)" />
          </el-select>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tickets" stripe style="margin-top: 16px">
        <el-table-column prop="ticketNo" label="工单号" width="150" />
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ row.statusName || statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提交人" width="120" />
        <el-table-column prop="assigneeName" label="处理人" width="120">
          <template #default="{ row }">{{ row.assigneeName || '未分配' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="userStore.role === 'user' && row.status === 3" link type="success" @click="confirmClose(row)">确认完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="loadTickets"
          @current-change="loadTickets"
        />
      </div>
    </div>

    <TicketDetailDrawer v-model="detailVisible" :ticket-id="activeTicketId" @updated="loadTickets" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { PRIORITY, TICKET_STATUS, priorityLabel, priorityType, statusLabel, statusType } from '@/utils/dicts'
import { formatDateTime } from '@/utils/format'
import { closeTicket, fetchTickets } from '@/api/ticket'
import { useUserStore } from '@/stores/user'
import { showAppToast, showErrorToast } from '@/utils/toast'
import TicketDetailDrawer from '@/components/TicketDetailDrawer.vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const detailVisible = ref(false)
const activeTicketId = ref(null)
const query = reactive({ keyword: '', status: '', priority: '' })
const page = reactive({ pageNum: 1, pageSize: 10 })

const subtitle = computed(() => {
  if (userStore.role === 'admin') return '管理员可查看全部工单，并进入分配和统计流程。'
  if (userStore.role === 'handler') return '客服人员可查看分配给自己的工单并进行回复处理。'
  return '普通用户可查看自己提交的工单、回复并确认完成。'
})

function buildParams() {
  return {
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    keyword: query.keyword || undefined,
    status: query.status === '' ? undefined : query.status,
    priority: query.priority === '' ? undefined : query.priority
  }
}

async function loadTickets() {
  loading.value = true
  try {
    const data = await fetchTickets(buildParams())
    tickets.value = data?.rows || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function search() {
  page.pageNum = 1
  loadTickets()
}

function resetSearch() {
  query.keyword = ''
  query.status = ''
  query.priority = ''
  search()
}

function openDetail(row) {
  activeTicketId.value = row.id
  detailVisible.value = true
  router.replace({ path: '/tickets', query: { ...route.query, detailId: row.id } })
}

function openDetailFromQuery() {
  if (!route.query.detailId) return
  activeTicketId.value = route.query.detailId
  detailVisible.value = true
}

async function confirmClose(row) {
  try {
    await ElMessageBox.confirm(`确认完成工单 ${row.ticketNo}？`, '确认操作', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await closeTicket(row.id)
    showAppToast({ message: '工单已完成' })
    loadTickets()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showErrorToast(error, '完成工单失败')
  }
}

function handleTicketUpdated() {
  loadTickets()
}

onMounted(() => {
  loadTickets()
  openDetailFromQuery()
  window.addEventListener('flowticket-ticket-updated', handleTicketUpdated)
})

onUnmounted(() => {
  window.removeEventListener('flowticket-ticket-updated', handleTicketUpdated)
})

watch(
  () => route.query.detailId,
  () => openDetailFromQuery()
)

watch(detailVisible, (visible) => {
  if (!visible && route.query.detailId) {
    const query = { ...route.query }
    delete query.detailId
    router.replace({ path: '/tickets', query })
  }
})
</script>

<style scoped>
.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
