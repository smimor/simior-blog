export default {
  extends: [
    'stylelint-config-standard-scss',
    'stylelint-config-recommended-vue',
    'stylelint-config-recess-order'
  ],

  rules: {
    /**
     * 允许 rgb()、rgba() 等现代颜色函数写法。
     */
    'color-function-notation': null,

    /**
     * 不强制透明度使用百分比。
     */
    'alpha-value-notation': null,

    'selector-class-pattern': null, // 选择器类名命名规则
    'custom-property-pattern': null, // 自定义属性命名规则
    'keyframes-name-pattern': null, // 动画帧节点样式命名规则
    'no-descending-specificity': null, // 允许无降序特异性
    'no-empty-source': null, // 允许空样式
    'property-no-vendor-prefix': null, // 允许属性前缀

    // 允许 global 、export 、deep伪类
    'selector-pseudo-class-no-unknown': [
      true,
      {
        ignorePseudoClasses: ['global', 'export', 'deep']
      }
    ],
    // 允许未知属性
    'property-no-unknown': [
      true,
      {
        ignoreProperties: []
      }
    ],
    // 允许未知规则
    'at-rule-no-unknown': [
      true,
      {
        ignoreAtRules: [
          'apply',
          'use',
          'mixin',
          'include',
          'extend',
          'each',
          'if',
          'else',
          'for',
          'while',
          'reference'
        ]
      }
    ],
    'scss/at-rule-no-unknown': [
      true,
      {
        ignoreAtRules: [
          'apply',
          'use',
          'mixin',
          'include',
          'extend',
          'each',
          'if',
          'else',
          'for',
          'while',
          'reference'
        ]
      }
    ]
  }
}
