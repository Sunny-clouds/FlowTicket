<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">工单分类管理</h2>
        <p class="page-subtitle">维护工单分类、编码、排序和启停状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
    </div>

    <div class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索分类名称或编码" clearable style="width: 260px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="search">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="pageCategories" stripe style="margin-top: 16px">
        <el-table-column prop="categoryName" label="分类名称" min-width="160" />
        <el-table-column prop="categoryCode" label="分类编码" width="150" />
        <el-table-column prop="description" label="说明" min-width="260" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="filteredCategories.length" layout="total, prev, pager, next" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingCategory ? '编辑分类' : '新增分类'" width="540px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类名称"><el-input v-model="form.categoryName" /></el-form-item>
        <el-form-item label="分类编码"><el-input v-model="form.categoryCode" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createCategory, deleteCategory, fetchCategories, updateCategory } from '@/api/category'
import { formatDateTime } from '@/utils/format'
import { showAppToast, showErrorToast } from '@/utils/toast'

const loading = ref(false)
const query = reactive({ keyword: '', status: '' })
const page = reactive({ current: 1, size: 10 })
const dialogVisible = ref(false)
const editingCategory = ref(null)
const categories = ref([])
const form = reactive({ id: null, categoryName: '', categoryCode: '', description: '', sort: 0, status: 1 })

const filteredCategories = computed(() => categories.value.filter((item) => {
  const keywordHit = !query.keyword || [item.categoryName, item.categoryCode].some((value) => String(value || '').includes(query.keyword))
  return keywordHit
}))

const pageCategories = computed(() => {
  const start = (page.current - 1) * page.size
  return filteredCategories.value.slice(start, start + page.size)
})

function buildParams() {
  return {
    onlyEnabled: false,
    status: query.status === '' ? undefined : query.status
  }
}

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await fetchCategories(buildParams())
  } finally {
    loading.value = false
  }
}

function fillForm(row = {}) {
  form.id = row.id || null
  form.categoryName = row.categoryName || ''
  form.categoryCode = row.categoryCode || ''
  form.description = row.description || ''
  form.sort = row.sort || 0
  form.status = row.status ?? 1
}

function search() {
  page.current = 1
  loadCategories()
}

function openCreate() {
  editingCategory.value = null
  fillForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingCategory.value = row
  fillForm(row)
  dialogVisible.value = true
}

async function saveCategory() {
  const payload = {
    categoryName: form.categoryName,
    categoryCode: form.categoryCode,
    description: form.description,
    sort: form.sort,
    status: form.status
  }
  try {
    if (editingCategory.value) await updateCategory(form.id, payload)
    else await createCategory(payload)
    dialogVisible.value = false
    showAppToast({ message: '分类已保存' })
    loadCategories()
  } catch (error) {
    showErrorToast(error, '保存分类失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除分类 ${row.categoryName}？`, '确认操作', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteCategory(row.id)
    showAppToast({ message: '分类已删除' })
    loadCategories()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    showErrorToast(error, '删除分类失败')
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
