<template>
  <div class="admin-page">
    <NavBar />

    <div class="editor-layout" style="padding-top: 56px;">
      <!-- 左侧编辑区 -->
      <div class="editor-main">
        <input class="title-input" v-model="form.title" placeholder="文章标题" />

        <div class="editor-toolbar">
          <label class="btn btn-secondary btn-small upload-md-btn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="12" y1="18" x2="12" y2="12"/>
              <line x1="9" y1="15" x2="15" y2="15"/>
            </svg>
            上传 .md 文件
            <input type="file" accept=".md" style="display:none" @change="handleUploadMd" />
          </label>
        </div>

        <div id="vditor" class="vditor-container"></div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="editor-sidebar">
        <div class="sidebar-section">
          <label class="sidebar-label">发布状态</label>
          <div class="toggle-row">
            <div :class="['toggle', { active: form.isPublished }]" @click="form.isPublished = !form.isPublished"></div>
            <span>{{ form.isPublished ? '已发布' : '草稿' }}</span>
          </div>
        </div>

        <div class="sidebar-section">
          <label class="sidebar-label">分类</label>
          <select class="input select" v-model="form.categoryId">
            <option :value="null">无分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>

        <div class="sidebar-section">
          <label class="sidebar-label">标签</label>
          <div class="tags-container">
            <span class="tag tag-removable" v-for="tag in selectedTags" :key="tag.id"
                  @click="removeTag(tag)">
              {{ tag.name }} ×
            </span>
          </div>
          <div class="tag-input-row">
            <input class="input" v-model="newTagName" placeholder="输入标签名回车添加"
                   @keydown.enter.prevent="addTag" style="font-size:13px; padding:8px 12px;" />
          </div>
        </div>

        <div class="sidebar-section">
          <label class="sidebar-label">封面图</label>
          <ImageUploader v-model="form.coverUrl" />
        </div>

        <div class="sidebar-section">
          <label class="sidebar-label">摘要</label>
          <textarea class="input textarea" v-model="form.summary" placeholder="可选，不填自动截取正文前100字"></textarea>
        </div>

        <button class="btn btn-primary save-btn" @click="handleSave" :disabled="saving">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { getAdminArticle, createArticle, updateArticle, getCategories, getTags, createTag, uploadMd } from '../api/index.js'
import NavBar from '../components/NavBar.vue'
import ImageUploader from '../components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({
  title: '',
  content: '',
  summary: '',
  coverUrl: '',
  categoryId: null,
  tagIds: [],
  isPublished: true
})

const categories = ref([])
const allTags = ref([])
const selectedTags = ref([])
const newTagName = ref('')
const saving = ref(false)
let vditor = null
let pendingContent = null // 待设置的内容

onMounted(async () => {
  // 加载分类和标签
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  allTags.value = tagRes.data

  // 如果是编辑模式，先加载文章数据
  if (isEdit) {
    try {
      const res = await getAdminArticle(route.params.id)
      const data = res.data
      form.value.title = data.title
      form.value.content = data.content || ''
      form.value.summary = data.summary || ''
      form.value.coverUrl = data.coverUrl || ''
      form.value.categoryId = data.category?.id || null
      form.value.isPublished = data.isPublished ?? false

      // 保存待设置的内容
      pendingContent = data.content || ''

      // 设置已选标签
      if (data.tags?.length) {
        selectedTags.value = [...data.tags]
        form.value.tagIds = data.tags.map(t => t.id)
      }
    } catch (err) {
      console.error('加载文章失败', err)
    }
  }

  // 初始化编辑器
  await nextTick()
  vditor = new Vditor('vditor', {
    theme: 'classic',
    height: 'calc(100vh - 220px)',
    placeholder: '开始写作...',
    outline: { enable: false },
    toolbar: [
      'emoji', 'headings', 'bold', 'italic', 'strike', '|',
      'line', 'quote', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', 'table', '|',
      'link', 'image', '|',
      'undo', 'redo', '|',
      'fullscreen', 'preview'
    ],
    input: (val) => {
      form.value.content = val
    },
    after: () => {
      // Vditor 初始化完成后，设置待设置的内容
      if (pendingContent !== null) {
        vditor.setValue(pendingContent)
        pendingContent = null
      }
    }
  })
})

onUnmounted(() => {
  vditor?.destroy()
})

async function handleSave() {
  if (!form.value.title.trim()) {
    alert('请输入标题')
    return
  }

  saving.value = true
  try {
    form.value.tagIds = selectedTags.value.map(t => t.id)

    if (isEdit) {
      await updateArticle(route.params.id, form.value)
      alert('更新成功')
    } else {
      const res = await createArticle(form.value)
      alert('创建成功')
      router.push(`/admin/articles/edit/${res.data.id}`)
    }
  } catch (err) {
    alert('保存失败：' + (err.response?.data?.message || err.message))
  } finally {
    saving.value = false
  }
}

async function handleUploadMd(e) {
  const file = e.target.files?.[0]
  if (!file) return

  try {
    const res = await uploadMd(file)
    form.value.title = res.data.title
    vditor.setValue(res.data.content)
    form.value.content = res.data.content
  } catch (err) {
    alert('上传失败')
  }
  e.target.value = ''
}

async function addTag() {
  const name = newTagName.value.trim()
  if (!name) return

  // 检查是否已选
  if (selectedTags.value.find(t => t.name === name)) {
    newTagName.value = ''
    return
  }

  // 检查是否已存在
  let tag = allTags.value.find(t => t.name === name)
  if (!tag) {
    try {
      const res = await createTag(name)
      tag = { id: res.data?.id || Date.now(), name }
    } catch {
      tag = { id: Date.now(), name }
    }
  }

  selectedTags.value.push(tag)
  form.value.tagIds.push(tag.id)
  newTagName.value = ''
}

function removeTag(tag) {
  selectedTags.value = selectedTags.value.filter(t => t.id !== tag.id)
  form.value.tagIds = form.value.tagIds.filter(id => id !== tag.id)
}
</script>

<style scoped>
.editor-layout {
  display: flex;
  gap: 0;
  height: calc(100vh - 56px);
  overflow: hidden;
}

.editor-main {
  flex: 1;
  min-width: 0;
  padding: 24px 32px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.title-input {
  width: 100%;
  border: none;
  border-bottom: 1px solid var(--color-border-light);
  padding: 16px 0;
  font-size: 28px;
  font-weight: 600;
  font-family: var(--font-sans);
  outline: none;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.title-input:focus {
  border-color: var(--color-accent);
}

.editor-toolbar {
  margin-bottom: 12px;
}

.upload-md-btn {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.vditor-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.vditor-container :deep(.vditor) {
  height: 100% !important;
  min-width: 0;
}

.vditor-container :deep(.vditor-content) {
  flex: 1;
  min-width: 0;
}

.editor-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-left: 1px solid var(--color-border-light);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-sub);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.toggle-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-removable {
  cursor: pointer;
}

.tag-removable:hover {
  background: var(--color-danger);
  color: #fff;
}

.tag-input-row {
  margin-top: 4px;
}

.save-btn {
  width: 100%;
  padding: 12px;
  margin-top: auto;
}

@media (max-width: 768px) {
  .editor-layout {
    flex-direction: column;
  }
  .editor-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--color-border-light);
  }
}
</style>
