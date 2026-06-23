/**
 * 路由全局前置守卫模块
 *
 * 提供完整的路由导航守卫功能
 *
 * @module router/guards/beforeEach
 */
import type { RouteLocationNormalized, RouteLocationRaw, Router } from 'vue-router'
import { nextTick } from 'vue'
import NProgress from 'nprogress'
import { useMenuStore, useSettingStore, useUserStore, useWorkTabStore } from '@/stores'
import { setWorkTab } from '@/utils/navigation'
import { setPageTitle } from '@/utils/router'
import { RoutesAlias } from '../routesAlias'
import { staticRoutes } from '../routes/staticRoutes'
import { loadingService } from '@/utils/ui'
import { useCommon } from '@/hooks'
import { authApi } from '@/api'
import { ApiStatus } from '@/utils/http/status'
import { isHttpError } from '@/utils/http/error'
import { IframeRouteManager, MenuProcessor, RoutePermissionValidator, RouteRegistry } from '../core'

// ==================== 自定义类型定义 ====================
/**
 * 路由全局前置守卫的标准化返回值类型
 */
export type RouteGuardResult = boolean | RouteLocationRaw | void
// =======================================================

let routeRegistry: RouteRegistry | null = null
const menuProcessor = new MenuProcessor()
let pendingLoading = false
let routeInitFailed = false
let routeInitInProgress = false

export function getPendingLoading(): boolean {
  return pendingLoading
}
export function resetPendingLoading(): void {
  pendingLoading = false
}
export function getRouteInitFailed(): boolean {
  return routeInitFailed
}
export function resetRouteInitState(): void {
  routeInitFailed = false
  routeInitInProgress = false
}

/**
 * 设置路由全局前置守卫
 */
export function setupBeforeEachGuard(router: Router): void {
  routeRegistry = new RouteRegistry(router)

  router.beforeEach(
    async (
      to: RouteLocationNormalized,
      from: RouteLocationNormalized
    ): Promise<RouteGuardResult> => {
      try {
        return await handleRouteGuard(to, from, router)
      } catch (error) {
        console.error('[RouteGuard] 路由守卫处理失败:', error)
        closeLoading()
        return { name: 'Exception500' }
      }
    }
  )
}

function closeLoading(): void {
  if (pendingLoading) {
    nextTick(() => {
      loadingService.hideLoading()
      pendingLoading = false
    })
  }
}

/**
 * 处理路由守卫逻辑
 */
async function handleRouteGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  router: Router
): Promise<RouteGuardResult> {
  const settingStore = useSettingStore()
  const userStore = useUserStore()

  // 启动进度条
  if (settingStore.showNprogress) {
    NProgress.start()
  }

  // 1. 检查登录状态
  const loginCheckResult = handleLoginStatus(to, userStore)
  if (loginCheckResult !== true) {
    return loginCheckResult
  }

  // 2. 检查路由初始化是否已失败（防止死循环）
  if (routeInitFailed) {
    if (to.matched.length > 0) {
      return true
    } else {
      return { name: 'Exception500', replace: true }
    }
  }

  // 3. 【核心修正】处理动态路由注册
  // 如果尚未注册路由，并且用户已登录，必须先走动态路由注册流程
  // 此时不要去看 to.matched.length，因为路由还没注册，哪怕合法的路由 matched 也会是 0
  if (!routeRegistry?.isRegistered() && userStore.isLogin) {
    if (routeInitInProgress) {
      return false
    }
    return await handleDynamicRoutes(to, router)
  }

  // 4. 处理根路径重定向
  const redirectResult = handleRootPathRedirect(to)
  if (redirectResult) {
    return redirectResult
  }

  // 5. 处理已匹配的路由（此时动态路由已注册完毕，matched 才是真实可信的）
  if (to.matched.length > 0) {
    setWorkTab(to)
    setPageTitle(to)
    return true
  }

  // 6. 动态路由注册完了，依然未匹配到路由，跳转到 404
  return { name: 'Exception404' }
}

/**
 * 处理登录状态
 */
