import { ref } from 'vue'
import { articleApi } from '@/api'

export const useArticle = () => {
  // 分页查询参数
  const params = reactive<Api.Article.ArticlePageQuery>({
    pageNum: 1,
    pageSize: 5
  })

  // 文章列表
  const articleList = ref<Api.Article.ArticleListVO[]>([])
  const total = ref(0) // 文章总数
  const loading = ref(false)

  /**
   * 获取文章列表
   */
  const getArticleList = async () => {
    loading.value = true
    try {
      const res = await articleApi.getArticleList(params)
      articleList.value = res.records
      total.value = res.total
    } catch (e) {
      console.error('获取文章列表失败:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 改变分页大小
   * @param val
   */
  const handlePageSizeChange = async (val: number) => {
    params.pageSize = val
    params.pageNum = 1
    await getArticleList()
  }

  /**
   * 改变当前页码
   * @param val
   */
  const handleCurrentPageChange = async (val: number) => {
    params.pageNum = val
    await getArticleList()
  }

  onMounted(async () => {
    await getArticleList()
  })

  return {
    params,
    total,
    articleList,
    loading,
    getArticleList,
    handlePageSizeChange,
    handleCurrentPageChange
  }
}
