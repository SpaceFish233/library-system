import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/user/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    redirect: '/books',
    children: [
      {
        path: 'books',
        name: 'BookList',
        component: () => import('../views/book/BookList.vue')
      },
      {
        path: 'books/:id',
        name: 'BookDetail',
        component: () => import('../views/book/BookDetail.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue')
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('../views/notification/Notifications.vue')
      },
      {
        path: 'my-borrows',
        name: 'MyBorrows',
        component: () => import('../views/borrow/MyBorrows.vue')
      },
      {
        path: 'my-reserves',
        name: 'MyReserves',
        component: () => import('../views/reserve/MyReserves.vue')
      },
      // 管理员路由
      {
        path: 'admin/books',
        name: 'AdminBooks',
        component: () => import('../views/admin/BookManage.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/categories',
        name: 'AdminCategories',
        component: () => import('../views/admin/CategoryManage.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/borrows',
        name: 'AdminBorrows',
        component: () => import('../views/admin/BorrowManage.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/reserves',
        name: 'AdminReserves',
        component: () => import('../views/admin/ReserveManage.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    if (token) {
      next('/')
    } else {
      next()
    }
    return
  }

  // 需要认证的页面
  if (!token) {
    next('/login')
    return
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && user?.role !== 'admin') {
    next('/books')
    return
  }

  next()
})

export default router