function handleLoginStatus(
  to: RouteLocationNormalized,
  userStore: ReturnType<typeof useUserStore>
): true | RouteLocationRaw {
  if (userStore.isLogin || to.path === RoutesAlias.Login || isStaticRoute(to.path)) {
    return true
  }

  userStore.logOut()
  return {
    name: 'Login',
    query: { redirect: to.fullPath }
  }
}

/**
 * 检查路由是否为静态路由
 */
function isStaticRoute(path: string): boolean {
  const checkRoute = (routes: any[], targetPath: string): boolean => {
    return routes.some((route) => {
      if (route.name === 'Exception404') {
        return false
      }

      const routePath = route.path
      const pattern = routePath.replace(/:[^/]+/g, '[^/]+').replace(/\*/g, '.*')
      const regex = new RegExp(`^${pattern}$`)

      if (regex.test(targetPath)) {
        return true
      }
      if (route.children && route.children.length > 0) {
        return checkRoute(route.children, targetPath)
      }
      return false
    })
  }

  return checkRoute(staticRoutes, path)
}

/**
 * 处理动态路由注册
 */
async function handleDynamicRoutes(
  to: RouteLocationNormalized,
  router: Router
): Promise<RouteGuardResult> {
  routeInitInProgress = true
  pendingLoading = true
  loadingService.showLoading()

  try {
    await fetchUserInfo()
    const menuList = await menuProcessor.getMenuList()

    if (!menuProcessor.validateMenuList(menuList)) {
      throw new Error('获取菜单列表失败，请重新登录')
    }

    routeRegistry?.register(menuList)

    const menuStore = useMenuStore()
    menuStore.setMenuList(menuList)
    menuStore.addRemoveRouteFns(routeRegistry?.getRemoveRouteFns() || [])

    IframeRouteManager.getInstance().save()
    useWorkTabStore().validateWorkTabs(router)

    // 静态路由不依赖菜单权限，初始化后直接恢复目标地址
    if (isStaticRoute(to.path)) {
      routeInitInProgress = false
      return {
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      }
    }

    const { homePath } = useCommon()
    const { path: validatedPath, hasPermission } = RoutePermissionValidator.validatePath(
      to.path,
      menuList,
      homePath.value || '/'
    )

    routeInitInProgress = false

    if (!hasPermission) {
      closeLoading()
      console.warn(`[RouteGuard] 用户无权限访问路径: ${to.path}，已跳转到首页`)
      return {
        path: validatedPath,
        replace: true
      }
    } else {
      // 有权限，重新触发一次导航以激活新注册的动态路由
      return {
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true
      }
    }
  } catch (error) {
    console.error('[RouteGuard] 动态路由注册失败:', error)
    closeLoading()

    if (isUnauthorizedError(error)) {
      routeInitInProgress = false
      return false
    }

    routeInitFailed = true
    routeInitInProgress = false

    if (isHttpError(error)) {
      console.error(`[RouteGuard] 错误码: ${error.code}, 消息: ${error.message}`)
    }

    return { name: 'Exception500', replace: true }
  }
}

/**
 * 获取用户信息
 */
async function fetchUserInfo(): Promise<void> {
  const userStore = useUserStore()
  const data = await authApi.getUserInfo()
  userStore.setUserInfo(data)
  userStore.checkAndClearWorkTabs()
}

/**
 * 重置路由相关状态
 */
export function resetRouterState(delay: number): void {
  setTimeout(() => {
    routeRegistry?.unregister()
    IframeRouteManager.getInstance().clear()

    const menuStore = useMenuStore()
    menuStore.removeAllDynamicRoutes()
    menuStore.setMenuList([])

    resetRouteInitState()
  }, delay)
}

/**
 * 处理根路径重定向到首页
 */
function handleRootPathRedirect(to: RouteLocationNormalized): RouteLocationRaw | false {
  if (to.path !== '/') {
    return false
  }

  const { homePath } = useCommon()
  if (homePath.value && homePath.value !== '/') {
    return { path: homePath.value, replace: true }
  }

  return false
}

function isUnauthorizedError(error: unknown): boolean {
  return isHttpError(error) && error.code === ApiStatus.unauthorized
}
