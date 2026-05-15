export const TICKET_STATUS = {
  1: { label: '待受理', type: 'info' },
  2: { label: '处理中', type: 'warning' },
  3: { label: '待确认', type: 'primary' },
  4: { label: '已完成', type: 'success' },
  5: { label: '已驳回', type: 'danger' }
}

export const PRIORITY = {
  1: { label: '低', type: 'info' },
  2: { label: '中', type: 'warning' },
  3: { label: '高', type: 'danger' },
  4: { label: '紧急', type: 'danger' }
}

export const ROLE_LABELS = {
  admin: '管理员',
  handler: '客服人员',
  user: '普通用户'
}

export const categoryOptions = [
  { id: 1, name: '账号问题' },
  { id: 2, name: '系统故障' },
  { id: 3, name: '功能建议' },
  { id: 4, name: '业务咨询' },
  { id: 5, name: '其他问题' },
  { id: 6, name: '权限问题' },
  { id: 7, name: '数据问题' },
  { id: 8, name: '页面显示' }
]

export function normalizeRole(role) {
  const value = String(role || '').replace(/^ROLE_/i, '').toLowerCase()
  if (value === 'administrator') return 'admin'
  if (value === 'service') return 'handler'
  return ['admin', 'handler', 'user'].includes(value) ? value : value
}

export function statusLabel(value) {
  return TICKET_STATUS[Number(value)]?.label || '未知'
}

export function statusType(value) {
  return TICKET_STATUS[Number(value)]?.type || 'info'
}

export function priorityLabel(value) {
  return PRIORITY[Number(value)]?.label || '未知'
}

export function priorityType(value) {
  return PRIORITY[Number(value)]?.type || 'info'
}
