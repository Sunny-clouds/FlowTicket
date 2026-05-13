import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 12000
})

function toError(message, detail) {
  const error = new Error(message || '请求处理失败')
  error.detail = detail
  return error
}

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')) {
      if ([1, 200].includes(body.code)) {
        return body.data
      }
      const message = body.msg || body.message || '请求处理失败'
      ElMessage.error(message)
      return Promise.reject(toError(message, body))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.msg || error.response?.data?.message || error.message || '网络请求异常'

    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.warning('登录已过期，请重新登录')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(toError(message, error.response?.data || error))
  }
)

export default service
