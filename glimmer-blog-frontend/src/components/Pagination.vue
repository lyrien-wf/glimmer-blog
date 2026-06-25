<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="currentPage <= 1" @click="go(currentPage - 1)">上一页</button>
    <template v-for="p in displayPages" :key="p">
      <span v-if="p === '...'" class="pagination-ellipsis">...</span>
      <button v-else :class="{ active: p === currentPage }" @click="go(p)">{{ p }}</button>
    </template>
    <button :disabled="currentPage >= totalPages" @click="go(currentPage + 1)">下一页</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 }
})

const emit = defineEmits(['update:currentPage'])

const displayPages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)

  const pages = []
  pages.push(1)
  if (current > 3) pages.push('...')
  for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
    pages.push(i)
  }
  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

function go(page) {
  emit('update:currentPage', page)
}
</script>

<style scoped>
.pagination-ellipsis {
  padding: 0 4px;
  color: var(--color-text-sub);
}
</style>
