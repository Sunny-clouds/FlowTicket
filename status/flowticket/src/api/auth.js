import request from './request'

export function loginApi(data) {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

export function registerApi(data) {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

export function getProfileApi() {
  return request({
    url: '/users/me',
    method: 'get'
  })
}
