<template>
  <div class="admin-page">
    <NavBar />

    <div class="container" style="padding-top: 80px;">
      <div class="admin-header">
        <h1>分类管理</h1>
      </div>

      <!-- 新建分类 -->
      <div class="create-bar">
        <input class="input" v-model="newName" placeholder="输入新分类名称" @keydown.enter="handleCreate" />
        <button class="btn btn-primary" @click="handleCreate" :disabled="!newName.trim()">添加</button>
      </div>

      <!-- 分类列表 -->
      <div class="category-list">
        <div v-for="cat in categories" :key="cat.id" class="category-item">
          <template v-if="editingId === cat.id">
            <input class="input edit-input" v-model="editingName" @keydown.enter="handleUpdate" @keydown.escape="cancelEdit" ref="editInput" />
            <div class="item-actions">
              <button class="btn btn-primary btn-small" @click="handleUpdate">保存</button>
              <button class="btn btn-secondary btn-small" @click="cancelEdit">取消</button>
            </div>
          </template>
          <template v-else>
            <span class="category-name">{{ cat.name }}</span>
            <div class="item-actions">
              <button class="btn btn-secondary btn-small" @click="startEdit(cat)">编辑</button>
              <button class="btn btn-danger btn-small" @click="handleDelete(cat)">删除</button>
            </div>
          </template>
        </div>

        <div v-if="!categories.length" class="empty-state">
          <p>暂无分类</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '../api/index.js'
import NavBar from '../components/NavBar.vue'

const categories = ref([])
const newName = ref('')
const editingId = ref(null)
const editingName = ref('')
const editInput = ref(null)

onMounted(() => loadCategories())

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (err) {
    console.error('加载分类失败', err)
  }
}

async function handleCreate() {
  const name = newName.value.trim()
  if (!name) return
  try {
    await createCategory(name)
    newName.value = ''
    loadCategories()
  } catch (err) {
    alert(err.response?.data?.message || '创建失败')
  }
}

function startEdit(cat) {
  editingId.value = cat.id
  editingName.value = cat.name
  nextTick(() => {
    editInput.value?.[0]?.focus()
  })
}

function cancelEdit() {
  editingId.value = null
  editingName.value = ''
}

async function handleUpdate() {
  const name = editingName.value.trim()
  if (!name) return
  try {
    await updateCategory(editingId.value, name)
    cancelEdit()
    loadCategories()
  } catch (err) {
    alert(err.response?.data?.message || '更新失败')
  }
}

async function handleDelete(cat) {
  if (!confirm(`确定删除分类「${cat.name}」？`)) return
  try {
    await deleteCategory(cat.id)
    loadCategories()
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  }
}
</script>

<style scoped>
.admin-page {
  padding-top: 56px;
}

.admin-header {
  margin-bottom: 24px;
}

.admin-header h1 {
  font-size: 28px;
  font-weight: 700;
}

.create-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.create-bar .input {
  flex: 1;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--color-border-light);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--color-bg);
}

.category-name {
  font-size: 15px;
  font-weight: 500;
}

.edit-input {
  flex: 1;
  max-width: 300px;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-sub);
  background: var(--color-bg);
}
</style>
