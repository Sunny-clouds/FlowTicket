import request from './request'

export function fetchUsers(params) {
  return request({ url: '/users/getAll', method: 'get', params })
}

export function createUser(data) {
  return request({ url: '/users/save', method: 'post', data })
}

export function updateUser(id, data) {
  return request({ url: `/users/${id}`, method: 'put', data })
}

export function fetchUserById(id) {
  return request({ url: `/users/${id}`, method: 'get' })
}

export function deleteUser(id) {
  return request({ url: `/users/${id}`, method: 'delete' })
}
