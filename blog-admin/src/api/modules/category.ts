import request from '@/utils/http'

/**
 * 分类模块 API
 *
 * 对应后端 CategoryController（/v1/categories）
 * 负责文章分类的增删改查操作
 */
const BASE_URL = '/v1/categories'

export const categoryApi = {
  /**
   * 获取所有分类列表
   *
   * GET /v1/categories
   */
  getCategoryList() {
    return request.get<Api.Category.CategoryVO[]>({
      url: BASE_URL
    })
  },

  /**
   * 创建分类
   *
   * POST /v1/categories
   * @param data 分类数据（名称、描述等）
   */
  createCategory(data: Api.Category.CategoryDTO) {
    return request.post<number>({
      url: BASE_URL,
      data
    })
  },

  /**
   * 更新分类
   *
   * PUT /v1/categories/{id}
   * @param id 分类ID
   * @param data 分类数据
   */
  updateCategory(id: number, data: Api.Category.CategoryDTO) {
    return request.put<string>({
      url: `${BASE_URL}/${id}`,
      data
    })
  },

  /**
   * 删除分类
   *
   * DELETE /v1/categories/{id}
   * @param id 分类ID
   */
  deleteCategory(id: number) {
    return request.del<string>({
      url: `${BASE_URL}/${id}`
    })
  }
}
