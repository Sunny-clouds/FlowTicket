import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 12000
})

const PUBLIC_URLS = ['/users/login', '/users/register']

function isSuccessCode(code) {
  return Number(code) === 1
}

function getErrorMessage(error) {
  return error.response?.data?.msg || error.response?.data?.message || error.message || '网络请求异常'
}

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const isPublic = PUBLIC_URLS.some((url) => config.url?.includes(url))

    if (userStore.token && !isPublic) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')) {
      if (isSuccessCode(body.code)) return body.data
      return Promise.reject(new Error(body.msg || body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = getErrorMessage(error)

    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.warning('登录已过期，请重新登录')
      if (window.location.pathname !== '/login') {
        window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
      }
    }

    return Promise.reject(new Error(message))
  }
)

export default request
