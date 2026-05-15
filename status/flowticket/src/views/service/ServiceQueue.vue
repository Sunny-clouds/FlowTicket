<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">客服处理台</h2>
        <p class="page-subtitle">查看分配给自己的工单，完成回复、处理和状态推进。</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="loadTickets">刷新队列</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索工单号或标题" clearable style="width: 260px" @keyup.enter="search" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
          <el-option v-for="(item, key) in TICKET_STATUS" :key="key" :label="item.label" :value="Number(key)" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tickets" stripe style="margin-top: 16px">
        <el-table-column prop="ticketNo" label="工单号" width="150" />
        <el-table-column prop="title" label="标题" min-width="230" />
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
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="处理动作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="$router.push({ path: '/tickets', query: { detailId: row.id } })">处理</el-button>
            <el-button size="small" @click="openReply(row)">回复</el-button>
            <el-button size="small" type="success" @click="openComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadTickets" />
      </div>
    </div>

    <el-dialog v-model="replyDialog" title="回复工单" width="520px">
      <el-input v-model="replyContent" type="textarea" :rows="5" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyDialog = false">取消</el-button>
        <el-button type="primary" @click="saveReply">保存回复</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialog" title="完成处理" width="560px">
      <el-form label-width="90px">
        <el-form-item label="处理说明"><el-input v-model="completeForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="处理结果"><el-input v-model="completeForm.handleResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialog = false">取消</el-button>
        <el-button type="primary" @click="complete">提交用户确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchTickets, processTicket, replyTicket } from '@/api/ticket'
import { TICKET_STATUS, priorityLabel, priorityType, statusLabel, statusType } from '@/utils/dicts'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { showAppToast, showErrorToast } from '@/utils/toast'

const userStore = useUserStore()
const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const currentTicket = ref(null)
const replyDialog = ref(false)
const completeDialog = ref(false)
const replyContent = ref('')
const completeForm = reactive({ content: '', handleResult: '' })
const query = reactive({ keyword: '', status: '' })
const page = reactive({ pageNum: 1, pageSize: 10 })

async function loadTickets() {
  loading.value = true
  try {
    const data = await fetchTickets({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      assigneeId: userStore.user?.id,
      keyword: query.keyword || undefined,
      status: query.status === '' ? undefined : query.status
    })
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

function openReply(row) {
  currentTicket.value = row
  replyDialog.value = true
}

function openComplete(row) {
  currentTicket.value = row
  completeForm.content = ''
  completeForm.handleResult = ''
  completeDialog.value = true
}

async function saveReply() {
  try {
    await replyTicket(currentTicket.value.id, { parentId: null, content: replyContent.value })
    replyDialog.value = false
    replyContent.value = ''
    showAppToast({ message: '回复已保存' })
    loadTickets()
  } catch (error) {
    showErrorToast(error, '回复失败')
  }
}

async function complete() {
  try {
    await processTicket(currentTicket.value.id, { content: completeForm.content, handleResult: completeForm.handleResult })
    completeDialog.value = false
    showAppToast({ message: '工单已提交用户确认' })
    loadTickets()
  } catch (error) {
    showErrorToast(error, '提交处理结果失败')
  }
}

function handleTicketUpdated() {
  loadTickets()
}

onMounted(() => {
  loadTickets()
  window.addEventListener('flowticket-ticket-updated', handleTicketUpdated)
})

onUnmounted(() => {
  window.removeEventListener('flowticket-ticket-updated', handleTicketUpdated)
})
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
