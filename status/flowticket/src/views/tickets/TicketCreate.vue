<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">提交工单</h2>
        <p class="page-subtitle">描述问题、选择分类与优先级，系统会进入客服受理队列。</p>
      </div>
    </div>

    <div class="panel form-panel">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="请简要说明问题" />
        </el-form-item>
        <el-form-item label="问题分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" filterable>
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio-button :label="1">低</el-radio-button>
            <el-radio-button :label="2">中</el-radio-button>
            <el-radio-button :label="3">高</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="问题描述" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="7" maxlength="500" show-word-limit placeholder="请补充问题现象、发生时间、影响范围等信息" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交工单</el-button>
          <el-button @click="$router.push('/tickets')">返回列表</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createTicket } from '@/api/ticket'
import { fetchCategories } from '@/api/category'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const categories = ref([])

const form = reactive({
  title: '',
  categoryId: '',
  priority: 2,
  content: '',
  contactName: '',
  contactPhone: '',
  contactEmail: ''
})

const rules = {
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择问题分类', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  content: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

async function loadCategories() {
  categories.value = await fetchCategories({ onlyEnabled: true })
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createTicket({ ...form })
    ElMessage.success('工单已提交')
    router.push('/tickets')
  } finally {
    submitting.value = false
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.form-panel {
  max-width: 860px;
}
</style>
