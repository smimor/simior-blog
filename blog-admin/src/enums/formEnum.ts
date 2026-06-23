/**
 * 表单相关枚举定义模块
 *
 * ## 主要功能
 *
 * - 页面模式枚举（新增、编辑）
 * - 表格尺寸枚举（默认、紧凑、宽松）
 *
 * @module enums/formEnum
 */

// 表格大小
export const TableSizeEnum = {
  DEFAULT: 'default',
  SMALL: 'small',
  LARGE: 'large'
}
export type TableSizeEnum = (typeof TableSizeEnum)[keyof typeof TableSizeEnum]
