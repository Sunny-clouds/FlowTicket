<template>
  <el-drawer
    :model-value="modelValue"
    title="工单详情"
    direction="rtl"
    size="520px"
    :destroy-on-close="false"
    custom-class="ticket-detail-drawer"
    @close="closeDrawer"
  >
    <div v-loading="loading" class="drawer-body">
      <table class="detail-table">
        <tbody>
          <tr><th>标题</th><td>{{ ticket.title || '-' }}</td></tr>
          <tr><th>分类</th><td>{{ ticket.categoryName || '-' }}</td></tr>
          <tr><th>优先级</th><td><el-tag :type="priorityType(ticket.priority)">{{ priorityLabel(ticket.priority) }}</el-tag></td></tr>
          <tr><th>状态</th><td><el-tag :type="statusType(ticket.status)">{{ ticket.statusName || statusLabel(ticket.status) }}</el-tag></td></tr>
          <tr><th>联系人</th><td>{{ ticket.contactName || '-' }}</td></tr>
          <tr><th>电话</th><td>{{ ticket.contactPhone || '-' }}</td></tr>
          <tr><th>邮箱</th><td>{{ ticket.contactEmail || '-' }}</td></tr>
          <tr><th>描述</th><td>{{ ticket.content || '-' }}</td></tr>
          <tr v-if="ticket.handleResult"><th>处理结果</th><td>{{ ticket.handleResult }}</td></tr>
        </tbody>
      </table>

      <div v-if="canProcess" class="drawer-section">
        <h3>处理工单</h3>
        <el-input v-model="processForm.content" type="textarea" :rows="3" placeholder="处理说明" />
        <el-input v-model="processForm.handleResult" type="textarea" :rows="3" placeholder="处理结果" />
        <div class="drawer-actions">
          <el-button type="primary" @click="processCurrentTicket">提交用户确认</el-button>
          <el-button type="danger" @click="rejectCurrentTicket">驳回</el-button>
        </div>
      </div>

      <div v-if="canClose" class="drawer-section compact">
        <el-button type="success" @click="closeCurrentTicket">确认完成</el-button>
      </div>

      <div v-if="canShowUrge" class="drawer-section compact urge-row">
        <el-button type="warning" :disabled="!ticket.canUrge" @click="urgeCurrentTicket">催促处理</el-button>
        <span>{{ ticket.urgeMessage }}</span>
      </div>

      <div class="drawer-section">
        <h3>回复记录</h3>
        <el-timeline v-if="replies.length">
          <el-timeline-item v-for="reply in replies" :key="reply.id" :timestamp="formatDateTime(reply.createTime)">
            <strong>{{ reply.realName || reply.username || '用户' }}</strong>
            <p>{{ reply.content }}</p>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无回复" />

        <el-input v-model="replyContent" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="添加回复" />
        <div class="drawer-actions">
          <el-button type="primary" :disabled="!replyContent.trim()" @click="submitReply">回复</el-button>
        </div>
      </div>

      <div class="drawer-section">
        <h3>流转日志</h3>
        <el-timeline v-if="flows.length">
          <el-timeline-item v-for="item in flows" :key="item.id" :timestamp="formatDateTime(item.createTime)">
            {{ item.operationDesc || item.operationType }}
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无日志" />
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
/* global defineProps, defineEmits */
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { closeTicket, fetchTicketComments, fetchTicketDetail, fetchTicketLogs, processTicket, rejectTicket, replyTicket, urgeTicket } from '@/api/ticket'
import { priorityLabel, priorityType, statusLabel, statusType } from '@/utils/dicts'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { showAppToast, showErrorToast } from '@/utils/toast'

const props = defineProps({
  modelValue: Boolean,
  ticketId: [Number, String]
})

const emit = defineEmits(['update:modelValue', 'updated'])

const userStore = useUserStore()
const loading = ref(false)
const ticket = ref({})
const replies = ref([])
const flows = ref([])
const replyContent = ref('')
const processForm = reactive({ content: '', handleResult: '' })

const canProcess = computed(() => userStore.role === 'handler' && ticket.value.assigneeId === userStore.user?.id && [1, 2].includes(ticket.value.status))
const canClose = computed(() => userStore.role === 'user' && ticket.value.creatorId === userStore.user?.id && ticket.value.status === 3)
const canShowUrge = computed(() => userStore.role === 'user' && ticket.value.creatorId === userStore.user?.id && [1, 2].includes(ticket.value.status))

watch(
  () => [props.modelValue, props.ticketId],
  ([visible, id]) => {
    if (visible && id) loadDetail()
  },
  { immediate: true }
)

async function loadDetail() {
  loading.value = true
  try {
    const [detail, comments, logs] = await Promise.all([
      fetchTicketDetail(props.ticketId),
      fetchTicketComments(props.ticketId),
      fetchTicketLogs(props.ticketId)
    ])
    ticket.value = detail || {}
    replies.value = comments || []
    flows.value = logs || []
  } catch (error) {
    showErrorToast(error, '工单详情加载失败')
  } finally {
    loading.value = false
  }
}

function closeDrawer() {
  emit('update:modelValue', false)
}

async function submitReply() {
  try {
    await replyTicket(props.ticketId, { parentId: null, content: replyContent.value.trim() })
    replyContent.value = ''
    showAppToast({ message: '回复已添加' })
    emit('updated')
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
    await processTicket(props.ticketId, { ...processForm })
    showAppToast({ message: '工单已提交用户确认' })
    emit('updated')
    loadDetail()
  } catch (error) {
    showErrorToast(error, '提交处理结果失败')
  }
}

async function rejectCurrentTicket() {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回工单', { inputType: 'textarea' })
    await rejectTicket(props.ticketId, { reason: value })
    showAppToast({ message: '工单已驳回' })
    emit('updated')
    loadDetail()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showErrorToast(error, '驳回工单失败')
  }
}

async function closeCurrentTicket() {
  try {
    await closeTicket(props.ticketId)
    showAppToast({ message: '工单已完成' })
    emit('updated')
    loadDetail()
  } catch (error) {
    showErrorToast(error, '完成工单失败')
  }
}

async function urgeCurrentTicket() {
  try {
    await urgeTicket(props.ticketId)
    showAppToast({ message: '已催促处理' })
    emit('updated')
    loadDetail()
  } catch (error) {
    showErrorToast(error, '催促失败')
  }
}
</script>

<style scoped>
.drawer-body {
  padding: 0 2px 24px;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #e5e7eb;
  table-layout: fixed;
}

.detail-table th,
.detail-table td {
  padding: 11px 12px;
  border: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: top;
  line-height: 1.55;
}

.detail-table th {
  width: 58px;
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
}

.drawer-section {
  margin-top: 22px;
}

.drawer-section.compact {
  margin-top: 16px;
}

.drawer-section h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.drawer-section .el-input + .el-input {
  margin-top: 10px;
}

.drawer-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
  gap: 8px;
}

.urge-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.urge-row span {
  color: #64748b;
  font-size: 13px;
}

.el-timeline {
  padding-left: 4px;
}

.el-timeline p {
  margin: 8px 0 0;
  color: #334155;
  line-height: 1.6;
}
</style>
