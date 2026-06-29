import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/article/:id',
    name: 'Article',
    component: () => import('../views/Article.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLogin.vue')
  },
  {
    path: '/admin/articles',
    name: 'AdminArticles',
    component: () => import('../views/AdminArticles.vue')
  },
  {
    path: '/admin/articles/edit',
    name: 'AdminEditNew',
    component: () => import('../views/AdminEdit.vue')
  },
  {
    path: '/admin/articles/edit/:id',
    name: 'AdminEdit',
    component: () => import('../views/AdminEdit.vue')
  },
  {
    path: '/admin/categories',
    name: 'AdminCategories',
    component: () => import('../views/AdminCategories.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫：/admin/** 需要登录
router.beforeEach((to, from, next) => {
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    const token = localStorage.getItem('blog_token')
    if (!token) {
      next('/admin/login')
      return
    }
  }
  next()
})

export default router
