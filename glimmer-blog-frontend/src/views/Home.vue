<template>
  <div class="home-page">
    <NavBar />

    <!-- Hero -->
    <section class="hero">
      <div class="container">
        <h1 class="hero-title">Glimmer Blog</h1>
        <p class="hero-subtitle">记录技术与生活</p>
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
      <div class="article-grid" v-if="articles.length">
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles, searchArticles } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
import ArticleCard from '../components/ArticleCard.vue'
import Pagination from '../components/Pagination.vue'

const route = useRoute()
const articles = ref([])
const page = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')
let searchTimer = null

onMounted(() => {
  if (route.query.categoryId) {
    loadArticles(1, route.query.categoryId, route.query.tagId)
  } else if (route.query.tagId) {
    loadArticles(1, null, route.query.tagId)
  } else {
    loadArticles(1)
  }
})

async function loadArticles(p, categoryId, tagId) {
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

.hero-title {
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin-bottom: 8px;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--color-text-sub);
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

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-sub);
  font-size: 16px;
}

@media (max-width: 768px) {
  .hero { padding: 60px 0 24px; }
  .hero-title { font-size: 36px; }
  .article-grid { grid-template-columns: 1fr; }
}
</style>
