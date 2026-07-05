/**
 * useTheme - 系统主题管理
 *
 * 提供完整的主题切换和管理功能，支持亮色、暗色和自动模式。
 * 自动处理主题切换时的过渡效果，确保切换流畅无闪烁。
 *
 * ## 主要功能
 *
 * 1. 主题切换 - 支持亮色、暗色、自动三种主题模式
 * 2. 自动模式 - 根据系统偏好自动切换主题
 * 3. 颜色适配 - 自动调整主题色的明暗变体（9 个层级）
 * 4. 过渡优化 - 切换时临时禁用过渡效果，避免闪烁
 * 5. 状态持久化 - 主题设置自动保存到 store
 *
 * ## 使用示例
 *
 * ```typescript
 * const { switchThemeStyles } = useTheme()
 *
 * // 切换到暗色主题
 * switchThemeStyles(SystemThemeEnum.DARK)
 *
 * // 切换到亮色主题
 * switchThemeStyles(SystemThemeEnum.LIGHT)
 *
 * // 切换到自动模式（跟随系统）
 * switchThemeStyles(SystemThemeEnum.AUTO)
 * ```
 *
 * @module useTheme
 */
import { useSettingStore } from '@/stores'
import { SystemThemeEnum } from '@/enums/appEnum.ts'
import { getDarkColor, getLightColor, setElementThemeColor } from '@/utils'
import { usePreferredDark } from '@vueuse/core'

export function useTheme() {
  const settingStore = useSettingStore()
  const prefersDark = usePreferredDark()

  // 使用 useColorMode 管理主题
  const colorMode = useColorMode({
    selector: 'html',
    attribute: 'class',
    modes: {
      light: 'light',
      dark: 'dark'
    },
    storageKey: 'blog-theme-mode',
    initialValue: settingStore.systemThemeMode
  })

  // 设置系统主题
  const setSystemTheme = (theme: SystemThemeEnum, themeMode?: SystemThemeEnum) => {
    const isDark = theme === SystemThemeEnum.DARK

    if (!themeMode) {
      themeMode = theme
    }

    // 使用 useColorMode 设置主题
    if (themeMode === SystemThemeEnum.AUTO) {
      colorMode.value = themeMode
    } else {
      colorMode.value = theme
    }

    // 设置按钮颜色加深或变浅
    const primary = settingStore.systemThemeColor

    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.setProperty(
        `--el-color-primary-light-${i}`,
        isDark ? `${getDarkColor(primary, i / 10)}` : `${getLightColor(primary, i / 10)}`
      )
    }

    // 更新 store
    settingStore.setGlopTheme(theme, themeMode)
  }

  // 自动设置系统主题
  const setSystemAutoTheme = () => {
    const theme = prefersDark.value ? SystemThemeEnum.DARK : SystemThemeEnum.LIGHT
    setSystemTheme(theme, SystemThemeEnum.AUTO)
  }

  // 切换主题
  const switchThemeStyles = (theme: SystemThemeEnum) => {
    if (theme === SystemThemeEnum.AUTO) {
      setSystemAutoTheme()
    } else {
      setSystemTheme(theme)
    }
  }

  return {
    setSystemTheme,
    setSystemAutoTheme,
    switchThemeStyles,
    prefersDark,
    colorMode
  }
}

/**
 * 初始化主题系统
 */
export function initializeTheme() {
  const settingStore = useSettingStore()
  const { switchThemeStyles } = useTheme()
  // 设置主题颜色
  setElementThemeColor(settingStore.systemThemeColor)

  switchThemeStyles(settingStore.systemThemeMode)
  // 设置圆角
  document.documentElement.style.setProperty('--custom-radius', `${settingStore.customRadius}rem`)
}
