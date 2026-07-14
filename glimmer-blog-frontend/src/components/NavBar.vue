<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="navbar-brand gradient-text">Glimmer Blog</router-link>
      <ul class="navbar-links">
        <li><router-link to="/">首页</router-link></li>
        <li v-if="isLoggedIn"><router-link to="/admin/articles">管理</router-link></li>
        <li v-else><router-link to="/admin/login">管理</router-link></li>
      </ul>
    </div>
  </nav>
</template>

<script setup>
import { useAuth } from '../stores/auth.js'

const { isLoggedIn } = useAuth()
</script>
