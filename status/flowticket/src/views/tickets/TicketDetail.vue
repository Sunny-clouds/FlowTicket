<template>
  <div class="page ticket-detail-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">工单详情</h2>
        <p class="page-subtitle">查看工单基本信息、处理进度和回复记录。</p>
      </div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button v-if="canClose" type="success" @click="closeCurrentTicket">确认完成</el-button>
      </div>
    </div>

    <div class="detail-grid">
      <div class="panel">
        <h3 class="panel-title">基本信息</h3>
        <el-descriptions v-loading="loading" :column="2" border>
          <el-descriptions-item label="工单号">{{ ticket.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ ticket.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ ticket.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityType(ticket.priority)">{{ priorityLabel(ticket.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(ticket.status)">{{ ticket.statusName || statusLabel(ticket.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交人">{{ ticket.creatorName }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ ticket.assigneeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(ticket.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ ticket.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ ticket.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ ticket.contactEmail }}</el-descriptions-item>
          <el-descriptions-item label="处理结果" :span="2">{{ ticket.handleResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">{{ ticket.content }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="canProcess" class="process-actions">
          <el-input v-model="processForm.content" placeholder="处理说明" />
          <el-input v-model="processForm.handleResult" placeholder="处理结果" />
          <el-button type="primary" @click="processCurrentTicket">提交待确认</el-button>
          <el-button type="danger" @click="rejectCurrentTicket">驳回</el-button>
        </div>
      </div>

      <div class="panel">
        <h3 class="panel-title">流转记录</h3>
        <el-timeline>
          <el-timeline-item v-for="item in flows" :key="item.id" :timestamp="formatDateTime(item.createTime)">
            {{ item.operationDesc || item.operationType }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <div class="panel">
      <h3 class="panel-title">回复记录</h3>
      <div class="reply-list">
        <div v-for="reply in replies" :key="reply.id" class="reply-item">
          <el-avatar :size="36">{{ (reply.realName || reply.username || 'U').slice(0, 1) }}</el-avatar>
          <div>
            <div class="reply-meta">
              <strong>{{ reply.realName || reply.username }}</strong>
              <span>{{ formatDateTime(reply.createTime) }}</span>
            </div>
            <p>{{ reply.content }}</p>
          </div>
        </div>
        <el-empty v-if="!replies.length" description="暂无回复" />
      </div>

      <el-divider />
      <el-input v-model="replyContent" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入回复内容" />
      <div class="reply-actions">
        <el-button type="primary" :disabled="!replyContent.trim()" @click="submitReply">添加回复</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { priorityLabel, priorityType, statusLabel, statusType } from '@/utils/dicts'
import { formatDateTime } from '@/utils/format'
import { closeTicket, fetchTicketComments, fetchTicketDetail, fetchTicketLogs, processTicket, rejectTicket, replyTicket } from '@/api/ticket'
import { useUserStore } from '@/stores/user'
import { showAppToast, showErrorToast } from '@/utils/toast'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const replyContent = ref('')
const ticket = ref({})
const flows = ref([])
const replies = ref([])
const processForm = reactive({ content: '', handleResult: '' })

const canProcess = computed(() => userStore.role === 'handler' && ticket.value.assigneeId === userStore.user?.id && [1, 2].includes(ticket.value.status))
const canClose = computed(() => userStore.role === 'user' && ticket.value.creatorId === userStore.user?.id && ticket.value.status === 3)

async function loadDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const [detail, comments, logs] = await Promise.all([
      fetchTicketDetail(id),
      fetchTicketComments(id),
      fetchTicketLogs(id)
    ])
    ticket.value = detail || {}
    replies.value = comments || []
    flows.value = logs || []
  } finally {
    loading.value = false
  }
}

async function submitReply() {
  try {
    await replyTicket(route.params.id, { parentId: null, content: replyContent.value.trim() })
    replyContent.value = ''
    showAppToast({ message: '回复已添加' })
    loadDetail()
  } catch (error) {
    showErrorToast(error, '回复失败')
  }
}

async function processCurrentTicket() {
  if (!processForm.content || !processForm.handleResult) {
    ElMessage.warning('请填写处理说明和处理结果')
    return
  }
  try {
    await processTicket(route.params.id, { ...processForm })
    showAppToast({ message: '工单已提交用户确认' })
    loadDetail()
  } catch (error) {
    showErrorToast(error, '提交处理结果失败')
  }
}

async function rejectCurrentTicket() {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回工单', { inputType: 'textarea' })
    await rejectTicket(route.params.id, { reason: value })
    showAppToast({ message: '工单已驳回' })
    loadDetail()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showErrorToast(error, '驳回工单失败')
  }
}

async function closeCurrentTicket() {
  try {
    await closeTicket(route.params.id)
    showAppToast({ message: '工单已完成' })
    loadDetail()
  } catch (error) {
    showErrorToast(error, '完成工单失败')
  }
}

function handleTicketUpdated(event) {
  if (String(event.detail?.ticketId) === String(route.params.id)) {
    loadDetail()
  }
}

onMounted(() => {
  loadDetail()
  window.addEventListener('flowticket-ticket-updated', handleTicketUpdated)
})

onUnmounted(() => {
  window.removeEventListener('flowticket-ticket-updated', handleTicketUpdated)
})
</script>

<style scoped>
.ticket-detail-page {
  max-width: 1120px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
}

.process-actions {
  margin-top: 18px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto auto;
  gap: 10px;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reply-item {
  display: flex;
  gap: 12px;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reply-meta span {
  color: #64748b;
  font-size: 13px;
}

.reply-item p {
  margin: 8px 0 0;
  color: #334155;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

@media (max-width: 1000px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .process-actions {
    grid-template-columns: 1fr;
  }
}
</style>
