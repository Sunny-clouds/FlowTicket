import request from './request'

export function fetchDashboardStats() {
  return request({ url: '/stats/dashboard', method: 'get' })
}

export function fetchTicketStats(params) {
  return request({ url: '/stats/logs', method: 'get', params })
}

export function fetchMyHandleLogs() {
  return request({ url: '/stats/my-handle-logs', method: 'get' })
}
