import { ref } from 'vue'

// 全局响应式登录状态
const isLoggedIn = ref(!!localStorage.getItem('blog_token'))

export function useAuth() {
  function login(token) {
    localStorage.setItem('blog_token', token)
    isLoggedIn.value = true
  }

  function logout() {
    localStorage.removeItem('blog_token')
    isLoggedIn.value = false
  }

  function checkAuth() {
    isLoggedIn.value = !!localStorage.getItem('blog_token')
  }

  return {
    isLoggedIn,
    login,
    logout,
    checkAuth
  }
}
