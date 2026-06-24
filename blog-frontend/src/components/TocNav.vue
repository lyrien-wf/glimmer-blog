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
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  content: { type: String, default: '' }
})

const headings = ref([])
const activeIndex = ref(0)

watch(() => props.content, () => {
  parseHeadings()
}, { immediate: true })

onMounted(() => {
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})

function parseHeadings() {
  const div = document.createElement('div')
  div.innerHTML = props.content
  const elements = div.querySelectorAll('h1, h2, h3')
  headings.value = Array.from(elements).map((el, i) => {
    const id = `heading-${i}`
    return { level: parseInt(el.tagName[1]), text: el.textContent, id }
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
  position: fixed;
  right: -280px;
  top: 100px;
  width: 220px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
}

@media (min-width: 1200px) {
  .toc-nav {
    right: max(-280px, calc((100vw - 1100px) / 2 - 280px));
  }
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
