<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">Glimmer Blog</h1>
      <p class="login-subtitle">管理后台</p>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <input class="input" v-model="username" placeholder="用户名" autofocus />
        </div>
        <div class="form-group">
          <input class="input" v-model="password" type="password" placeholder="密码" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/index.js'
import { useAuth } from '../stores/auth.js'

const router = useRouter()
const { login: authLogin } = useAuth()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const res = await login(username.value, password.value)
    authLogin(res.data.token)
    router.push('/admin/articles')
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-alt);
}

.login-card {
  width: 360px;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-modal);
  padding: 48px 36px;
  text-align: center;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin-bottom: 4px;
}

.login-subtitle {
  font-size: 14px;
  color: var(--color-text-sub);
  margin-bottom: 32px;
}

.form-group {
  margin-bottom: 16px;
}

.error-msg {
  color: var(--color-danger);
  font-size: 13px;
  margin-bottom: 12px;
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 15px;
}
</style>
