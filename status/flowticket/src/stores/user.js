import { defineStore } from 'pinia'
import { loginApi, getProfileApi } from '@/api/auth'

export const ROLE_LABELS = {
  ADMIN: '管理员',
  SERVICE: '客服人员',
  USER: '普通用户'
}

const ROLE_MAP = {
  admin: 'ADMIN',
  handler: 'SERVICE',
  user: 'USER',
  role_admin: 'ADMIN',
  role_handler: 'SERVICE',
  role_user: 'USER'
}

function normalizeRole(role) {
  const key = String(role || '').toLowerCase()
  return ROLE_MAP[key] || String(role || '').replace(/^ROLE_/i, '').toUpperCase()
}

function normalizeUser(user) {
  if (!user) return user
  return {
    ...user,
    rawRole: user.role,
    role: normalizeRole(user.role)
  }
}

function userFromLogin(data) {
  const { token, ...user } = data || {}
  return { token, user: normalizeUser(user) }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('flowticket_token') || '',
    user: normalizeUser(JSON.parse(localStorage.getItem('flowticket_user') || 'null'))
  }),
  getters: {
    role: (state) => state.user?.role || '',
    roleLabel: (state) => ROLE_LABELS[state.user?.role] || '未登录',
    isLogin: (state) => Boolean(state.token)
  },
  actions: {
    setSession(payload) {
      this.token = payload.token
      this.user = normalizeUser(payload.user)
      localStorage.setItem('flowticket_token', payload.token)
      localStorage.setItem('flowticket_user', JSON.stringify(this.user))
    },
    async login(form) {
      const payload = userFromLogin(await loginApi(form))
      if (!payload.token) {
        throw new Error('登录接口未返回 token')
      }
      this.setSession(payload)
      return payload.user
    },
    async loadProfile(force = false) {
      if (!this.token) return null
      if (!force && this.user) return this.user
      this.user = normalizeUser(await getProfileApi())
      localStorage.setItem('flowticket_user', JSON.stringify(this.user))
      return this.user
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('flowticket_token')
      localStorage.removeItem('flowticket_user')
    }
  }
})
