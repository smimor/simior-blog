export default {
  extends: ['stylelint-config-standard-scss', 'stylelint-config-recommended-vue'],

  rules: {
    /**
     * Vue 项目通常使用 kebab-case 或 BEM 命名，
     * 不强制 class 命名格式。
     */
    'selector-class-pattern': null,

    /**
     * Scoped 样式、嵌套选择器容易误报，
     * 建议关闭。
     */
    'no-descending-specificity': null,

    /**
     * 允许 rgb()、rgba() 等现代颜色函数写法。
     */
    'color-function-notation': null,

    /**
     * 不强制透明度使用百分比。
     */
    'alpha-value-notation': null
  }
}
