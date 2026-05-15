<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">工单分配</h2>
        <p class="page-subtitle">管理员可查看待受理工单，并分配给客服人员。</p>
      </div>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索工单号或标题" clearable style="width: 280px" @keyup.enter="loadTickets" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="search">
          <el-option v-for="(item, key) in TICKET_STATUS" :key="key" :label="item.label" :value="Number(key)" />
        </el-select>
        <el-select v-model="query.priority" placeholder="优先级" clearable style="width: 140px" @change="search">
          <el-option v-for="(item, key) in PRIORITY" :key="key" :label="item.label" :value="Number(key)" />
        </el-select>
        <el-select v-model="query.assigneeId" placeholder="当前处理人" clearable style="width: 160px" @change="search">
          <el-option label="未分配" :value="-1" />
          <el-option v-for="user in serviceUsers" :key="user.id" :label="user.realName || user.username" :value="user.id" />
        </el-select>
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
            <el-tag :type="statusType(row.status)">{{ row.statusName || statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提交人" width="120" />
        <el-table-column prop="assigneeName" label="当前处理人" width="130">
          <template #default="{ row }">{{ row.assigneeName || '未分配' }}</template>
        </el-table-column>
        <el-table-column label="分配" width="280" fixed="right">
          <template #default="{ row }">
            <el-select v-model="row.assigneeId" placeholder="选择客服" style="width: 160px">
              <el-option v-for="user in serviceUsers" :key="user.id" :label="user.realName || user.username" :value="user.id" />
            </el-select>
            <el-button type="primary" size="small" @click="assign(row)">确认</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadTickets" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { assignTicket, fetchTickets } from '@/api/ticket'
import { fetchUsers } from '@/api/user'
import { PRIORITY, TICKET_STATUS, priorityLabel, priorityType, statusLabel, statusType } from '@/utils/dicts'
import { showAppToast, showErrorToast } from '@/utils/toast'

const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const serviceUsers = ref([])
const query = reactive({ keyword: '', status: '', priority: '', assigneeId: '' })
const page = reactive({ pageNum: 1, pageSize: 10 })

function buildParams() {
  return {
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    keyword: query.keyword || undefined,
    status: query.status === '' ? undefined : query.status,
    priority: query.priority === '' ? undefined : query.priority,
    assigneeId: query.assigneeId === '' ? undefined : query.assigneeId
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

async function loadServiceUsers() {
  const data = await fetchUsers({ pageNum: 1, pageSize: 100 })
  serviceUsers.value = (data?.rows || []).filter((item) => String(item.role).toLowerCase() === 'handler')
}

function search() {
  page.pageNum = 1
  loadTickets()
}

async function assign(row) {
  if (!row.assigneeId) {
    ElMessage.warning('请选择客服人员')
    return
  }
  try {
    await assignTicket(row.id, { assigneeId: row.assigneeId })
    showAppToast({ message: '工单已分配' })
    loadTickets()
  } catch (error) {
    showErrorToast(error, '工单分配失败')
  }
}

onMounted(() => {
  loadTickets()
  loadServiceUsers()
  window.addEventListener('flowticket-ticket-updated', loadTickets)
})

onUnmounted(() => {
  window.removeEventListener('flowticket-ticket-updated', loadTickets)
})
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
