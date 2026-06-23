<script setup lang="ts">
import { Delete, Edit, Hide, Search } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, categoryApi, tagApi } from '@/api'

// 获取全部文章信息
import { useArticle } from './composables/article.ts'

const router = useRouter()

  // 分页操作
  const {
    params,
    total,
    articleList,
    getArticleList,
    handlePageSizeChange,
    handleCurrentPageChange
  } = useArticle()

  const tagList = ref<{ label: string; value: number }[]>([])
  const categoryList = ref<{ label: string; value: number }[]>([])

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
      console.error('加载筛选项失败:', e)
    }
  }

  /** 搜索表单 */
  const inquire = reactive({
    keyword: '',
    categoryId: undefined as number | undefined,
    tagId: undefined as number | undefined
  })

  /** 执行搜索 */
  const handleSearch = () => {
    params.keyword = inquire.keyword || undefined
    params.categoryId = inquire.categoryId
    params.tagId = inquire.tagId
    params.pageNum = 1
    getArticleList()
  }

  /** 重置搜索 */
  const handleReset = () => {
    inquire.keyword = ''
    inquire.categoryId = undefined
    inquire.tagId = undefined
    params.keyword = undefined
    params.categoryId = undefined
    params.tagId = undefined
    params.pageNum = 1
    getArticleList()
  }

  /** 编辑文章 */
  const handleEdit = (row: Api.Article.ArticleListVO) => {
    router.push({ path: '/article/publish', query: { id: String(row.id) } })
  }

  /** 隐藏/显示文章（暂未实现后端接口，预留） */
  const handleHide = (row: Api.Article.ArticleListVO) => {
    ElMessage.info(`隐藏/显示文章: ${row.title}（功能待接入后端）`)
  }

  /** 删除文章 */
  const handleDelete = async (row: Api.Article.ArticleListVO) => {
    try {
      await ElMessageBox.confirm(`确定要删除文章「${row.title}」吗？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await articleApi.deleteArticle(row.id)
      ElMessage.success('删除成功')
      await getArticleList()
    } catch (e: any) {
      // 用户取消时不做处理
      if (e !== 'cancel') {
        console.error('删除文章失败:', e)
      }
    }
  }

  onMounted(() => {
    loadOptions()
  })
</script>
<template>
  <div class="article-list page-content">
    <el-form :inline="true" :model="inquire" :label-width="72" label-suffix=":">
      <el-form-item label="文章标题">
        <el-input
          v-model="inquire.keyword"
          :prefix-icon="Search"
          clearable
          placeholder="请输入文章标题"
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="文章标签">
        <el-select
          filterable
          clearable
          v-model="inquire.tagId"
          placeholder="请选择文章标签"
          style="width: 180px"
        >
          <el-option
            v-for="tag in tagList"
            :key="tag.value"
            :label="tag.label"
            :value="tag.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="文章分类">
        <el-select
          clearable
          filterable
          v-model="inquire.categoryId"
          placeholder="请选择文章分类"
          style="width: 180px"
        >
          <el-option
            v-for="category in categoryList"
            :key="category.value"
            :label="category.label"
            :value="category.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="articleList"
      border
      :cell-style="{ textAlign: 'center' }"
      :header-cell-style="{ 'text-align': 'center' }"
      style="width: 100%"
      max-height="100%"
    >
      <el-table-column type="selection" fixed="left" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="title" label="标题" width="120" />
      <el-table-column prop="summary" label="描述" width="180" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="{ row }">
          <el-tag type="warning">
            {{ row.categoryName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="coverImage" label="封面" min-width="120">
        <template #default="{ row }">
          <BlogImage :src="row.coverImage"></BlogImage>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布日期" width="180" />
      <el-table-column fixed="right" label="编辑" min-width="180">
        <template #default="{ row }">
          <el-button
            link
            type="success"
            size="small"
            @click="handleEdit(row as Api.Article.ArticleListVO)"
          >
            <el-icon>
              <Edit />
            </el-icon>
            编辑
          </el-button>
          <el-button
            link
            type="info"
            size="small"
            @click="handleHide(row as Api.Article.ArticleListVO)"
          >
            <el-icon>
              <Hide />
            </el-icon>
            隐藏
          </el-button>
          <el-button
            link
            type="danger"
            size="small"
            @click="handleDelete(row as Api.Article.ArticleListVO)"
          >
            <el-icon>
              <Delete />
            </el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="没有数据"></el-empty>
      </template>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="params.pageNum"
      v-model:page-size="params.pageSize"
      :page-sizes="[2, 5, 10, 15, 20, 25]"
      background
      layout="prev, pager, next, total,jumper"
      :total="total"
      @size-change="handlePageSizeChange"
      @current-change="handleCurrentPageChange"
      class="justify-end mt-5!"
    />
  </div>
</template>

<style lang="scss" scoped></style>
