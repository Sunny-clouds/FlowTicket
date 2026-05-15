<template>
  <el-container class="app-layout">
    <el-aside class="sidebar" width="236px">
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
        <div class="topbar-title">
          <h1>{{ currentTitle }}</h1>
        </div>

        <div class="user-area">
          <el-popover placement="bottom-end" width="360" trigger="click" @show="messageStore.loadMessages()">
            <template #reference>
              <el-badge :value="messageStore.unreadCount" :hidden="messageStore.unreadCount === 0" :max="99">
                <el-button class="notice-button" circle :icon="Bell" />
              </el-badge>
            </template>
            <div class="notice-panel">
              <div class="notice-header">
                <strong>消息提醒</strong>
                <el-button link type="primary" :disabled="messageStore.unreadCount === 0" @click="messageStore.markAllRead()">
                  全部已读
                </el-button>
              </div>
              <el-scrollbar max-height="360px">
                <div v-if="messageStore.messages.length === 0" class="notice-empty">暂无消息</div>
                <div
                  v-for="message in messageStore.messages"
                  :key="message.id"
                  class="notice-item"
                  :class="{ unread: Number(message.isRead) === 0 }"
                  @click="openMessage(message)"
                >
                  <div class="notice-title">{{ message.title }}</div>
                  <div class="notice-content">{{ message.content }}</div>
                  <div class="notice-time">{{ formatNoticeTime(message.createTime) }}</div>
                </div>
              </el-scrollbar>
            </div>
          </el-popover>
          <el-tag effect="plain">{{ userStore.roleLabel }}</el-tag>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="profile">
              <el-avatar :size="32">{{ avatarText }}</el-avatar>
              <span>{{ userStore.user?.realName || userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item disabled>{{ userStore.user?.email || userStore.user?.phone || '未设置联系方式' }}</el-dropdown-item>
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
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { asyncMenus } from '@/router'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'
import { showAppToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()

const menus = computed(() => asyncMenus.filter((item) => !item.meta.hidden && item.meta.roles.includes(userStore.role)))
const currentTitle = computed(() => route.meta.title || 'FlowTicket')
const avatarText = computed(() => (userStore.user?.realName || userStore.user?.username || 'U').slice(0, 1))

function handleCommand(command) {
  if (command === 'profile') router.push('/profile')
  if (command === 'logout') {
    messageStore.reset()
    userStore.logout()
    showAppToast({ message: '已退出登录' })
    router.push('/login')
  }
}

function formatNoticeTime(time) {
  if (!time) return ''
  return String(time).replace('T', ' ').slice(0, 19)
}

async function openMessage(message) {
  if (Number(message.isRead) === 0) {
    await messageStore.markRead(message.id)
  }
  if (message.ticketId) {
    router.push({ path: '/tickets', query: { detailId: message.ticketId } })
  }
}

function startMessageNotice() {
  if (!userStore.token) return
  messageStore.loadUnreadCount()
  messageStore.loadMessages()
  messageStore.connect(userStore.token)
}

onMounted(startMessageNotice)
onUnmounted(() => messageStore.disconnect())

watch(
  () => userStore.token,
  (token) => {
    messageStore.reset()
    if (token) startMessageNotice()
  }
)
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

.notice-button {
  color: #475569;
}

.notice-panel {
  padding: 2px 0;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2px 10px;
  border-bottom: 1px solid #eef2f7;
}

.notice-empty {
  padding: 28px 0;
  color: #94a3b8;
  text-align: center;
}

.notice-item {
  padding: 12px 4px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
}

.notice-item:hover {
  background: #f8fafc;
}

.notice-item.unread .notice-title::before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 7px;
  border-radius: 50%;
  background: #ef4444;
  vertical-align: 1px;
}

.notice-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.notice-content {
  margin-top: 5px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.notice-time {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
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
