<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">个人中心</h2>
        <p class="page-subtitle">维护当前登录用户的基础信息和联系方式。</p>
      </div>
      <el-tag effect="plain">{{ userStore.roleLabel }}</el-tag>
    </div>

    <div class="profile-grid">
      <div class="panel profile-card">
        <el-avatar :size="72" :src="form.avatar">{{ avatarText }}</el-avatar>
        <h3>{{ form.realName }}</h3>
        <p>{{ form.username }}</p>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="账号">{{ form.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ userStore.roleLabel }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ form.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ form.email }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="panel">
        <h3 class="panel-title">资料编辑</h3>
        <el-form label-width="90px" :model="form">
          <el-form-item label="姓名">
            <el-input v-model="form.realName" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="头像">
            <el-input v-model="form.avatar" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateUser } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const saving = ref(false)
const form = reactive({
  id: userStore.user?.id,
  username: userStore.user?.username || '',
  realName: userStore.user?.realName || '',
  phone: userStore.user?.phone || '',
  email: userStore.user?.email || '',
  avatar: userStore.user?.avatar || '',
  role: userStore.user?.role,
  status: userStore.user?.status ?? 1
})

const avatarText = computed(() => (form.realName || form.username || 'U').slice(0, 1))

async function save() {
  saving.value = true
  try {
    await updateUser(form.id, {
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar,
      role: form.role,
      status: form.status
    })
    userStore.updateProfile(form)
    ElMessage.success('个人资料已保存')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
}

.profile-card {
  text-align: center;
}

.profile-card h3 {
  margin: 14px 0 6px;
}

.profile-card p {
  margin: 0 0 18px;
  color: #64748b;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
