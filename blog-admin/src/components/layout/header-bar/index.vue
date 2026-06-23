<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useFullscreen } from '@vueuse/core'
import { LanguageEnum } from '@/enums/appEnum.ts'
import { useMenuStore, useSettingStore, useUserStore } from '@/stores'
import { languageOptions } from '@/locales' // 语言
import { mittBus } from '@/utils/sys'
import { themeAnimation } from '@/utils/ui/animation'
import { useCommon, useHeaderBar } from '@/hooks'
import UserMenu from './widget/UserMenu.vue'

// 检测操作系统类型
  const isWindows = navigator.userAgent.includes('Windows')

  const { locale } = useI18n()

  const settingStore = useSettingStore()
  const userStore = useUserStore()
  const menuStore = useMenuStore()

  // 顶部栏功能配置
  const {
    shouldShowMenuButton,
    shouldShowRefreshButton,
    shouldShowBreadcrumb,
    shouldShowGlobalSearch,
    shouldShowFullscreen,
    shouldShowNotification,
    shouldShowLanguage,
    shouldShowSettings,
    shouldShowThemeToggle
  } = useHeaderBar()

  const { isDark } = storeToRefs(settingStore)
  const { language } = storeToRefs(userStore)
  const { menuOpen } = storeToRefs(menuStore)

  const showNotice = ref(false)

  const { isFullscreen, toggle: toggleFullscreen } = useFullscreen()

  onMounted(() => {
    initLanguage()
    document.addEventListener('click', bodyCloseNotice)
  })

  onUnmounted(() => {
    document.removeEventListener('click', bodyCloseNotice)
  })

  /**
   * 切换菜单显示/隐藏状态
   */
  const visibleMenu = (): void => {
    menuStore.setMenuOpen(!menuOpen.value)
  }

  const { refresh } = useCommon()
  /**
   * 刷新页面
   * @param {number} time - 延迟时间，默认为0毫秒
   */
  const reload = (time: number = 0): void => {
    setTimeout(() => {
      refresh()
    }, time)
  }

  /**
   * 初始化语言设置
   */
  const initLanguage = (): void => {
    locale.value = language.value
  }

  /**
   * 切换语言
   * @param {string} lang - 目标语言类型
   */
  const changeLanguage = (lang: LanguageEnum) => {
    if (locale.value === lang) return
    locale.value = lang
    userStore.setLanguage(lang)
    reload(50)
  }

  /**
   * 打开设置面板
   */
  const openSetting = (): void => {
    mittBus.emit('openSetting')
  }

  /**
   * 打开全局搜索对话框
   */
  const openSearchDialog = (): void => {
    mittBus.emit('openSearchDialog')
  }

  /**
   * 点击页面其他区域关闭通知面板
   * @param {Event} e - 点击事件对象
   */
  const bodyCloseNotice = (e: any): void => {
    if (!showNotice.value) return

    const target = e.target as HTMLElement

    // 检查是否点击了通知按钮或通知面板内部
    const isNoticeButton = target.closest('.notice-button')
    const isNoticePanel = target.closest('.notification-panel')

    if (!isNoticeButton && !isNoticePanel) {
      showNotice.value = false
    }
  }

  /**
   * 切换通知面板显示状态
   */
  const visibleNotice = (): void => {
    showNotice.value = !showNotice.value
  }
</script>

