<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">工单列表</h2>
        <p class="page-subtitle">查看、筛选和跟进权限范围内的工单。</p>
      </div>
      <el-button v-if="userStore.role === 'USER'" type="primary" :icon="Plus" @click="$router.push('/tickets/create')">提交工单</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索工单号、标题或提交人" clearable style="width: 260px" @keyup.enter="loadTickets" />
        <div class="filters">
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
            <el-option label="待受理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="待确认" :value="3" />
            <el-option label="已关闭" :value="4" />
          </el-select>
          <el-select v-model="query.priority" placeholder="优先级" clearable style="width: 140px">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
          <el-select v-model="query.categoryId" placeholder="分类" clearable style="width: 160px">
            <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadTickets">查询</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tickets" stripe style="margin-top: 16px">
        <el-table-column prop="ticketNo" label="工单号" width="150" />
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ row.statusName || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提交人" width="120" />
        <el-table-column prop="assigneeName" label="处理人" width="120">
          <template #default="{ row }">{{ row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="['ADMIN', 'SERVICE'].includes(userStore.role)" link type="primary" @click="openComplete(row)">处理</el-button>
            <el-button v-if="row.status !== 4" link type="danger" @click="close(row)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-sizes="[10, 20, 50]"
        class="pager"
        @current-change="loadTickets"
        @size-change="loadTickets"
      />
    </div>

    <el-drawer v-model="drawerVisible" title="工单详情" size="520px">
      <el-descriptions v-if="currentTicket" :column="1" border>
        <el-descriptions-item label="工单号">{{ currentTicket.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentTicket.title }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentTicket.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ priorityText(currentTicket.priority) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentTicket.statusName || statusText(currentTicket.status) }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentTicket.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentTicket.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentTicket.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentTicket.content }}</el-descriptions-item>
        <el-descriptions-item v-if="currentTicket.handleResult" label="处理结果">{{ currentTicket.handleResult }}</el-descriptions-item>
      </el-descriptions>

      <div class="drawer-section">
        <h3>回复记录</h3>
        <el-timeline>
          <el-timeline-item v-for="item in comments" :key="item.id" :timestamp="formatTime(item.createTime)">
            <strong>{{ item.nickname || item.username }}</strong>
            <p>{{ item.content }}</p>
          </el-timeline-item>
        </el-timeline>
        <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="添加回复" />
        <el-button type="primary" class="mt" @click="submitComment">回复</el-button>
      </div>

      <div class="drawer-section">
        <h3>流转日志</h3>
        <el-timeline>
          <el-timeline-item v-for="item in logs" :key="item.id" :timestamp="formatTime(item.createTime)">
            {{ item.operationDesc || item.operationType }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>

    <el-dialog v-model="completeVisible" title="处理工单" width="520px">
      <el-form :model="completeForm" label-width="90px">
        <el-form-item label="处理说明">
          <el-input v-model="completeForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="completeForm.handleResult" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { addTicketComment, closeTicket, completeTicket, fetchTicketComments, fetchTicketLogs, fetchTickets } from '@/api/ticket'
import { fetchCategories } from '@/api/category'

const userStore = useUserStore()
const drawerVisible = ref(false)
const completeVisible = ref(false)
const currentTicket = ref(null)
const completeTicketId = ref(null)
const tickets = ref([])
const categories = ref([])
const comments = ref([])
const logs = ref([])
const commentContent = ref('')
const loading = ref(false)
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '', priority: '', categoryId: '' })
const completeForm = reactive({ content: '', handleResult: '' })

function cleanParams(params) {
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== '' && value !== null && value !== undefined))
}

async function loadTickets() {
  loading.value = true
  try {
    const data = await fetchTickets(cleanParams(query))
    tickets.value = data?.rows || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await fetchCategories({ onlyEnabled: true })
}

async function openDetail(row) {
  currentTicket.value = row
  drawerVisible.value = true
  comments.value = await fetchTicketComments(row.id)
  logs.value = await fetchTicketLogs(row.id)
}

function openComplete(row) {
  completeTicketId.value = row.id
  completeForm.content = ''
  completeForm.handleResult = ''
  completeVisible.value = true
}

async function submitComplete() {
  await completeTicket(completeTicketId.value, { ...completeForm })
  ElMessage.success('处理结果已提交')
  completeVisible.value = false
  loadTickets()
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  await addTicketComment(currentTicket.value.id, { parentId: 0, content: commentContent.value.trim() })
  commentContent.value = ''
  comments.value = await fetchTicketComments(currentTicket.value.id)
  ElMessage.success('回复已提交')
}

async function close(row) {
  await ElMessageBox.confirm('确认关闭该工单吗？', '提示', { type: 'warning' })
  await closeTicket(row.id)
  ElMessage.success('工单已关闭')
  loadTickets()
}

function priorityText(priority) {
  return ({ 1: '低', 2: '中', 3: '高' })[priority] || priority || '-'
}

function priorityType(priority) {
  return priority === 3 ? 'danger' : priority === 2 ? 'warning' : 'info'
}

function statusText(status) {
  return ({ 1: '待受理', 2: '处理中', 3: '待确认', 4: '已关闭' })[status] || status || '-'
}

function statusType(status) {
  return ({ 1: 'info', 2: 'warning', 3: 'success', 4: '' })[status]
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

onMounted(() => {
  loadCategories()
  loadTickets()
})
</script>

<style scoped>
.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.drawer-section {
  margin-top: 22px;
}

.drawer-section h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.mt {
  margin-top: 10px;
}
</style>
