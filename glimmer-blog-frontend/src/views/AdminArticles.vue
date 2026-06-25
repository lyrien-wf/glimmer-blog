<template>
  <div class="admin-page">
    <NavBar />

    <div class="container" style="padding-top: 80px;">
      <div class="admin-header">
        <h1>文章管理</h1>
        <div class="header-actions">
          <router-link to="/admin/categories" class="btn btn-secondary">分类管理</router-link>
          <router-link to="/admin/articles/edit" class="btn btn-primary">新建文章</router-link>
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
import { getAdminArticles, deleteArticle } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Pagination from '../components/Pagination.vue'

const articles = ref([])
const page = ref(1)
const totalPages = ref(1)

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
    loadArticles(page.value)
  } catch (err) {
    alert('删除失败')
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
  gap: 12px;
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
