import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

export const asyncMenus = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '工作台', icon: 'DataBoard', roles: ['ADMIN', 'SERVICE', 'USER'] }
  },
  {
    path: '/tickets',
    name: 'TicketList',
    component: () => import('@/views/tickets/TicketList.vue'),
    meta: { title: '工单列表', icon: 'Tickets', roles: ['ADMIN', 'SERVICE', 'USER'] }
  },
  {
    path: '/tickets/create',
    name: 'TicketCreate',
    component: () => import('@/views/tickets/TicketCreate.vue'),
    meta: { title: '提交工单', icon: 'EditPen', roles: ['USER'] }
  },
  {
    path: '/service/queue',
    name: 'ServiceQueue',
    component: () => import('@/views/service/ServiceQueue.vue'),
    meta: { title: '客服处理台', icon: 'Headset', roles: ['ADMIN', 'SERVICE'] }
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { title: '用户管理', icon: 'UserFilled', roles: ['ADMIN'] }
  },
  {
    path: '/admin/settings',
    name: 'SystemSettings',
    component: () => import('@/views/admin/SystemSettings.vue'),
    meta: { title: '系统配置', icon: 'Setting', roles: ['ADMIN'] }
  }
]

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: asyncMenus
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/ForbiddenView.vue'),
    meta: { public: true, title: '无权限' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.meta.public) return true

  if (!userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  await userStore.loadProfile()
  const roles = to.meta.roles
  if (roles && !roles.includes(userStore.role)) {
    return '/403'
  }
  return true
})

export default router
