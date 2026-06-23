import App from './App.vue'
import { createApp } from 'vue'
import { initStore } from './stores' // store
import { initRouter } from './router' // router
import language from './locales' // 国际化
import '@/styles/tailwind.css' // tailwind
import '@/styles/index.scss' // 样式
import { setupGlobDirectives } from '@/directives' // 指令
import { setupErrorHandle } from '@/utils/sys/error-handle.ts' // 全局错误处理

document.addEventListener('touchstart', function () {}, { passive: false })

const app = createApp(App)
initStore(app)
initRouter(app)
setupGlobDirectives(app)
setupErrorHandle(app)

app.use(language)
app.mount('#app')
