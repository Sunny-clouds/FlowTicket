<template>
  <div class="login-page">
    <section class="login-visual">
      <div class="brand-line">
        <span>FlowTicket</span>
        <strong>企业级工单协同处理系统</strong>
      </div>
      <div class="metrics">
        <div>
          <b>96%</b>
          <span>准时响应率</span>
        </div>
        <div>
          <b>24h</b>
          <span>全流程跟进</span>
        </div>
        <div>
          <b>3</b>
          <span>角色协同</span>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <h1>登录工作台</h1>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入后端用户账号" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" size="large" type="password" placeholder="请输入密码" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button class="login-btn" type="primary" size="large" :loading="loading" @click="submit">
            登录系统
          </el-button>
        </el-form>

        <div class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  if (loading.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请先填写账号和密码')
    return
  }

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } catch (error) {
    ElMessage.error(error?.message || '登录失败，请检查账号或密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background: #eef3fb;
}

.login-visual {
  padding: 64px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
  background:
    linear-gradient(rgba(15, 23, 42, 0.62), rgba(15, 23, 42, 0.62)),
    url("https://images.unsplash.com/photo-1551434678-e076c223a692?auto=format&fit=crop&w=1400&q=80") center/cover;
}

.brand-line span,
.brand-line strong {
  display: block;
}

.brand-line span {
  font-size: 44px;
  font-weight: 800;
}

.brand-line strong {
  margin-top: 14px;
  font-size: 18px;
  font-weight: 500;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.metrics div {
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.36);
}

.metrics b,
.metrics span {
  display: block;
}

.metrics b {
  font-size: 26px;
}

.metrics span {
  margin-top: 6px;
  font-size: 13px;
  color: #dbeafe;
}

.login-panel {
  display: grid;
  place-items: center;
  padding: 32px;
}

.login-card {
  width: min(420px, 100%);
  padding: 34px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
}

.login-card h1 {
  margin: 0;
  font-size: 26px;
}

.login-card p {
  margin: 10px 0 26px;
  color: #64748b;
}

.login-btn {
  width: 100%;
}

.demo-tip,
.register-link {
  margin-top: 14px;
  color: #64748b;
  font-size: 13px;
}

.register-link a {
  color: #2563eb;
  font-weight: 600;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: 320px;
    padding: 36px;
  }
}
</style>
