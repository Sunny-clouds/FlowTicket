import request from './request'

export function fetchDashboardStats() {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}

export function fetchOperationLogs() {
  return request({
    url: '/stats/logs',
    method: 'get'
  })
}

export function fetchMyHandleLogs() {
  return request({
    url: '/stats/my-handle-logs',
    method: 'get'
  })
}
