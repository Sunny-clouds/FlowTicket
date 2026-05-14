<template>
  <div class="register-page">
    <div class="register-card">
      <h1>注册 FlowTicket</h1>
      <p>注册普通用户账号后即可提交和跟踪自己的工单。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" size="large" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" size="large" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" size="large" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" size="large" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="submit">注册账号</el-button>
      </el-form>

      <div class="login-link">
        已有账号？
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validatePassword = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePassword, trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await registerApi({
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      role: 'user',
      status: 1
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background: #eef3fb;
}

.register-card {
  width: min(520px, 100%);
  padding: 34px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.register-card h1 {
  margin: 0;
  font-size: 26px;
}

.register-card p {
  margin: 10px 0 24px;
  color: #64748b;
}

.submit-btn {
  width: 100%;
}

.login-link {
  margin-top: 16px;
  color: #475569;
}

.login-link a {
  color: #2563eb;
  font-weight: 600;
}
</style>
