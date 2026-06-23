<script setup lang="ts">
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

import { useSettingStore } from '@/stores'
import { articleApi, categoryApi, tagApi } from '@/api'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
  const route = useRoute()
  const { isDark } = storeToRefs(useSettingStore())

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const isEditMode = ref(false)
  const editId = ref<number | undefined>()

  const tagList = ref<{ label: string; value: number }[]>([])
  const categoryList = ref<{ label: string; value: number }[]>([])

  const articleForm = reactive<Api.Article.ArticleDTO>({
    title: '',
    summary: '',
    content: '',
    categoryId: undefined,
    tagIds: [],
    isTop: 0,
    isDraft: 0
  })

  const rules: FormRules = {
    title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择文章分类', trigger: 'change' }]
  }

  /** 加载标签和分类选项 */
  const loadOptions = async () => {
    try {
      const [tagRes, categoryRes] = await Promise.all([
        tagApi.getTagList(),
        categoryApi.getCategoryList()
      ])
      tagList.value = (tagRes || []).map((t: Api.Tag.TagVO) => ({
        label: t.tagName,
        value: t.id
      }))
      categoryList.value = (categoryRes || []).map((c: Api.Category.CategoryVO) => ({
        label: c.categoryName,
        value: c.id
      }))
    } catch (e) {
      console.error('加载选项失败:', e)
    }
  }

  /** 加载文章详情（编辑模式） */
  const loadArticle = async (id: number) => {
    try {
      const res = await articleApi.getArticleDetail(id)
      const article = res as Api.Article.ArticleVO
      articleForm.title = article.title
      articleForm.summary = article.summary
      articleForm.content = article.content
      articleForm.categoryId = article.categoryId
      articleForm.tagIds = article.tags?.map((t) => t.id) || []
      articleForm.isTop = article.isTop
      articleForm.isDraft = article.isDraft
    } catch (e) {
      console.error('加载文章失败:', e)
      ElMessage.error('加载文章失败')
    }
  }

  /** 提交文章 */
  const handleSubmit = async () => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
      if (isEditMode.value && editId.value) {
        await articleApi.updateArticle(editId.value, articleForm)
        ElMessage.success('更新成功')
      } else {
        await articleApi.publishArticle(articleForm)
        ElMessage.success('发布成功')
      }
      router.push({ name: 'ArticleList' })
    } catch (e) {
      console.error('提交文章失败:', e)
    } finally {
      submitting.value = false
    }
  }

  /** 重置表单 */
  const handleReset = () => {
    formRef.value?.resetFields()
    articleForm.content = ''
  }

  onMounted(async () => {
    await loadOptions()
    const id = route.query.id
    if (id) {
      isEditMode.value = true
      editId.value = Number(id)
      await loadArticle(editId.value)
    }
  })
</script>
<template>
  <div class="page-content">
    <el-form ref="formRef" :model="articleForm" :rules="rules" class="form">
      <el-form-item label="文章标题" prop="title">
        <div class="w-full flex items-center gap-col-2">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
          <el-button type="primary" class="ml-2!" :loading="submitting" @click="handleSubmit">
            {{ isEditMode ? '更新' : '发布' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form-item>

      <el-form-item label="文章摘要" prop="summary">
        <el-input
          v-model="articleForm.summary"
          type="textarea"
          :rows="2"
          placeholder="请输入文章摘要（可选）"
        />
      </el-form-item>

      <el-form-item label="文章分类" prop="categoryId">
        <el-select
          v-model="articleForm.categoryId"
          placeholder="请选择分类"
          clearable
          filterable
          style="width: 200px"
        >
          <el-option v-for="c in categoryList" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="文章标签" prop="tagIds">
        <el-select
          v-model="articleForm.tagIds"
          multiple
          filterable
          placeholder="请选择标签"
          style="width: 300px"
        >
          <el-option v-for="t in tagList" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <MdEditor v-model="articleForm.content" :theme="isDark ? 'dark' : 'light'" />
      </el-form-item>
    </el-form>
  </div>
</template>

<style lang="scss" scoped>
  .form {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100%;

    .el-form-item:last-child {
      flex: 1;
      overflow: hidden;
      margin-bottom: 0;

      :deep(.el-form-item__content) {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }
    }
  }
</style>
