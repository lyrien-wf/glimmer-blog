<template>
  <router-link :to="`/article/${article.id}`" class="article-card card">
    <div class="card-cover" :style="coverStyle">
      <img v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" />
      <div v-else class="card-cover-placeholder">
        <span>{{ article.title?.charAt(0) || 'B' }}</span>
      </div>
    </div>
    <div class="card-body">
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-meta">
        <span class="card-date">{{ formatDate(article.createdAt) }}</span>
        <span class="card-views">{{ article.views }} 阅读</span>
      </div>
      <div class="card-tags" v-if="article.tags?.length">
        <span class="tag" v-for="tag in article.tags" :key="tag.id">{{ tag.name }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  article: { type: Object, required: true }
})

const coverStyle = computed(() => {
  if (props.article.coverUrl) return {}
  const colors = ['#0071e3', '#34c759', '#ff9500', '#af52de', '#ff2d55']
  const idx = (props.article.id || 0) % colors.length
  return { background: colors[idx] }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.article-card {
  display: block;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.card-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--color-bg-alt);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.article-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.6);
}

.card-body {
  padding: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.3;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.01em;
}

.card-summary {
  font-size: 14px;
  color: var(--color-text-sub);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.card-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-sub);
  margin-bottom: 10px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
