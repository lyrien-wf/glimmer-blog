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
        <p class="hero-subtitle">记录技术与生活</p>
      </div>
    </section>

    <!-- 分类筛选标签 -->
    <section class="container category-section">
      <div class="category-tabs">
        <button :class="['category-tab', { active: !currentCategoryId }]"
                @click="selectCategory(null)">
          全部
        </button>
        <button v-for="cat in categories" :key="cat.id"
                :class="['category-tab', { active: currentCategoryId === cat.id }]"
                @click="selectCategory(cat.id)">
          {{ cat.name }}
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
import { getArticles, searchArticles, getCategories } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'
import Pagination from '../components/Pagination.vue'

const route = useRoute()
const router = useRouter()
const articles = ref([])
const categories = ref([])
const page = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
const loading = ref(true)
const currentCategoryId = ref(null)
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
  document.title = 'Glimmer Blog'
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  // 加载分类列表
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (err) {
    console.error('加载分类失败', err)
  }

  // 如果 URL 带有 categoryId 参数，同步选中状态
  if (route.query.categoryId) {
    currentCategoryId.value = Number(route.query.categoryId)
  }

  // 加载文章
  loadArticles(1)
})

onUnmounted(() => {
  clearInterval(clockTimer)
})

function selectCategory(catId) {
  currentCategoryId.value = catId
  searchQuery.value = ''
  page.value = 1

  // 更新 URL
  if (catId) {
    router.push({ query: { categoryId: catId } })
  } else {
    router.push('/')
  }

  loadArticles(1)
}

async function loadArticles(p) {
  loading.value = true
  try {
    const params = { page: p, size: 9 }
    if (currentCategoryId.value) params.categoryId = currentCategoryId.value
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
    loadArticles(p)
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
  background: var(--gradient-brand);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  line-height: 1.1;
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

/* 分类筛选标签 */
.category-section {
  margin-bottom: 24px;
}

.category-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.category-tab {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: 100px;
  font-size: 14px;
  color: var(--color-text-sub);
  cursor: pointer;
  transition: all var(--transition);
}

.category-tab:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.category-tab.active {
  background: var(--gradient-brand);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 14px var(--color-accent-glow);
}

.search-section {
  margin-bottom: 40px;
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

/* 卡片依次淡入 */
.article-grid > * {
  animation: card-fade-in 0.5s cubic-bezier(0.25, 0.1, 0.25, 1) both;
}

.article-grid > *:nth-child(1) { animation-delay: 0.03s; }
.article-grid > *:nth-child(2) { animation-delay: 0.08s; }
.article-grid > *:nth-child(3) { animation-delay: 0.13s; }
.article-grid > *:nth-child(4) { animation-delay: 0.18s; }
.article-grid > *:nth-child(5) { animation-delay: 0.23s; }
.article-grid > *:nth-child(6) { animation-delay: 0.28s; }
.article-grid > *:nth-child(7) { animation-delay: 0.33s; }
.article-grid > *:nth-child(8) { animation-delay: 0.38s; }
.article-grid > *:nth-child(9) { animation-delay: 0.43s; }

@keyframes card-fade-in {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
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
  .category-tabs {
    gap: 6px;
  }
  .category-tab {
    padding: 6px 14px;
    font-size: 13px;
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
