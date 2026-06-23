import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { LanguageEnum } from '@/enums/appEnum.ts'
import { router } from '@/router'
import { useMenuStore, useSettingStore, useWorkTabStore } from '@/stores'
import type { AppRouteRecord } from '@/types'
import { setPageTitle, StorageConfig } from '@/utils'
import { resetRouterState } from '@/router/guards/beforeEach'
import { RoutesAlias } from '@/router/routesAlias'
import { authApi } from '@/api/modules/auth'

/**
 * 用户状态管理
 * 管理用户登录状态、个人信息、语言设置、搜索历史、锁屏状态等
 */
export const useUserStore = defineStore(
  'userStore',
  () => {
    // 语言设置
    const language = ref<LanguageEnum>(LanguageEnum.ZH)
    // 登录状态
    const isLogin = ref(false)
    // 用户信息
    const userInfo = ref<Partial<Api.Auth.UserInfo>>({})
    // 搜索历史记录
    const searchHistory = ref<AppRouteRecord[]>([])
    // 访问令牌
    const accessToken = ref('')
    // 刷新令牌
    const refreshToken = ref('')

    // 获取用户信息
    const getUserInfo = computed(() => userInfo.value)
    // 计算属性：获取设置状态
    const getSettingState = computed(() => useSettingStore().$state)
    // 计算属性：获取工作台状态
    const getWorkTabState = computed(() => useWorkTabStore().$state)

    /**
     * 设置用户信息
     * @param newInfo 新的用户信息
     */
    const setUserInfo = (newInfo: Api.Auth.UserInfo) => {
      userInfo.value = newInfo
    }
    /**
     * 设置登录状态
     * @param status 登录状态
     */
    const setLoginStatus = (status: boolean) => {
      isLogin.value = status
    }
    /**
     * 设置语言
     * @param lang 语言枚举值
     */
    const setLanguage = (lang: LanguageEnum) => {
      setPageTitle(router.currentRoute.value)
      language.value = lang
    }
    /**
     * 设置搜索历史
     * @param list 搜索历史列表
     */
    const setSearchHistory = (list: AppRouteRecord[]) => {
      searchHistory.value = list
    }

    /**
     * 设置令牌
     * @param newAccessToken 访问令牌
     * @param newRefreshToken 刷新令牌（可选）
     */
    const setToken = (newAccessToken: string, newRefreshToken?: string) => {
      accessToken.value = newAccessToken
      if (newRefreshToken) {
        refreshToken.value = newRefreshToken
      }
    }

    /**
     * 退出登录
     * 清空所有用户相关状态并跳转到登录页
     * 如果是同一账号重新登录，保留工作台标签页
     *
     * @param skipServerLogout 是否跳过服务端注销请求（401 场景下 token 已失效，无需调用）
     */
    const logOut = async (skipServerLogout: boolean = false) => {
      // 保存当前用户 ID，用于下次登录时判断是否为同一用户
      const currentUserId = userInfo.value.id
      if (currentUserId) {
        localStorage.setItem(StorageConfig.LAST_USER_ID_KEY, String(currentUserId))
      }

      // 通知服务端注销（失败也继续本地清理）
      if (!skipServerLogout) {
        try {
          await authApi.logout()
        } catch {
          // 服务端注销失败不影响本地退出
        }
      }

      // 清空用户信息
      userInfo.value = {}
      // 重置登录状态
      isLogin.value = false
      // 清空访问令牌
      accessToken.value = ''
      // 清空刷新令牌
      refreshToken.value = ''
      // 注意：不清空工作台标签页，等下次登录时根据用户判断
      // 清空主页路径
      useMenuStore().setHomePath('')
      // 重置路由状态
      resetRouterState(500)
      // 跳转到登录页，携带当前路由作为 redirect 参数
      const currentRoute = router.currentRoute.value
      const redirect = currentRoute.path !== RoutesAlias.Login ? currentRoute.fullPath : undefined
      router.push({
        name: 'Login',
        query: redirect ? { redirect } : undefined
      })
    }

    /**
     * 检查并清理工作台标签页
     * 如果不是同一用户登录，清空工作台标签页
     * 应在登录成功后调用
     */
    const checkAndClearWorkTabs = () => {
      const lastUserId = localStorage.getItem(StorageConfig.LAST_USER_ID_KEY)
      const currentUserId = userInfo.value.id

      // 无法获取当前用户 ID，跳过检查
      if (!currentUserId) return

      // 首次登录或缓存已清除，保留现有标签页
      if (!lastUserId) {
        return
      }

      // 不同用户登录，清空工作台标签页
      if (String(currentUserId) !== lastUserId) {
        const workTabStore = useWorkTabStore()
        workTabStore.opened = []
        workTabStore.keepAliveExclude = []
      }

      // 清除临时存储
      localStorage.removeItem(StorageConfig.LAST_USER_ID_KEY)
    }

    return {
      language,
      isLogin,
      userInfo,
      searchHistory,
      accessToken,
      refreshToken,
      getUserInfo,
      getSettingState,
      getWorkTabState,
      setUserInfo,
      setLoginStatus,
      setLanguage,
      setSearchHistory,
      setToken,
      logOut,
      checkAndClearWorkTabs
    }
  },
  {
    persist: {
      key: 'user',
      storage: localStorage
    }
  }
)
