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
            <el-button link type="primary" @click="$router.push(`/tickets/${row.id}`)">详情</el-button>
            <el-button v-if="userStore.role === 'user' && [3, 4].includes(row.status)" link type="success" @click="confirmClose(row)">确认关闭</el-button>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { PRIORITY, TICKET_STATUS, priorityLabel, priorityType, statusLabel, statusType } from '@/utils/dicts'
import { formatDateTime } from '@/utils/format'
import { closeTicket, fetchTickets } from '@/api/ticket'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', priority: '' })
const page = reactive({ pageNum: 1, pageSize: 10 })

const subtitle = computed(() => {
  if (userStore.role === 'admin') return '管理员可查看全部工单，并进入分配和统计流程。'
  if (userStore.role === 'handler') return '客服人员可查看分配给自己的工单并进行回复处理。'
  return '普通用户可查看自己提交的工单、回复并确认关闭。'
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

async function confirmClose(row) {
  await ElMessageBox.confirm(`确认关闭工单 ${row.ticketNo}？`, '确认操作', { type: 'warning' })
  await closeTicket(row.id)
  ElMessage.success('工单已关闭')
  loadTickets()
}

onMounted(loadTickets)
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
