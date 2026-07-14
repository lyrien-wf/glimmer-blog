<template>
  <div class="admin-page">
    <NavBar />

    <div class="container" style="padding-top: 80px;">
      <div class="admin-header">
        <h1>文章管理</h1>
        <div class="header-actions">
          <router-link to="/admin/categories" class="btn header-btn">分类管理</router-link>
          <router-link to="/admin/articles/edit" class="btn header-btn header-btn-primary">新建文章</router-link>
          <button class="btn header-btn header-btn-danger" @click="handleLogout">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
              <polyline points="16,17 21,12 16,7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            退出登录
          </button>
        </div>
      </div>

      <table class="admin-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>分类</th>
            <th>标签</th>
            <th>状态</th>
            <th>日期</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="article in articles" :key="article.id">
            <td class="title-cell">{{ article.title }}</td>
            <td>{{ article.category?.name || '-' }}</td>
            <td>
              <span class="tag" v-for="tag in article.tags" :key="tag.id">{{ tag.name }}</span>
            </td>
            <td>
              <span :class="['status-badge', article.isPublished ? 'published' : 'draft']">
                {{ article.isPublished ? '已发布' : '草稿' }}
              </span>
            </td>
            <td class="date-cell">{{ formatDate(article.createdAt) }}</td>
            <td class="action-cell">
              <router-link :to="`/admin/articles/edit/${article.id}`" class="btn btn-secondary btn-small">编辑</router-link>
              <button class="btn btn-danger btn-small" @click="handleDelete(article)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!articles.length" class="empty-state">
        <p>还没有文章，写点什么吧</p>
      </div>

      <Pagination :currentPage="page" :totalPages="totalPages" @update:currentPage="loadArticles" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminArticles, deleteArticle } from '../api/index.js'
import { useAuth } from '../stores/auth.js'
import { toast } from '../stores/toast.js'
import NavBar from '../components/NavBar.vue'
import Pagination from '../components/Pagination.vue'

const router = useRouter()
const { logout } = useAuth()
const articles = ref([])
const page = ref(1)
const totalPages = ref(1)

function handleLogout() {
  logout()
  router.push('/admin/login')
}

onMounted(() => loadArticles(1))

async function loadArticles(p) {
  try {
    const res = await getAdminArticles(p)
    articles.value = res.data.list
    totalPages.value = res.data.pages
    page.value = p
  } catch (err) {
    console.error('加载文章失败', err)
  }
}

async function handleDelete(article) {
  if (!confirm(`确定删除「${article.title}」？`)) return
  try {
    await deleteArticle(article.id)
    toast.success('删除成功')
    loadArticles(page.value)
  } catch (err) {
    toast.error('删除失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.admin-page {
  padding-top: 56px;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.admin-header h1 {
  font-size: 28px;
  font-weight: 700;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 统一按钮风格 */
.header-btn {
  padding: 8px 16px !important;
  font-size: 13px !important;
  font-weight: 500;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 默认：白底灰边 */
.header-btn:not(.header-btn-primary):not(.header-btn-danger) {
  background: var(--color-bg);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.header-btn:not(.header-btn-primary):not(.header-btn-danger):hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 主按钮：蓝色 */
.header-btn-primary {
  background: var(--color-accent);
  color: #fff;
  border: 1px solid var(--color-accent);
}

.header-btn-primary:hover {
  box-shadow: 0 2px 12px rgba(0, 113, 227, 0.4);
}

/* 危险按钮：橙色 */
.header-btn-danger {
  background: #ff9500;
  color: #fff;
  border: 1px solid #ff9500;
}

.header-btn-danger:hover {
  box-shadow: 0 2px 12px rgba(255, 149, 0, 0.4);
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.admin-table th {
  text-align: left;
  padding: 12px 16px;
  border-bottom: 2px solid var(--color-border);
  font-weight: 600;
  color: var(--color-text-sub);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.admin-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.title-cell {
  font-weight: 500;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-cell {
  color: var(--color-text-sub);
  white-space: nowrap;
}

.action-cell {
  display: flex;
  gap: 8px;
  white-space: nowrap;
}

.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.published {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge.draft {
  background: var(--color-bg-alt);
  color: var(--color-text-sub);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-sub);
}
</style>
