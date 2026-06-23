<script setup lang="ts">
import { useMenuStore } from '@/stores'
import { useWindowSize } from '@vueuse/core'
import { computed } from 'vue'
import { useAutoLayoutHeight } from '@/hooks'

const menuStore = useMenuStore()

const { menuWidth, menuOpen } = storeToRefs(menuStore)
const { width } = useWindowSize()
const { containerMinHeight } = useAutoLayoutHeight()

// 移动端断点
const MOBILE_BREAKPOINT = 800

// 移动端屏幕判断
const isMobile = computed(() => width.value <= MOBILE_BREAKPOINT)
</script>

<template>
  <el-container class="app-layout">
    <el-aside
      id="app-sidebar"
      :width="menuWidth"
      :class="{ 'mobile-sidebar': isMobile, 'sidebar-open': isMobile && menuOpen }"
    >
      <SidebarMenu />
    </el-aside>
    <el-container id="app-main">
      <el-header id="app-header">
        <HeaderBar />
      </el-header>
      <el-scrollbar class="main-scrollbar">
        <el-main>
          <PageContent />
        </el-main>
      </el-scrollbar>
    </el-container>

    <div id="app-global">
      <GlobalComponent />
    </div>
  </el-container>
</template>
<style lang="scss" scoped>
.app-layout {
  width: 100%;
  min-height: 100vh;
  background-color: var(--default-bg-color);
}

// 侧栏样式
#app-sidebar {
  background-color: var(--default-box-color);
  border-right: 1px solid var(--card-border);
  height: 100vh;
  user-select: none;
  transition:
    width 0.3s ease,
    transform 0.3s ease; // 添加过渡效果
}

// 头部样式
.el-header {
  padding: 0;
  height: auto;
}

.main-scrollbar {
  width: 100%;
  height: v-bind(containerMinHeight);
}

// 主体样式
.el-main {
  :deep(.page-content) {
    position: relative;
    box-sizing: border-box;
    overflow: hidden;
    padding: 20px;
    background: var(--default-box-color);
    border-radius: calc(var(--custom-radius) / 2 + 2px);
    border: 1px solid var(--card-border);
  }
}

@media only screen and (width <= 1180px) {
  #app-main {
    height: 100dvh;
  }
}

@media only screen and (width <= 640px) {
  .el-main {
    padding: 15px;
  }
}

.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 300;
  height: 100vh;
  transform: translateX(-100%);

  &.sidebar-open {
    transform: translateX(0);
  }
}
</style>