<template>
  <div class="header-container">
    <div class="relative box-border px-3! flex justify-between h-15 select-none">
      <div class="left-section">
        <!-- 菜单按钮 -->
        <IconButton
          v-if="shouldShowMenuButton"
          icon="ri:menu-2-fill"
          @click="visibleMenu"
        ></IconButton>

        <!--刷新按钮-->
        <IconButton
          v-if="shouldShowRefreshButton"
          icon="ri:refresh-line"
          class="refresh-btn max-sm:hidden!"
          @click="reload"
        ></IconButton>

        <!-- 面包屑 -->
        <Breadcrumb v-if="shouldShowBreadcrumb" />
      </div>

      <div class="user-actions">
        <!-- 搜索 -->
        <div
          v-if="shouldShowGlobalSearch"
          class="search-box w-40 h-9 rounded-custom-sm max-md:hidden!"
          @click="openSearchDialog"
        >
          <div class="search-content">
            <SvgIcon icon="ri:search-line" class="text-sm text-g-500" />
            <span class="text-xs font-normal text-g-500">{{ $t('topBar.search.title') }}</span>
          </div>
          <div class="shortcut-keys text-g-500/80">
            <SvgIcon v-if="isWindows" icon="vaadin:ctrl-a" class="text-sm" />
            <SvgIcon v-else icon="ri:command-fill" class="text-xs" />
            <span class="text-xs">k</span>
          </div>
        </div>

        <!--全屏按钮-->
        <IconButton
          v-if="shouldShowFullscreen"
          :icon="isFullscreen ? 'ri:fullscreen-exit-line' : 'ri:fullscreen-fill'"
          :class="[!isFullscreen ? 'full-screen-btn' : 'exit-full-screen-btn']"
          class="max-md:hidden!"
          @click="toggleFullscreen"
        ></IconButton>

        <!-- 国际化按钮 -->
        <el-dropdown
          @command="changeLanguage"
          popper-class="lang-dropDown-style"
          v-if="shouldShowLanguage"
        >
          <IconButton icon="ri:translate-2" class="language-btn text-[19px]" />
          <template #dropdown>
            <el-dropdown-menu>
              <div v-for="item in languageOptions" :key="item.value" class="lang-btn-item">
                <el-dropdown-item
                  :command="item.value"
                  :class="{ 'is-selected': locale === item.value }"
                >
                  <span class="menu-txt">{{ item.label }}</span>
                  <SvgIcon icon="ri:check-fill" v-if="locale === item.value" />
                </el-dropdown-item>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!--通知按钮-->
        <IconButton
          v-if="shouldShowNotification"
          icon="ri:notification-2-line"
          class="notice-button relative"
          @click="visibleNotice"
        >
          <div class="absolute top-2 right-2 size-1.5 bg-danger! rounded-full"></div>
        </IconButton>

        <!-- 设置按钮 -->
        <IconButton
          v-if="shouldShowSettings"
          icon="ri:settings-line"
          class="setting-btn"
          @click="openSetting"
        ></IconButton>

        <!--主题切换按钮-->
        <IconButton
          v-if="shouldShowThemeToggle"
          @click="themeAnimation"
          :icon="isDark ? 'ri:sun-fill' : 'ri:moon-line'"
        ></IconButton>

        <!-- 用户头像、菜单 -->
        <UserMenu />
      </div>
    </div>

    <!-- 标签页 -->
    <WorkTab />

    <!-- 通知 -->
    <Notification v-model:value="showNotice" />
  </div>
</template>

<style lang="scss" scoped>
  .header-container {
    width: 100%;
    height: inherit;
  }

  .left-section {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
    gap: 0.625rem;
  }

  .user-actions {
    display: flex;
    align-items: center;
    gap: 0.625rem;
  }

  .search-box {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-inline: 0.625rem;
    cursor: pointer;
    border: 1px solid var(--color-g-400);

    .search-content {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .shortcut-keys {
      display: flex;
      align-items: center;
      gap: 0.125rem;
      height: 20px;
      padding-inline: 0.375rem;
      border: inherit;
      border-radius: 0.25rem;
    }
  }

  /* Custom animations */
  @keyframes rotate180 {
    0% {
      transform: rotate(0);
    }

    100% {
      transform: rotate(180deg);
    }
  }

  @keyframes shake {
    0% {
      transform: rotate(0);
    }

    25% {
      transform: rotate(-5deg);
    }

    50% {
      transform: rotate(5deg);
    }

    75% {
      transform: rotate(-5deg);
    }

    100% {
      transform: rotate(0);
    }
  }

  @keyframes expand {
    0% {
      transform: scale(1);
    }

    50% {
      transform: scale(1.1);
    }

    100% {
      transform: scale(1);
    }
  }

  @keyframes shrink {
    0% {
      transform: scale(1);
    }

    50% {
      transform: scale(0.9);
    }

    100% {
      transform: scale(1);
    }
  }

  @keyframes moveUp {
    0% {
      transform: translateY(0);
    }

    50% {
      transform: translateY(-3px);
    }

    100% {
      transform: translateY(0);
    }
  }

  /* Hover animation classes */
  .refresh-btn:hover :deep(.svg-icon) {
    animation: rotate180 0.5s;
  }

  .language-btn:hover :deep(.svg-icon) {
    animation: moveUp 0.4s;
  }

  .setting-btn:hover :deep(.svg-icon) {
    animation: rotate180 0.5s;
  }

  .full-screen-btn:hover :deep(.svg-icon) {
    animation: expand 0.6s forwards;
  }

  .exit-full-screen-btn:hover :deep(.svg-icon) {
    animation: shrink 0.6s forwards;
  }

  .notice-button:hover :deep(.svg-icon) {
    animation: shake 0.5s ease-in-out;
  }
</style>
