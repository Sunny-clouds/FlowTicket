import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { showAppToast } from '@/utils/toast'

const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 12000
})

const PUBLIC_URLS = ['/users/login', '/users/register']
const AUTH_EXPIRED_NOTICE_KEY = 'flowticket_auth_expired_notice'
let authExpiredNotified = false

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
      if (!authExpiredNotified) {
        authExpiredNotified = true
        const notice = '登录已过期，请重新登录'
        sessionStorage.setItem(AUTH_EXPIRED_NOTICE_KEY, notice)
        showAppToast({ type: 'warning', message: notice, duration: 8000 })
      }
      if (window.location.pathname !== '/login') {
        window.setTimeout(() => {
          window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
        }, 200)
      }
      return new Promise(() => {})
    }

    return Promise.reject(new Error(message))
  }
)

export default request
