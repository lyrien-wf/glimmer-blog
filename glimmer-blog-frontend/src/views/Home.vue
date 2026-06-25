<template>
  <div class="home-page">
    <NavBar />

    <!-- Hero -->
    <section class="hero">
      <div class="container">
        <div class="hero-clock">
          <span class="clock-time">{{ clockTime }}</span>
          <span class="clock-date">{{ clockDate }}</span>
        </div>
        <p class="hero-subtitle" v-if="!currentCategory && !currentTag">记录技术与生活</p>
      </div>
    </section>

    <!-- 筛选状态指示器 -->
    <section class="container" v-if="currentCategory || currentTag">
      <div class="filter-bar">
        <div class="filter-info">
          <svg class="filter-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z"/>
          </svg>
          <span class="filter-label">当前筛选：</span>
          <span class="filter-name">{{ currentCategory?.name || currentTag?.name }}</span>
        </div>
        <button class="filter-clear" @click="clearFilter">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
          返回全部
        </button>
      </div>
    </section>

    <!-- 搜索 -->
    <section class="container search-section">
      <div class="search-box">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
        <input class="search-input" v-model="searchQuery" placeholder="搜索文章..." @input="onSearch" />
      </div>
    </section>

    <!-- 文章列表 -->
    <main class="container">
      <!-- 骨架屏 -->
      <div class="article-grid" v-if="loading">
        <div class="skeleton-card card" v-for="n in 6" :key="n">
          <div class="skeleton-cover"></div>
          <div class="skeleton-body">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-text"></div>
            <div class="skeleton-line skeleton-text-short"></div>
            <div class="skeleton-meta">
              <div class="skeleton-line skeleton-date"></div>
              <div class="skeleton-line skeleton-views"></div>
            </div>
          </div>
        </div>
      </div>
      <!-- 文章列表 -->
      <div class="article-grid" v-else-if="articles.length">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <div v-else class="empty-state">
        <p>暂无文章</p>
      </div>
      <Pagination :currentPage="page" :totalPages="totalPages" @update:currentPage="onPageChange" />
    </main>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticles, searchArticles, getCategories, getTags } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'
import Pagination from '../components/Pagination.vue'

const route = useRoute()
const router = useRouter()
const articles = ref([])
const page = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const loading = ref(true)
const currentCategory = ref(null)
const currentTag = ref(null)
let searchTimer = null
let clockTimer = null

const clockTime = ref('')
const clockDate = ref('')

function updateClock() {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  clockTime.value = `${h}:${m}:${s}`

  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekday = weekdays[now.getDay()]
  clockDate.value = `${month}月${day}日 星期${weekday}`
}

onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  // 获取分类和标签信息
  await loadFilterInfo()

  // 加载文章
  if (route.query.categoryId) {
    loadArticles(1, route.query.categoryId, route.query.tagId)
  } else if (route.query.tagId) {
    loadArticles(1, null, route.query.tagId)
  } else {
    loadArticles(1)
  }
})

async function loadFilterInfo() {
  try {
    if (route.query.categoryId) {
      const res = await getCategories()
      currentCategory.value = res.data.find(c => c.id === Number(route.query.categoryId))
    }
    if (route.query.tagId) {
      const res = await getTags()
      currentTag.value = res.data.find(t => t.id === Number(route.query.tagId))
    }
  } catch (err) {
    console.error('加载筛选信息失败', err)
  }
}

function clearFilter() {
  currentCategory.value = null
  currentTag.value = null
  searchQuery.value = ''
  router.push('/')
  loadArticles(1)
}

onUnmounted(() => {
  clearInterval(clockTimer)
})

async function loadArticles(p, categoryId, tagId) {
  loading.value = true
  try {
    const params = { page: p, size: 9 }
    if (categoryId) params.categoryId = categoryId
    if (tagId) params.tagId = tagId
    const res = await getArticles(params)
    articles.value = res.data.list
    totalPages.value = res.data.pages
    page.value = p
  } catch (err) {
    console.error('加载文章失败', err)
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  if (searchQuery.value) {
    doSearch(searchQuery.value, p)
  } else {
    loadArticles(p, route.query.categoryId, route.query.tagId)
  }
}

function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    if (searchQuery.value) {
      doSearch(searchQuery.value, 1)
    } else {
      loadArticles(1)
    }
  }, 300)
}

async function doSearch(q, p) {
  try {
    const res = await searchArticles(q, p)
    articles.value = res.data.list
    totalPages.value = res.data.pages
    page.value = p
  } catch (err) {
    console.error('搜索失败', err)
  }
}
</script>

<style scoped>
.home-page {
  padding-top: 56px;
}

.hero {
  padding: 80px 0 40px;
  text-align: center;
}

.hero-clock {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.clock-time {
  font-size: 64px;
  font-weight: 200;
  letter-spacing: 4px;
  font-variant-numeric: tabular-nums;
  color: var(--color-text);
  line-height: 1;
}

.clock-date {
  font-size: 16px;
  color: var(--color-text-sub);
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--color-text-sub);
}

.search-section {
  margin-bottom: 40px;
}

/* 筛选状态指示器 */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: var(--color-bg-alt);
  border-radius: var(--radius-md);
  margin-bottom: 32px;
}

.filter-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-icon {
  color: var(--color-accent);
}

.filter-label {
  font-size: 14px;
  color: var(--color-text-sub);
}

.filter-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.filter-clear {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-text-sub);
  cursor: pointer;
  transition: all var(--transition);
}

.filter-clear:hover {
  background: var(--color-bg);
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.search-box {
  position: relative;
  max-width: 480px;
  margin: 0 auto;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-sub);
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 42px;
  border: 1px solid var(--color-border);
  border-radius: 100px;
  font-size: 15px;
  font-family: var(--font-sans);
  outline: none;
  transition: all var(--transition);
  background: var(--color-bg-alt);
}

.search-input:focus {
  border-color: var(--color-accent);
  background: var(--color-bg);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.1);
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-sub);
  font-size: 16px;
}

@media (max-width: 768px) {
  .hero { padding: 60px 0 24px; }
  .clock-time { font-size: 42px; }
  .article-grid { grid-template-columns: 1fr; }
  .filter-bar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .article-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 骨架屏样式 */
.skeleton-card {
  pointer-events: none;
}

.skeleton-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  background: var(--color-bg-alt);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  animation: skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-body {
  padding: 20px;
}

.skeleton-line {
  height: 14px;
  background: var(--color-bg-alt);
  border-radius: 4px;
  margin-bottom: 12px;
  animation: skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-title {
  width: 70%;
  height: 18px;
}

.skeleton-text {
  width: 100%;
}

.skeleton-text-short {
  width: 50%;
}

.skeleton-meta {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.skeleton-date {
  width: 80px;
}

.skeleton-views {
  width: 50px;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
