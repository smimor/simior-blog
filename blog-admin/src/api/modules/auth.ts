import request from '@/utils/http'

/**
 * 认证模块 API
 *
 * 对应后端 AuthController（/v1/auth）
 * 负责登录、登出、获取当前用户信息等认证相关操作
 */
const BASE_URL = '/v1/auth'

export const authApi = {
  /**
   * 用户登录
   *
   * POST /v1/auth/login
   * @param params 登录参数（用户名 + 密码）
   */
  login(params: Api.Auth.LoginParams) {
    return request.post<Api.Auth.LoginResponse>({
      url: `${BASE_URL}/login`,
      params
    })
  },

  /**
   * 获取当前登录用户信息
   *
   * GET /v1/auth/info
   */
  getUserInfo() {
    return request.get<Api.Auth.UserInfo>({
      url: `${BASE_URL}/info`
    })
  },

  /**
   * 用户登出
   *
   * POST /v1/auth/logout
   */
  logout() {
    return request.post<string>({
      url: `${BASE_URL}/logout`
    })
  }
}
