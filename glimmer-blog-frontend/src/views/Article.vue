<template>
  <div class="article-page">
    <NavBar />

    <div v-if="article">
      <!-- 封面 -->
      <div class="article-cover" :style="coverStyle">
        <img v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" />
      </div>

      <article class="container article-container">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span v-if="article.category">{{ article.category.name }}</span>
          <span v-if="article.category && article.tags?.length">·</span>
          <span v-for="(tag, i) in article.tags" :key="tag.id">
            <router-link :to="`/?tagId=${tag.id}`" class="tag">{{ tag.name }}</router-link>
            <span v-if="i < article.tags.length - 1"> </span>
          </span>
          <span v-if="article.tags?.length">·</span>
          <span>{{ formatDate(article.createdAt) }}</span>
          <span>·</span>
          <span>{{ article.views }} 阅读</span>
        </div>

        <div class="article-body md-content" v-html="article.htmlContent"></div>
      </article>

      <TocNav :content="article.htmlContent" />
    </div>

    <div v-else class="loading-state container">
      <p>加载中...</p>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
import TocNav from '../components/TocNav.vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const route = useRoute()
const article = ref(null)

const coverStyle = computed(() => {
  if (article.value?.coverUrl) return {}
  return { background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
})

onMounted(async () => {
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data
    document.title = article.value.title + ' - Glimmer Blog'

    // 代码高亮
    setTimeout(() => {
      document.querySelectorAll('pre code').forEach(block => {
        hljs.highlightElement(block)
      })
      // 给代码块添加语言标签
      document.querySelectorAll('pre code').forEach(block => {
        const lang = block.className.replace('hljs ', '').replace('language-', '')
        if (lang) {
          const pre = block.parentElement
          if (!pre.querySelector('.code-lang')) {
            const label = document.createElement('span')
            label.className = 'code-lang'
            label.textContent = lang
            pre.style.position = 'relative'
            pre.appendChild(label)
          }
        }
      })
    }, 100)
  } catch (err) {
    console.error('加载文章失败', err)
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.article-page {
  padding-top: 56px;
}

.article-cover {
  width: 100%;
  max-height: 420px;
  overflow: hidden;
  background: var(--color-bg-alt);
}

.article-cover img {
  width: 100%;
  height: 420px;
  object-fit: cover;
}

.article-container {
  max-width: 760px;
  padding-top: 40px;
  padding-bottom: 80px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.2;
  margin-bottom: 20px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-text-sub);
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.article-body {
  padding-top: 8px;
}

.loading-state {
  padding: 120px 0;
  text-align: center;
  color: var(--color-text-sub);
}

/* 代码语言标签 */
:deep(pre) {
  position: relative;
}

:deep(.code-lang) {
  position: absolute;
  top: 8px;
  right: 12px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  font-family: var(--font-mono);
  text-transform: uppercase;
}
</style>
