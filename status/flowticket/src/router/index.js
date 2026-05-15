import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

export const asyncMenus = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '后台首页', icon: 'DataBoard', roles: ['admin'] }
  },
  {
    path: '/tickets',
    name: 'TicketList',
    component: () => import('@/views/tickets/TicketList.vue'),
    meta: { title: '工单列表', icon: 'Tickets', roles: ['admin', 'handler', 'user'] }
  },
  {
    path: '/tickets/create',
    name: 'TicketCreate',
    component: () => import('@/views/tickets/TicketCreate.vue'),
    meta: { title: '提交工单', icon: 'EditPen', roles: ['user'] }
  },
  {
    path: '/tickets/:id',
    name: 'TicketDetail',
    component: () => import('@/views/tickets/TicketDetail.vue'),
    meta: { title: '工单详情', icon: 'Document', roles: ['admin', 'handler', 'user'], hidden: true }
  },
  {
    path: '/service/queue',
    name: 'ServiceQueue',
    component: () => import('@/views/service/ServiceQueue.vue'),
    meta: { title: '客服处理台', icon: 'Headset', roles: ['handler'] }
  },
  {
    path: '/admin/assign',
    name: 'TicketAssign',
    component: () => import('@/views/admin/TicketAssign.vue'),
    meta: { title: '工单分配', icon: 'Connection', roles: ['admin'] }
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] }
  },
  {
    path: '/admin/categories',
    name: 'CategoryManage',
    component: () => import('@/views/admin/CategoryManage.vue'),
    meta: { title: '分类管理', icon: 'CollectionTag', roles: ['admin'] }
  },
  {
    path: '/admin/statistics',
    name: 'Statistics',
    component: () => import('@/views/admin/StatisticsView.vue'),
    meta: { title: '数据统计', icon: 'TrendCharts', roles: ['admin'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: '个人中心', icon: 'User', roles: ['admin', 'handler', 'user'] }
  }
]

export function defaultPathByRole(role) {
  if (role === 'admin') return '/dashboard'
  if (role === 'handler') return '/service/queue'
  return '/tickets'
}

export function canRoleVisitPath(role, path) {
  const purePath = String(path || '').split('?')[0]
  const target = asyncMenus.find((item) => item.path === purePath || (item.path.includes('/:') && purePath.startsWith(item.path.split('/:')[0])))
  return !target?.meta?.roles || target.meta.roles.includes(role)
}

function readStoredRole() {
  try {
    const user = JSON.parse(localStorage.getItem('flowticket_user') || 'null')
    return user?.role
  } catch (error) {
    return ''
  }
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/AppLayout.vue'),
    redirect: () => defaultPathByRole(readStoredRole()),
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
    redirect: () => defaultPathByRole(readStoredRole())
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  document.title = `${to.meta.title || 'FlowTicket'} - FlowTicket`

  if (to.meta.public) {
    if (to.path === '/login' && userStore.isLogin) {
      try {
        await userStore.loadProfile()
      } catch (error) {
        userStore.logout()
        return true
      }
      return defaultPathByRole(userStore.role)
    }
    return true
  }

  if (!userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  try {
    await userStore.loadProfile()
  } catch (error) {
    userStore.logout()
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/') {
    return defaultPathByRole(userStore.role)
  }
  const roles = to.meta.roles
  if (roles && !roles.includes(userStore.role)) {
    return '/403'
  }
  return true
})

export default router
