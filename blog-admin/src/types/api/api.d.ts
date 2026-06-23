/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 */
declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数 */
    interface PaginationParams {
      /** 当前页码 */
      pageNum: number
      /** 每页条数 */
      pageSize: number
      /** 总条数 */
      total?: number
    }

    /** 通用搜索参数 */
    type CommonSearchParams = Pick<PaginationParams, 'pageNum' | 'pageSize'>

    /** 分页响应基础结构 */
    interface PaginatedResponse<T = any> {
      records: T[]
      current: number
      size: number
      total: number
    }

    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录参数 */
    interface LoginParams {
      username: string
      password: string
    }

    /** 登录响应（对应后端 LoginVO） */
    interface LoginResponse {
      userId: number
      username: string
      nickname: string
      token: string
      tokenName: string
      tokenTimeout: number
    }

    /** 用户信息（对应后端 UserInfoVO，附加权限字段） */
    interface UserInfo {
      id: number
      username: string
      nickname: string
      email: string
      phone: string
      avatar: string
      intro: string
      status: number
      createTime: string
      /** 前端权限按钮列表（由 AuthService 填充） */
      buttons: string[]
      /** 用户角色列表 */
      roles: string[]
    }
  }

  /**
   * 分类相关类型定义
   */
  namespace Category {
    // 分类DTO
    interface CategoryDTO {
      /** 分类名称 */
      categoryName: string
      /** 分类描述 */
      categoryDesc?: string
      /** 排序 */
      sort?: number
    }

    interface CategoryVO {
      /** 分类ID */
      id: number
      /** 分类名称 */
      categoryName: string
      /** 分类描述 */
      categoryDesc: string
      /** 排序 */
      sort: number
      /** 文章数量 */
      articleCount: number
      /** 创建时间 */
      createTime: string // ISO 8601 格式
    }
  }

  /**
   * 标签相关类型定义
   */
  namespace Tag {
    // 标签DTO
    interface TagDTO {
      /** 标签名称 */
      tagName: string
      /** 标签颜色 */
      color: string
    }

    interface TagVO {
      /** 标签ID */
      id: number
      /** 标签名称 */
      tagName: string
      /** 标签颜色 */
      color: string
      /** 文章数量 */
      articleCount: number
      /** 创建时间 */
      createTime: string // ISO 8601 格式
    }
  }

  /**
   * 文章相关类型定义
   */
  namespace Article {
    // 文章DTO
    interface ArticleDTO {
      /** 文章标题 */
      title: string
      /** 文章摘要 */
      summary?: string
      /** 封面图 */
      coverImage?: string
      /** 文章内容(Markdown) */
      content: string
      /** 分类ID */
      categoryId?: number
      /** 标签ID列表 */
      tagIds?: number[]
      /** 是否置顶 0-否 1-是 */
      isTop?: number
      /** 是否草稿 0-否 1-是 */
      isDraft?: number
    }

    // 文章分页查询
    interface ArticlePageQuery extends Common.PaginationParams {
      /** 分类ID */
      categoryId?: number
      /** 标签ID */
      tagId?: number
      /** 关键词 */
      keyword?: string
    }

    interface ArticleListVO {
      /** 文章ID */
      id: number
      /** 作者ID */
      userId: number
      /** 作者昵称 */
      authorNickname: string
      /** 作者头像 */
      authorAvatar: string
      /** 分类ID */
      categoryId: number
      /** 分类名称 */
      categoryName: string
      /** 文章标题 */
      title: string
      /** 文章摘要 */
      summary: string
      /** 封面图 */
      coverImage: string
      /** 是否置顶 */
      isTop: number
      /** 浏览量 */
      viewCount: number
      /** 点赞数 */
      likeCount: number
      /** 评论数 */
      commentCount: number
      /** 收藏数 */
      collectCount: number
      /** 创建时间 */
      createTime: string // ISO 8601 格式
    }

    interface ArticleVO {
      /** 文章ID */
      id: number
      /** 作者ID */
      userId: number
      /** 作者昵称 */
      authorNickname: string
      /** 作者头像 */
      authorAvatar: string
      /** 分类ID */
      categoryId: number
      /** 分类名称 */
      categoryName: string
      /** 文章标题 */
      title: string
      /** 文章摘要 */
      summary: string
      /** 封面图 */
      coverImage: string
      /** 文章内容(Markdown) */
      content: string
      /** HTML内容 */
      htmlContent: string
      /** 是否置顶 */
      isTop: number
      /** 是否草稿 */
      isDraft: number
      /** 浏览量 */
      viewCount: number
      /** 点赞数 */
      likeCount: number
      /** 评论数 */
      commentCount: number
      /** 收藏数 */
      collectCount: number
      /** 标签列表 */
      tags: Tag.TagVO[]
      /** 是否已点赞 */
      isLiked: boolean
      /** 是否已收藏 */
      isCollected: boolean
      /** 创建时间 */
      createTime: string // ISO 8601 格式
      /** 更新时间 */
      updateTime: string // ISO 8601 格式
    }
  }
}
