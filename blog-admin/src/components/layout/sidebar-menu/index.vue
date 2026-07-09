<script setup lang="ts">
import AppConfig from '@/config'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore, useSettingStore } from '@/stores'
import SidebarSubmenu from './widget/SidebarSubmenu.vue'
import { useCommon } from '@/hooks'
import { useTimeoutFn, useWindowSize } from '@vueuse/core'

const MOBILE_BREAKPOINT = 800

  const router = useRouter()
  const settingStore = useSettingStore()
  const menuStore = useMenuStore()

  const { menuOpen, menuList } = storeToRefs(menuStore)
  const { uniqueOpened } = storeToRefs(settingStore)
  const { width } = useWindowSize() // 使用 VueUse 的窗口尺寸监听

  // 组件内部状态
  const isMobileMode = computed(() => width.value < MOBILE_BREAKPOINT) // 是否为移动端模式
  const showMobileModal = ref(false) // 移动端遮罩层显示状态
  const current = computed(() => router.currentRoute.value.path)

  /**
   * 延迟隐藏移动端模态框（使用 VueUse 的 useTimeoutFn）
   */
  const { start: delayHideMobileModal } = useTimeoutFn(
    () => {
      showMobileModal.value = false
    },
    350,
    { immediate: false }
  )

  const { homePath } = useCommon()

  /**
   * 导航到首页
   */
  const navigateToHome = (): void => {
    router.push(homePath.value)
    // 移动端点击logo后关闭菜单
    if (isMobileMode.value) {
      menuStore.setMenuOpen(false)
    }
  }

  /**
   * 切换菜单显示/隐藏
   */
  const toggleMenuVisibility = (): void => {
    menuStore.setMenuOpen(!menuOpen.value)

    // 移动端模态框控制逻辑
    if (isMobileMode.value) {
      if (!menuOpen.value) {
        // 菜单即将打开，立即显示模态框
        showMobileModal.value = true
      } else {
        // 菜单即将关闭，延迟隐藏模态框确保动画完成
        delayHideMobileModal()
      }
    }
  }

  /**
   * 处理菜单关闭（来自子组件）
   */
  const handleMenuClose = (): void => {
    if (isMobileMode.value) {
      menuStore.setMenuOpen(false)
      delayHideMobileModal()
    }
  }

  /**
   * 监听窗口尺寸变化，自动处理移动端菜单
   */
  watch(width, (newWidth) => {
    if (newWidth < MOBILE_BREAKPOINT) {
      menuStore.setMenuOpen(false)
      if (!menuOpen.value) {
        showMobileModal.value = false
      }
    } else {
      showMobileModal.value = false
    }
  })

  /**
   * 监听菜单开关状态变化
   */
  watch(menuOpen, (isMenuOpen: boolean) => {
    if (!isMobileMode.value) {
      // 大屏幕设备上，模态框始终隐藏
      showMobileModal.value = false
    } else {
      // 小屏幕设备上，根据菜单状态控制模态框
      if (isMenuOpen) {
        // 菜单打开时立即显示模态框
        showMobileModal.value = true
      } else {
        // 菜单关闭时延迟隐藏模态框，确保动画完成
        delayHideMobileModal()
      }
    }
  })
</script>

<template>
  <el-scrollbar class="sidebar-scrollbar" :class="`menu-left-${!menuOpen ? 'close' : 'open'}`">
    <div class="logo-container" @click="navigateToHome">
      <SvgIcon icon="mdi-abjad-hebrew" class="logo-icon" />
      <p v-show="menuOpen" class="logo-title">{{ AppConfig.systemInfo.name }}</p>
    </div>

    <el-menu
      class="sidebar-menu"
      :collapse="!menuOpen"
      :default-active="current"
      router
      :unique-opened="uniqueOpened"
      :show-timeout="50"
      :hide-timeout="50"
      :collapse-transition="false"
    >
      <SidebarSubmenu :list="menuList" @close="handleMenuClose"></SidebarSubmenu>
    </el-menu>

    <Teleport to="body">
      <div
        class="menu-model"
        @click="toggleMenuVisibility"
        :style="{ opacity: !menuOpen ? 0 : 1 }"
        v-show="showMobileModal"
      />
    </Teleport>
  </el-scrollbar>
</template>

<style lang="scss" scoped>
  .sidebar-scrollbar {
    width: 100%;
    height: 100%;
    background-color: inherit;
  }

  .logo-container {
    display: flex;
    column-gap: 0.675rem;
    align-items: center;
    justify-content: flex-start;
    width: 100%;
    height: 60px;
    padding-left: 1.375rem;
    overflow: hidden;
    cursor: pointer;

    .logo-icon {
      flex-shrink: 0; // 防止 logo 缩小
      font-size: 2rem;
      color: var(--theme-color);
    }

    .logo-title {
      font-size: 1.2rem;
      white-space: nowrap;
    }
  }

  .sidebar-menu {
    width: 100%;
    height: calc(100vh - 60px);
    padding: 0 0.375rem;
    background-color: inherit;
    border-right: none;

    // 一级菜单项
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 45px;
      margin-bottom: 8px;
      border-radius: 8px;

      &:hover {
        background-color: var(--hover-color);
      }

      &.is-active {
        color: var(--theme-color);
        background-color: var(--active-color);
        box-shadow: inset 3px 0 0 var(--theme-color);
      }
    }

    // 二级菜单项
    :deep(.el-sub-menu) .el-menu {
      background-color: inherit;
    }
  }

  .menu-model {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 299;
    width: 100vw;
    height: 100vh;
    background: rgba($color: #000, $alpha: 50%);
    transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  }
</style>
