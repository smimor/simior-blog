import request from '@/utils/http'

/**
 * 文章模块 API
 *
 * 对应后端 ArticleController（/v1/articles）
 * 负责文章的增删改查、点赞、收藏等操作
 */
const BASE_URL = '/v1/articles'

export const articleApi = {
  /**
   * 获取文章分页列表
   *
   * GET /v1/articles
   * @param params 分页查询参数（页码、每页条数、分类、标签等）
   */
  getArticleList(params: Api.Article.ArticlePageQuery) {
    return request.get<Api.Common.PaginatedResponse<Api.Article.ArticleListVO>>({
      url: BASE_URL,
      params
    })
  },

  /**
   * 获取文章详情
   *
   * GET /v1/articles/{id}
   * @param id 文章ID
   */
  getArticleDetail(id: number) {
    return request.get<Api.Article.ArticleVO>({
      url: `${BASE_URL}/${id}`
    })
  },

  /**
   * 发布文章
   *
   * POST /v1/articles
   * @param data 文章数据（标题、内容、分类、标签等）
   */
  publishArticle(data: Api.Article.ArticleDTO) {
    return request.post<number>({
      url: BASE_URL,
      data
    })
  },

  /**
   * 更新文章
   *
   * PUT /v1/articles/{id}
   * @param id 文章ID
   * @param data 文章数据
   */
  updateArticle(id: number, data: Api.Article.ArticleDTO) {
    return request.put<string>({
      url: `${BASE_URL}/${id}`,
      data
    })
  },

  /**
   * 删除文章
   *
   * DELETE /v1/articles/{id}
   * @param id 文章ID
   */
  deleteArticle(id: number) {
    return request.del<string>({
      url: `${BASE_URL}/${id}`
    })
  }
}
