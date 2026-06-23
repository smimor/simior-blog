/**
 * 系统级别枚举定义模块
 *
 * ## 主要功能
 *
 * - 主题类型枚举（亮色、暗色、自动）
 * - 菜单主题枚举（设计、亮色、暗色）
 * - 语言类型枚举（中文、英文）
 * - 容器宽度枚举（全屏、固定）
 * - 菜单宽度枚举（收起宽度）
 */
/**
 * 系统主题
 */
export const SystemThemeEnum = {
  /** 暗色主题 */
  DARK: 'dark',
  /** 亮色主题 */
  LIGHT: 'light',
  /** 自动主题（跟随系统） */
  AUTO: 'auto'
} as const
export type SystemThemeEnum = (typeof SystemThemeEnum)[keyof typeof SystemThemeEnum]

/**
 * 菜单主题枚举
 */
export const MenuThemeEnum = {
  /** 暗色主题 */
  DARK: 'dark',
  /** 亮色主题 */
  LIGHT: 'light',
  /** 设计主题 */
  DESIGN: 'design'
} as const
export type MenuThemeEnum = (typeof MenuThemeEnum)[keyof typeof MenuThemeEnum]

/**
 * 菜单宽度
 */
export const MenuWidth = {
  /** 展开宽度 */
  OPEN: '230px',
  /** 收起宽度 */
  CLOSE: '4.75rem'
}

/**
 * 语言类型
 */
export const LanguageEnum = {
  /** 中文 */
  ZH: 'zh',
  /** 英文 */
  EN: 'en'
} as const
export type LanguageEnum = (typeof LanguageEnum)[keyof typeof LanguageEnum]

/**
 * 容器宽度枚举
 */
export const ContainerWidthEnum = {
  /** 全屏宽度 */
  FULL: '100%',
  /** 固定宽度 */
  BOXED: '1200px'
} as const
export type ContainerWidthEnum = (typeof ContainerWidthEnum)[keyof typeof ContainerWidthEnum]
