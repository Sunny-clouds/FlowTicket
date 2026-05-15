import { defineStore } from 'pinia'
import { fetchMessages, fetchUnreadCount, markAllMessagesRead, markMessageRead } from '@/api/message'
import { playNotificationSound } from '@/utils/notificationSound'
import { showAppToast } from '@/utils/toast'

function apiBaseUrl() {
  return process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api'
}

function isTicketNotice(notice) {
  return Boolean(notice?.ticketId || String(notice?.messageType || '').startsWith('TICKET_'))
}

export const useMessageStore = defineStore('message', {
  state: () => ({
    messages: [],
    unreadCount: 0,
    eventSource: null
  }),
  actions: {
    async loadMessages() {
      this.messages = await fetchMessages()
      return this.messages
    },
    async loadUnreadCount() {
      this.unreadCount = Number(await fetchUnreadCount()) || 0
      return this.unreadCount
    },
    async markRead(id) {
      await markMessageRead(id)
      const message = this.messages.find((item) => item.id === id)
      if (message && Number(message.isRead) === 0) {
        message.isRead = 1
        this.unreadCount = Math.max(0, this.unreadCount - 1)
      }
    },
    async markAllRead() {
      await markAllMessagesRead()
      this.messages.forEach((message) => {
        message.isRead = 1
      })
      this.unreadCount = 0
    },
    connect(token) {
      if (!token || this.eventSource) return

      // EventSource 不支持自定义请求头，后端会从 token 参数中校验登录态。
      const streamUrl = `${apiBaseUrl()}/messages/stream?token=${encodeURIComponent(token)}`
      this.eventSource = new EventSource(streamUrl)

      this.eventSource.addEventListener('message', (event) => {
        const notice = JSON.parse(event.data)
        this.upsertRealtimeMessage(notice)
        this.emitTicketUpdated(notice)
        if (isTicketNotice(notice)) {
          playNotificationSound()
        }
        showAppToast({
          type: 'info',
          message: notice.title || notice.content || '你有一条新的站内消息',
          duration: 7000
        })
      })

      // EventSource 自带重连能力，网络短暂抖动时让浏览器自动恢复连接。
      this.eventSource.onerror = () => {}
    },
    disconnect() {
      if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
      }
    },
    upsertRealtimeMessage(notice) {
      const index = this.messages.findIndex((item) => item.id === notice.id)
      if (index >= 0) {
        this.messages.splice(index, 1, notice)
      } else {
        this.messages.unshift(notice)
      }
      this.unreadCount = Number(notice.unreadCount) || this.unreadCount + 1
    },
    emitTicketUpdated(notice) {
      if (!notice?.ticketId) return
      // 通知当前打开的工单页面刷新列表或详情，保持状态实时同步。
      window.dispatchEvent(new CustomEvent('flowticket-ticket-updated', { detail: notice }))
    },
    reset() {
      this.disconnect()
      this.messages = []
      this.unreadCount = 0
    }
  }
})
