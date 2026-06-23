import request from '@/utils/http'

/**
 * 标签模块 API
 *
 * 对应后端 TagController（/v1/tags）
 * 负责文章标签的增删改查操作
 */
const BASE_URL = '/v1/tags'

export const tagApi = {
  /**
   * 获取所有标签列表
   *
   * GET /v1/tags
   */
  getTagList() {
    return request.get<Api.Tag.TagVO[]>({
      url: BASE_URL
    })
  },

  /**
   * 创建标签
   *
   * POST /v1/tags
   * @param data 标签数据（名称等）
   */
  createTag(data: Api.Tag.TagDTO) {
    return request.post<number>({
      url: BASE_URL,
      data
    })
  },

  /**
   * 更新标签
   *
   * PUT /v1/tags/{id}
   * @param id 标签ID
   * @param data 标签数据
   */
  updateTag(id: number, data: Api.Tag.TagDTO) {
    return request.put<string>({
      url: `${BASE_URL}/${id}`,
      data
    })
  },

  /**
   * 删除标签
   *
   * DELETE /v1/tags/{id}
   * @param id 标签ID
   */
  deleteTag(id: number) {
    return request.del<string>({
      url: `${BASE_URL}/${id}`
    })
  }
}
