<template>
  <footer class="footer">
    <p>© {{ year }} Glimmer Blog. Built with Spring Boot & Vue.</p>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>
