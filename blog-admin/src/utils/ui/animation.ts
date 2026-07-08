/**
 * 主题动画工具模块
 *
 * 提供主题切换的视觉动画效果
 *
 * ## 主要功能
 *
 * - 基于鼠标点击位置的圆形扩散动画
 * - View Transition API 支持（现代浏览器）
 * - 降级处理（不支持动画的浏览器）
 * - 暗黑主题切换过渡效果
 * - 页面刷新时的主题过渡优化
 *
 * ## 使用场景
 *
 * - 明暗主题切换
 * - 提升用户体验的视觉反馈
 * - 页面刷新时的平滑过渡
 *
 * ## 技术实现
 *
 * - 使用 CSS 变量存储点击位置和半径
 * - 利用 View Transition API 实现流畅动画
 * - 通过 CSS class 控制过渡效果
 * - 自动计算最大扩散半径
 *
 * @module utils/theme/animation
 */

import { useCommon, useTheme } from '@/hooks'
import { SystemThemeEnum } from '@/enums/appEnum'
import { useSettingStore } from '@/stores'

const { LIGHT, DARK } = SystemThemeEnum

/** 缓存 composable 实例，避免每次切换主题重新创建（防止内存泄漏） */
let cachedTheme: ReturnType<typeof useTheme> | null = null

/**
 * 主题切换动画
 * @param e 鼠标点击事件
 */
export const themeAnimation = (e: any) => {
  // 懒初始化 composable（确保 Pinia 已就绪）
  // if (!cachedTheme) cachedTheme = useTheme()
  // const { colorMode } = cachedTheme

  const x = e.clientX
  const y = e.clientY
  // 计算鼠标点击位置距离视窗的最大圆半径
  const radius = Math.hypot(Math.max(x, innerWidth - x), Math.max(y, innerHeight - y))

  document.documentElement.style.setProperty('--vt-x', `${x}px`)
  document.documentElement.style.setProperty('--vt-y', `${y}px`)
  document.documentElement.style.setProperty('--vt-r', `${radius}px`)

  // 检查浏览器是否支持 View Transitions API
  if (!document.startViewTransition) {
    toggleTheme()
    return
  }

  // 主题切换动画
  document.startViewTransition(() => {
    toggleTheme()
  })
}

/**
 * 切换主题
 */
const toggleTheme = () => {
  if (!cachedTheme) cachedTheme = useTheme()
  cachedTheme.switchThemeStyles(useSettingStore().systemThemeType === LIGHT ? DARK : LIGHT)
  useCommon().refresh()
}
