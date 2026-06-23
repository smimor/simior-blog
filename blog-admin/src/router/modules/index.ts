import type { AppRouteRecord } from '@/types'
import { dashboardRoutes } from './dashboard.ts'
import { systemRoutes } from './system.ts'
import { articleRoutes } from './article.ts'
import { exceptionRoutes } from './exception'
import { helpRoutes } from './help'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  systemRoutes,
  articleRoutes,
  exceptionRoutes,
  ...helpRoutes
]
