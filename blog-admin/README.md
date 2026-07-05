# Dependencies

| 依赖                            | 作用说明                                                                     |
|-------------------------------|--------------------------------------------------------------------------|
| `@element-plus/icons-vue`     | Element Plus 官方图标库，为输入框清空按钮、日期选择器、分页器等组件提供默认图标，也可用于页面自定义图标。              |
| `@iconify/vue`                | Iconify Vue 组件，统一管理多套图标库（如 Material、Tabler、Carbon 等），当前采用运行时 API 按需加载图标。 |
| `@tailwindcss/vite`           | Tailwind CSS 官方 Vite 插件，负责在开发与构建阶段处理 Tailwind 样式。                        |
| `@vueuse/core`                | Vue Composition API 工具库，提供暗黑模式、事件监听、防抖节流、LocalStorage、窗口监听等常用组合式函数。      |
| `axios`                       | HTTP 请求库，用于封装统一的请求、响应拦截器，实现 Token 自动携带、统一异常处理等功能。                        |
| `crypto-js`                   | 前端加密工具库，用于登录密码 AES、MD5 等加密，配合 HTTPS 提高数据传输安全性。                           |
| `dayjs`                       | 轻量级日期处理库，用于时间格式化、日期计算、时区处理等。                                             |
| `echarts`                     | Apache ECharts 图表核心库，用于实现折线图、柱状图、饼图等数据可视化。                               |
| `vue-echarts`                 | ECharts 的 Vue3 组件封装，负责图表实例创建、更新及生命周期管理。                                  |
| `element-plus`                | Vue3 中后台 UI 组件库，提供表格、表单、弹窗、菜单、分页等常用组件。                                   |
| `md-editor-v3`                | Markdown 编辑器组件，支持实时预览、代码高亮、图片上传、数学公式等功能。                                 |
| `mitt`                        | 超轻量事件总线，用于跨组件事件通信，例如 WebSocket 消息通知、页面刷新通知等。                             |
| `nprogress`                   | 页面顶部进度条组件，用于路由切换、接口请求时展示加载进度。                                            |
| `pinia`                       | Vue 官方状态管理库，用于存储用户信息、权限菜单、系统配置等全局状态。                                     |
| `pinia-plugin-persistedstate` | Pinia 持久化插件，将状态自动保存至 LocalStorage 或 SessionStorage，避免页面刷新后数据丢失。          |
| `tailwindcss`                 | 原子化 CSS 框架，通过组合 Utility Class 快速构建页面样式。                                  |
| `vue`                         | Vue3 前端框架核心库，负责组件化开发及响应式数据管理。                                            |
| `vue-draggable-plus`          | 基于 SortableJS 的 Vue3 拖拽组件，实现列表、表格等拖拽排序功能。                                |
| `vue-i18n`                    | Vue 国际化插件，实现后台管理系统中英文等多语言切换。                                             |
| `vue-router`                  | Vue 官方路由管理器，实现页面导航、动态路由、权限拦截等功能。                                         |
| `vue3-count-to`               | 数字滚动动画组件，常用于仪表盘统计数据展示。                                                   |

---

# DevDependencies

| 依赖                                 | 作用说明                                                        |
|------------------------------------|-------------------------------------------------------------|
| `@eslint/js`                       | ESLint 官方 JavaScript 基础规则集，适用于 Flat Config 配置方式。            |
| `@types/crypto-js`                 | 为 `crypto-js` 提供 TypeScript 类型声明，提高代码提示与类型检查能力。             |
| `@types/node`                      | Node.js API 类型定义，为 `vite.config.ts` 等配置文件提供完整类型支持。          |
| `@types/nprogress`                 | 为 `nprogress` 提供 TypeScript 类型声明。                           |
| `@vitejs/plugin-vue`               | Vite 官方 Vue 插件，负责解析并编译 `.vue` 单文件组件。                        |
| `@vue/tsconfig`                    | Vue 官方 TypeScript 配置预设，提供推荐的 TS 编译配置。                       |
| `eslint`                           | JavaScript、TypeScript、Vue 代码静态检查工具，用于规范代码质量。                |
| `eslint-config-prettier`           | 关闭 ESLint 中与 Prettier 冲突的格式化规则，避免重复检查。                      |
| `eslint-plugin-vue`                | Vue 官方 ESLint 插件，支持检查 `.vue` 单文件组件代码规范。                     |
| `globals`                          | 提供浏览器、Node.js 等运行环境的全局变量声明，配合 ESLint Flat Config 使用。        |
| `prettier`                         | 代码格式化工具，统一代码风格，如缩进、引号、换行等。                                  |
| `sass`                             | Dart Sass 编译器，用于解析并编译 SCSS 样式文件。                            |
| `stylelint`                        | CSS、SCSS 样式代码静态检查工具，用于统一样式规范。                               |
| `stylelint-config-recommended-vue` | Stylelint Vue 配置，使 Stylelint 支持检查 `.vue` 文件中的 `<style>` 样式。 |
| `stylelint-config-standard-scss`   | Stylelint 官方 SCSS 标准规则预设，提供推荐的 SCSS 编码规范。                   |
| `typescript`                       | TypeScript 编译器，为项目提供静态类型检查与类型推导能力。                          |
| `typescript-eslint`                | TypeScript 与 ESLint 的桥梁，使 ESLint 能够解析并检查 TypeScript 代码。     |
| `unplugin-auto-import`             | 自动导入 Vue、Pinia、Vue Router 等常用 API，无需手动编写 `import`。          |
| `unplugin-vue-components`          | 自动按需导入并注册 Vue 组件（如 Element Plus），无需手动注册组件。                  |
| `vite`                             | 下一代前端开发服务器与构建工具，提供极速热更新与高性能构建能力。                            |
| `vite-plugin-compression`          | 构建时自动生成 Gzip/Brotli 压缩资源，减少生产环境静态资源传输体积。                    |
| `vue-tsc`                          | Vue 官方 TypeScript 类型检查工具，对 `.vue` 文件执行完整的类型校验。              |