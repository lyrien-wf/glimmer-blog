<template>
  <div class="uploader" @click="triggerUpload" :class="{ 'has-image': modelValue }">
    <img v-if="modelValue" :src="modelValue" class="preview" />
    <div v-else class="uploader-placeholder">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <rect x="3" y="3" width="18" height="18" rx="3"/>
        <circle cx="8.5" cy="8.5" r="1.5"/>
        <path d="M21 15l-5-5L5 21"/>
      </svg>
      <span>点击上传封面图</span>
    </div>
    <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleUpload" />
    <div v-if="loading" class="uploader-loading">上传中...</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { uploadImage } from '../api/index.js'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const loading = ref(false)

function triggerUpload() {
  fileInput.value?.click()
}

async function handleUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return

  loading.value = true
  try {
    const res = await uploadImage(file)
    emit('update:modelValue', res.data.url)
  } catch (err) {
    alert('图片上传失败')
  } finally {
    loading.value = false
    e.target.value = ''
  }
}
</script>

<style scoped>
.uploader {
  width: 100%;
  aspect-ratio: 16 / 9;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition);
  overflow: hidden;
  position: relative;
}

.uploader:hover {
  border-color: var(--color-accent);
}

.uploader.has-image {
  border-style: solid;
  border-color: transparent;
}

.uploader-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--color-text-sub);
  font-size: 13px;
}

.preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.uploader-loading {
  position: absolute;
  inset: 0;
  background: rgba(255,255,255,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--color-text-sub);
}
</style>
