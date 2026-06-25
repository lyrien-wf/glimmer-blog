<template>
  <div class="categories-page">
    <NavBar />

    <section class="container">
      <div class="page-header">
        <h1 class="page-title">分类</h1>
        <router-link to="/" class="back-home">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
            <polyline points="9,22 9,12 15,12 15,22"/>
          </svg>
          返回首页
        </router-link>
      </div>
      <!-- 骨架屏 -->
      <div class="category-grid" v-if="loading">
        <div class="skeleton-category card" v-for="n in 4" :key="n">
          <div class="skeleton-cat-icon"></div>
          <div class="skeleton-cat-name"></div>
        </div>
      </div>
      <!-- 分类列表 -->
      <div class="category-grid" v-else-if="categories.length">
        <router-link v-for="cat in categories" :key="cat.id"
                     :to="`/?categoryId=${cat.id}`" class="category-card card">
          <div class="category-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
            </svg>
          </div>
          <h3 class="category-name">{{ cat.name }}</h3>
        </router-link>
      </div>
      <div v-else class="empty-state">
        <p>暂无分类</p>
      </div>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategories } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'

const categories = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (err) {
    console.error('加载分类失败', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.categories-page {
  padding-top: 56px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 60px 0 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.back-home {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--color-bg-alt);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--color-text-sub);
  text-decoration: none;
  transition: all var(--transition);
}

.back-home:hover {
  background: var(--color-bg);
  color: var(--color-accent);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 80px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px 20px;
  text-decoration: none;
  color: inherit;
  text-align: center;
}

.category-icon {
  color: var(--color-accent);
}

.category-name {
  font-size: 16px;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-sub);
}

/* 骨架屏样式 */
.skeleton-category {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px 20px;
  pointer-events: none;
}

.skeleton-cat-icon {
  width: 32px;
  height: 32px;
  background: var(--color-bg-alt);
  border-radius: 50%;
  animation: skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-cat-name {
  width: 60px;
  height: 16px;
  background: var(--color-bg-alt);
  border-radius: 4px;
  animation: skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@media (max-width: 768px) {
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
