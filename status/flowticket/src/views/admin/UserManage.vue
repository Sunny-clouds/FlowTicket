<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">维护账号、角色和启停状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="realName" label="姓名" width="130" />
        <el-table-column prop="nickname" label="昵称" width="130" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="130">
          <template #default="{ row }">
            <el-tag>{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" width="170">
          <template #default="{ row }">{{ formatTime(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        layout="total, sizes, prev, pager, next"
        :total="total"
        class="pager"
        @current-change="loadUsers"
        @size-change="loadUsers"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="560px">
      <el-form :model="form" label-width="86px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="头像"><el-input v-model="form.avatar" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" value="admin" />
            <el-option label="客服人员" value="handler" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createUser, deleteUser, fetchUsers, updateUser } from '@/api/user'

const roleMap = { admin: '管理员', handler: '客服人员', user: '普通用户' }
const users = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const page = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive(emptyForm())

function emptyForm() {
  return { id: '', username: '', password: '', nickname: '', realName: '', phone: '', email: '', avatar: '', role: 'user', status: 1 }
}

function roleText(role) {
  return roleMap[String(role || '').toLowerCase()] || role || '-'
}

function toBackendRole(role) {
  const map = { ADMIN: 'admin', SERVICE: 'handler', HANDLER: 'handler', USER: 'user' }
  return map[String(role || '').toUpperCase()] || String(role || '').toLowerCase()
}

function resetForm(data = emptyForm()) {
  Object.assign(form, data, { role: toBackendRole(data.role) })
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await fetchUsers(page)
    users.value = data?.rows || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  resetForm({ ...emptyForm(), ...row, password: '' })
  dialogVisible.value = true
}

async function save() {
  const payload = { ...form, role: toBackendRole(form.role) }
  if (!payload.password) delete payload.password
  if (payload.id) {
    await updateUser(payload.id, payload)
  } else {
    delete payload.id
    await createUser(payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadUsers()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username} 吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadUsers()
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

onMounted(loadUsers)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
