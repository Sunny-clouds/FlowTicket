<template>
  <el-container class="app-layout">
    <el-aside class="sidebar" width="232px">
      <div class="brand">
        <div class="brand-mark">FT</div>
        <div>
          <strong>FlowTicket</strong>
          <span>工单协同平台</span>
        </div>
      </div>

      <el-menu
        router
        :default-active="$route.path"
        background-color="#111827"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <span>{{ item.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>清晰分派、跟进和度量每一张工单</p>
        </div>

        <div class="user-area">
          <el-tag effect="plain">{{ userStore.roleLabel }}</el-tag>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="profile">
              <el-avatar :size="32">{{ avatarText }}</el-avatar>
              <span>{{ userStore.user?.realName || userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ userStore.user?.department }}</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { asyncMenus } from '@/router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = computed(() => asyncMenus.filter((item) => item.meta.roles.includes(userStore.role)))
const currentTitle = computed(() => route.meta.title || '工作台')
const avatarText = computed(() => (userStore.user?.realName || userStore.user?.username || 'U').slice(0, 1))

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.sidebar {
  background: #111827;
  color: #fff;
  overflow: hidden;
}

.brand {
  height: 68px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-weight: 800;
  background: #2563eb;
}

.brand strong,
.brand span {
  display: block;
}

.brand span {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.el-menu {
  border-right: none;
}

.topbar {
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.topbar h1 {
  margin: 0;
  font-size: 18px;
}

.topbar p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.user-area,
.profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile {
  cursor: pointer;
  color: #334155;
  outline: none;
}

.content {
  padding: 22px;
  background: #f5f7fb;
}
</style>
