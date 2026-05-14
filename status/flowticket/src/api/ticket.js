import request from './request'

export function fetchTickets(params) {
  return request({ url: '/tickets', method: 'get', params })
}

export function fetchTicketDetail(id) {
  return request({ url: `/tickets/${id}`, method: 'get' })
}

export function createTicket(data) {
  return request({ url: '/tickets', method: 'post', data })
}

export function assignTicket(id, data) {
  return request({ url: `/tickets/${id}/assign`, method: 'post', data })
}

export function processTicket(id, data) {
  return request({ url: `/tickets/${id}/complete`, method: 'post', data })
}

export function closeTicket(id) {
  return request({ url: `/tickets/${id}/close`, method: 'post' })
}

export function replyTicket(id, data) {
  return request({ url: `/tickets/${id}/comments`, method: 'post', data })
}

export function rejectTicket(id, data) {
  return request({ url: `/tickets/${id}/reject`, method: 'post', data })
}

export function fetchTicketComments(id) {
  return request({ url: `/tickets/${id}/comments`, method: 'get' })
}

export function fetchTicketLogs(id) {
  return request({ url: `/tickets/${id}/logs`, method: 'get' })
}

export function updateTicketPriority(id, data) {
  return request({ url: `/tickets/${id}/priority`, method: 'put', data })
}
