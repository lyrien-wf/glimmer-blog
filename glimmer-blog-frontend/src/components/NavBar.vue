<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="navbar-brand">Glimmer Blog</router-link>
      <ul class="navbar-links">
        <li><router-link to="/">首页</router-link></li>
        <li><router-link to="/categories">分类</router-link></li>
        <li v-if="isAdmin"><router-link to="/admin/articles">管理</router-link></li>
        <li v-if="!isAdmin"><router-link to="/admin/login">管理</router-link></li>
      </ul>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'

const isAdmin = computed(() => {
  return !!localStorage.getItem('blog_token')
})
</script>
