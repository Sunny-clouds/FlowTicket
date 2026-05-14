<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理员维护账号、角色、联系方式和启停状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索账号、姓名或手机号" clearable style="width: 280px" />
        <el-select v-model="query.role" placeholder="角色" clearable style="width: 150px">
          <el-option label="管理员" value="admin" />
          <el-option label="客服人员" value="handler" />
          <el-option label="普通用户" value="user" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="filteredUsers" stripe style="margin-top: 16px">
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="realName" label="姓名" width="130" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="130">
          <template #default="{ row }">
            <el-tag>{{ ROLE_LABELS[normalizeRole(row.role)] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近登录" width="180">
          <template #default="{ row }">{{ formatDateTime(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item v-if="!editingUser" label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
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
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ROLE_LABELS, normalizeRole } from '@/utils/dicts'
import { formatDateTime } from '@/utils/format'
import { createUser, deleteUser, fetchUsers, updateUser } from '@/api/user'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const query = reactive({ keyword: '', role: '' })
const page = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const editingUser = ref(null)
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', email: '', role: 'user', status: 1 })

const filteredUsers = computed(() => users.value.filter((item) => {
  const keywordHit = !query.keyword || [item.username, item.realName, item.phone].some((value) => String(value || '').includes(query.keyword))
  const roleHit = !query.role || normalizeRole(item.role) === query.role
  return keywordHit && roleHit
}))

async function loadUsers() {
  loading.value = true
  try {
    const data = await fetchUsers({ pageNum: page.pageNum, pageSize: page.pageSize })
    users.value = data?.rows || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function search() {
  page.pageNum = 1
  loadUsers()
}

function fillForm(row = {}) {
  form.id = row.id || null
  form.username = row.username || ''
  form.password = ''
  form.realName = row.realName || ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.role = normalizeRole(row.role) || 'user'
  form.status = row.status ?? 1
}

function openCreate() {
  editingUser.value = null
  fillForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingUser.value = row
  fillForm(row)
  dialogVisible.value = true
}

async function saveUser() {
  const payload = {
    username: form.username,
    password: form.password || undefined,
    realName: form.realName,
    phone: form.phone,
    email: form.email,
    role: form.role,
    status: form.status
  }
  if (editingUser.value) await updateUser(form.id, payload)
  else await createUser(payload)
  dialogVisible.value = false
  ElMessage.success('用户已保存')
  loadUsers()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.realName || row.username}？`, '确认操作', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('用户已删除')
  loadUsers()
}

onMounted(loadUsers)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
