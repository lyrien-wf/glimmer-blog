<template>
  <footer class="footer">
    <p>© {{ year }} Blog. Built with Spring Boot & Vue.</p>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>
