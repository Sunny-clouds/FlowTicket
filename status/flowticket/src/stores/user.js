import { defineStore } from 'pinia'
import { loginApi, getProfileApi } from '@/api/auth'
import { ROLE_LABELS, normalizeRole } from '@/utils/dicts'

function normalizeUser(user) {
  if (!user) return null
  return {
    ...user,
    realName: user.realName || user.username,
    role: normalizeRole(user.role)
  }
}

function parseLoginPayload(data) {
  return {
    token: data?.token,
    user: normalizeUser(data)
  }
}

function readStoredUser() {
  try {
    return normalizeUser(JSON.parse(localStorage.getItem('flowticket_user') || 'null'))
  } catch (error) {
    localStorage.removeItem('flowticket_user')
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('flowticket_token') || '',
    user: readStoredUser()
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
      localStorage.setItem('flowticket_token', this.token)
      localStorage.setItem('flowticket_user', JSON.stringify(this.user))
    },
    clearSession() {
      this.token = ''
      this.user = null
      localStorage.removeItem('flowticket_token')
      localStorage.removeItem('flowticket_user')
    },
    async login(form) {
      this.clearSession()
      const payload = parseLoginPayload(await loginApi(form))
      if (!payload.token) {
        throw new Error('登录接口未返回 token')
      }
      this.setSession(payload)
      return this.user
    },
    async loadProfile(force = false) {
      if (!this.token) return null
      if (!force && this.user) return this.user
      const user = await getProfileApi()
      this.user = normalizeUser(user)
      localStorage.setItem('flowticket_user', JSON.stringify(this.user))
      return this.user
    },
    updateProfile(profile) {
      this.user = normalizeUser({ ...this.user, ...profile })
      localStorage.setItem('flowticket_user', JSON.stringify(this.user))
    },
    logout() {
      this.clearSession()
    }
  }
})
