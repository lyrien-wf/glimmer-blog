<template>
  <div class="article-page">
    <NavBar />

    <div v-if="article">
      <!-- 封面 -->
      <div class="article-cover" :style="coverStyle">
        <img v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" />
      </div>

      <!-- 三栏布局：目录 | 文章 | (留白) -->
      <div class="article-layout">
        <!-- 左侧目录 -->
        <aside class="article-sidebar">
          <TocNav :content="article.htmlContent" />
        </aside>

        <!-- 文章主体 -->
        <article class="article-main">
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
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-else-if="!error" class="loading-state container">
      <div class="skeleton-article">
        <div class="skeleton-cover-lg"></div>
        <div class="skeleton-body-lg">
          <div class="skeleton-line-lg skeleton-title-lg"></div>
          <div class="skeleton-meta-lg">
            <div class="skeleton-line-lg skeleton-tag"></div>
            <div class="skeleton-line-lg skeleton-date-lg"></div>
            <div class="skeleton-line-lg skeleton-views-lg"></div>
          </div>
          <div class="skeleton-content">
            <div class="skeleton-line-lg skeleton-para" v-for="n in 8" :key="n"
                 :style="{ width: n % 3 === 0 ? '60%' : n % 2 === 0 ? '90%' : '100%' }"></div>
          </div>
        </div>
      </div>
      <p>加载中...</p>
    </div>
    <div v-else class="error-state container">
      <h2>文章加载失败</h2>
      <p>{{ error }}</p>
      <router-link to="/" class="btn btn-primary">返回首页</router-link>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
import TocNav from '../components/TocNav.vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const route = useRoute()
const article = ref(null)
const error = ref('')

const coverStyle = computed(() => {
  if (article.value?.coverUrl) return {}
  return { background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
})

onMounted(async () => {
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data
    document.title = article.value.title + ' - Glimmer Blog'

    await nextTick()

    // 为文章标题添加 ID，与目录关联
    const articleBody = document.querySelector('.article-body')
    if (articleBody) {
      const headings = articleBody.querySelectorAll('h1, h2, h3')
      headings.forEach((el, i) => {
        el.id = `heading-${i}`
      })
    }

    // 代码高亮
    document.querySelectorAll('pre code').forEach(block => {
      hljs.highlightElement(block)
      const lang = block.className.replace('hljs ', '').replace('language-', '')
      if (lang) {
        const pre = block.parentElement
        if (pre && !pre.querySelector('.code-lang')) {
          const label = document.createElement('span')
          label.className = 'code-lang'
          label.textContent = lang
          pre.style.position = 'relative'
          pre.appendChild(label)
        }
      }
    })
  } catch (err) {
    error.value = '文章不存在或已被删除'
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

/* 三栏布局 */
.article-layout {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 80px;
  gap: 48px;
}

.article-sidebar {
  width: 220px;
  flex-shrink: 0;
}

.article-main {
  flex: 1;
  max-width: 760px;
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

.error-state {
  padding: 120px 0;
  text-align: center;
}

.error-state h2 {
  font-size: 22px;
  margin-bottom: 8px;
}

.error-state p {
  color: var(--color-text-sub);
  margin-bottom: 24px;
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

/* 骨架屏样式 */
.skeleton-article {
  padding-top: 40px;
  padding-bottom: 80px;
}

.skeleton-cover-lg {
  width: 100%;
  height: 420px;
  background: var(--color-bg-alt);
  border-radius: var(--radius-md);
  margin-bottom: 40px;
  animation: skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-body-lg {
  max-width: 760px;
  margin: 0 auto;
}

.skeleton-line-lg {
  height: 14px;
  background: var(--color-bg-alt);
  border-radius: 4px;
  margin-bottom: 16px;
  animation: skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-title-lg {
  width: 70%;
  height: 36px;
  margin-bottom: 24px;
}

.skeleton-meta-lg {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
}

.skeleton-tag {
  width: 60px;
}

.skeleton-date-lg {
  width: 100px;
}

.skeleton-views-lg {
  width: 70px;
}

.skeleton-content {
  padding-top: 8px;
}

.skeleton-para {
  width: 100%;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 响应式 */
@media (max-width: 1199px) {
  .article-sidebar {
    display: none;
  }
  .article-layout {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .article-cover img {
    height: 240px;
  }
  .article-layout {
    padding: 24px 16px 60px;
  }
  .article-title {
    font-size: 28px;
  }
}
</style>
