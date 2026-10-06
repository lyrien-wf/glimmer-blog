import axios from 'axios'

const api = axios.create({
  timeout: 30000
})

// 请求拦截器：自动添加 token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('blog_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：401 清除登录态并跳转登录页
api.interceptors.response.use(
  response => response.data,
  error => {
    const status = error.response?.status
    const url = error.config?.url || ''
    // 登录接口自身的 401 就是"用户名或密码错误"的正常反馈，必须原样交给页面展示；
    // 否则会被强制刷新跳转，用户永远看不到那句错误提示。
    const isLoginRequest = url.includes('/api/auth/login')
    if (status === 401 && !isLoginRequest) {
      localStorage.removeItem('blog_token')
      if (!window.location.pathname.startsWith('/admin/login')) {
        window.location.href = '/admin/login'
      }
    }
    return Promise.reject(error)
  }
)

// ===== 认证 =====
export const login = (username, password) =>
  api.post('/api/auth/login', { username, password })

// ===== 公开接口 =====
export const getArticles = (params) =>
  api.get('/api/articles', { params })

export const getArticle = (id) =>
  api.get(`/api/articles/${id}`)

export const searchArticles = (q, page = 1) =>
  api.get('/api/articles/search', { params: { q, page } })

export const getCategories = () =>
  api.get('/api/categories')

export const getTags = () =>
  api.get('/api/tags')

// ===== 管理接口 =====
export const getAdminArticles = (page = 1) =>
  api.get('/api/admin/articles', { params: { page } })

export const getAdminArticle = (id) =>
  api.get(`/api/admin/articles/${id}`)

export const createArticle = (data) =>
  api.post('/api/admin/articles', data)

export const updateArticle = (id, data) =>
  api.put(`/api/admin/articles/${id}`, data)

export const deleteArticle = (id) =>
  api.delete(`/api/admin/articles/${id}`)

export const uploadMd = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/api/admin/articles/upload-md', formData)
}

export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/api/admin/upload/image', formData)
}

export const createCategory = (name) =>
  api.post('/api/admin/categories', { name })

export const deleteCategory = (id) =>
  api.delete(`/api/admin/categories/${id}`)

export const updateCategory = (id, name) =>
  api.put(`/api/admin/categories/${id}`, { name })

export const sortCategories = (sortedIds) =>
  api.put('/api/admin/categories/sort', { sortedIds })

export const createTag = (name) =>
  api.post('/api/admin/tags', { name })

export const deleteTag = (id) =>
  api.delete(`/api/admin/tags/${id}`)

export const changePassword = (data) =>
  api.put('/api/admin/user/password', data)
