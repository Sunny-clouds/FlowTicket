<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">客服处理台</h2>
        <p class="page-subtitle">面向客服人员的受理、备注和状态推进入口。</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="loadQueue">刷新队列</el-button>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="queue" stripe>
        <el-table-column prop="ticketNo" label="工单号" width="150" />
        <el-table-column prop="title" label="标题" min-width="230" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.priority === 3 ? 'danger' : row.priority === 2 ? 'warning' : 'info'">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提交人" width="120" />
        <el-table-column prop="assigneeName" label="处理人" width="120">
          <template #default="{ row }">{{ row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="120">
          <template #default="{ row }">{{ row.statusName || statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="处理动作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="accept(row)">受理</el-button>
            <el-button size="small" @click="openRemark(row)">备注</el-button>
            <el-button size="small" type="success" @click="openComplete(row)">解决</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="remarkVisible" title="处理备注" width="520px">
      <el-input v-model="remark" type="textarea" :rows="5" placeholder="记录沟通结论、处理动作或下一步计划" />
      <template #footer>
        <el-button @click="remarkVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存备注</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeVisible" title="解决工单" width="520px">
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
        <el-button type="primary" @click="saveComplete">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { addTicketComment, assignTicket, completeTicket, fetchTickets } from '@/api/ticket'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const remarkVisible = ref(false)
const completeVisible = ref(false)
const remark = ref('')
const currentTicket = ref(null)
const queue = ref([])
const completeForm = reactive({ content: '', handleResult: '' })

async function loadQueue() {
  loading.value = true
  try {
    const data = await fetchTickets({ pageNum: 1, pageSize: 50, status: 1 })
    queue.value = data?.rows || []
  } finally {
    loading.value = false
  }
}

async function accept(row) {
  await assignTicket(row.id, { assigneeId: userStore.user?.id })
  ElMessage.success(`已受理 ${row.ticketNo}`)
  loadQueue()
}

function openRemark(row) {
  currentTicket.value = row
  remark.value = ''
  remarkVisible.value = true
}

async function saveRemark() {
  await addTicketComment(currentTicket.value.id, { parentId: 0, content: remark.value })
  remarkVisible.value = false
  remark.value = ''
  ElMessage.success('备注已保存')
}

function openComplete(row) {
  currentTicket.value = row
  completeForm.content = ''
  completeForm.handleResult = ''
  completeVisible.value = true
}

async function saveComplete() {
  await completeTicket(currentTicket.value.id, { ...completeForm })
  completeVisible.value = false
  ElMessage.success('处理结果已提交')
  loadQueue()
}

function priorityText(priority) {
  return ({ 1: '低', 2: '中', 3: '高' })[priority] || priority || '-'
}

function statusText(status) {
  return ({ 1: '待受理', 2: '处理中', 3: '待确认', 4: '已关闭' })[status] || status || '-'
}

onMounted(loadQueue)
</script>
