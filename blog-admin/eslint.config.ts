// 从 URL 和路径模块中导入必要的功能
import fs from 'fs'
import path, { dirname } from 'path'
import { fileURLToPath } from 'url'

import js from '@eslint/js'
import globals from 'globals'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import eslintConfigPrettier from 'eslint-config-prettier'

// 使用 import.meta.url 获取当前模块的路径
const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

// 读取 .eslintrc-auto-import.json 文件的内容，并将其解析为 JSON 对象
const autoImportConfig = JSON.parse(
  fs.readFileSync(path.resolve(__dirname, '.eslintrc-auto-import.json'), 'utf-8')
)

export default defineConfigWithVueTs([
  {
    ignores: ['node_modules/**', 'dist/**', 'public/**', '.vscode/**', '.idea/**', '*.local']
  },
  js.configs.recommended,
  {
    files: ['**/*.{js,mjs,cjs,ts,mts,cts,vue}'],
    languageOptions: { globals: { ...globals.browser, ...globals.node } }
  },
  tseslint.configs.recommended,
  pluginVue.configs['flat/recommended'],
  vueTsConfigs.recommended,
  { files: ['**/*.vue'], languageOptions: { parserOptions: { parser: tseslint.parser } } },

  // 自定义规则
  {
    // 针对所有 JavaScript、TypeScript 和 Vue 文件应用以下配置
    files: ['**/*.{js,mjs,cjs,ts,mts,cts,vue}'],
    languageOptions: {
      globals: {
        // 合并从 autoImportConfig 中读取的全局变量配置
        ...autoImportConfig.globals,
        // TypeScript 全局命名空间
        Api: 'readonly'
      }
    },
    rules: {
      'no-var': 'error', // 要求使用 let 或 const 而不是 var
      '@typescript-eslint/no-explicit-any': 'off', // 禁用 any 检查
      'vue/multi-word-component-names': 'off' // 禁用对 Vue 组件名称的多词要求检查
    }
  },

  // prettier 配置
  eslintConfigPrettier
])
