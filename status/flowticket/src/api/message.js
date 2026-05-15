import request from './request'

export function fetchMessages() {
  return request({ url: '/messages', method: 'get' })
}

export function fetchUnreadCount() {
  return request({ url: '/messages/unread-count', method: 'get' })
}

export function markMessageRead(id) {
  return request({ url: `/messages/${id}/read`, method: 'put' })
}

export function markAllMessagesRead() {
  return request({ url: '/messages/read-all', method: 'put' })
}
