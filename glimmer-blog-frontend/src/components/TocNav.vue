<template>
  <nav class="toc-nav" v-if="headings.length > 0">
    <div class="toc-title">目录</div>
    <ul>
      <li v-for="(h, i) in headings" :key="i"
          :class="['toc-item', `toc-level-${h.level}`, { active: activeIndex === i }]">
        <a :href="`#${h.id}`" @click.prevent="scrollTo(h.id)">{{ h.text }}</a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'

const props = defineProps({
  content: { type: String, default: '' }
})

const headings = ref([])
const activeIndex = ref(0)
let observer = null

watch(() => props.content, () => {
  // 等待 DOM 渲染完成后再解析
  nextTick(() => {
    setTimeout(parseHeadings, 100)
  })
}, { immediate: true })

onMounted(() => {
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (observer) observer.disconnect()
})

function parseHeadings() {
  // 从实际 DOM 中解析标题，确保 ID 与文章内容一致
  const articleBody = document.querySelector('.article-body')
  if (!articleBody) return

  const elements = articleBody.querySelectorAll('h1, h2, h3')
  headings.value = Array.from(elements).map((el) => {
    return {
      level: parseInt(el.tagName[1]),
      text: el.textContent,
      id: el.id
    }
  })
}

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function onScroll() {
  const headingElements = headings.value
    .map(h => document.getElementById(h.id))
    .filter(Boolean)

  for (let i = headingElements.length - 1; i >= 0; i--) {
    if (headingElements[i].getBoundingClientRect().top <= 120) {
      activeIndex.value = i
      break
    }
  }
}
</script>

<style scoped>
.toc-nav {
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  padding-right: 20px;
}

.toc-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-sub);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}

.toc-nav ul {
  list-style: none;
  padding: 0;
}

.toc-item {
  margin: 6px 0;
}

.toc-item a {
  font-size: 13px;
  color: var(--color-text-sub);
  text-decoration: none;
  transition: color var(--transition);
  display: block;
  padding: 2px 0;
}

.toc-item.active a,
.toc-item a:hover {
  color: var(--color-accent);
}

.toc-level-2 { padding-left: 12px; }
.toc-level-3 { padding-left: 24px; }

@media (max-width: 1199px) {
  .toc-nav { display: none; }
}
</style>
