<script setup lang="ts">
import { useSettingsPanel } from './composables/useSettingsPanel'

import SettingDrawer from './widget/SettingDrawer.vue'
import SettingHeader from './widget/SettingHeader.vue'
import ThemeSettings from './widget/ThemeSettings.vue'
import ColorSettings from './widget/ColorSettings.vue'
import ContainerSettings from './widget/ContainerSettings.vue'
import BasicSettings from './widget/BasicSettings.vue'
import SettingActions from './widget/SettingActions.vue'

interface Props {
  /** 是否打开 */
  open?: boolean
}
const props = defineProps<Props>()

// 使用设置面板逻辑
const settingsPanel = useSettingsPanel()
const { showDrawer } = settingsPanel

// 获取各种处理器
const { closeDrawer } = settingsPanel.useDrawerControl()
const { initializeSettings, cleanupSettings } = settingsPanel.useSettingsInitializer()

// 监听 props 变化
settingsPanel.usePropsWatcher(props)

onMounted(() => {
  initializeSettings()
})

onUnmounted(() => {
  cleanupSettings()
})
</script>

<template>
  <SettingDrawer v-model="showDrawer">
    <!-- 头部关闭按钮 -->
    <SettingHeader @close="closeDrawer" />
    <!-- 主题风格 -->
    <ThemeSettings />
    <!-- 系统主题色 -->
    <ColorSettings />
    <!-- 容器宽度 -->
    <ContainerSettings />
    <!-- 基础配置 -->
    <BasicSettings />
    <!-- 操作按钮 -->
    <SettingActions />
  </SettingDrawer>
</template>

<style lang="scss">
// 设置抽屉模态框样式
.setting-modal {
  background: transparent !important;

  .el-drawer {
    // 背景滤镜效果
    background: rgba($color: #fff, $alpha: 50%) !important;
    box-shadow: 0 0 30px rgb(0 0 0 / 10%) !important;
    backdrop-filter: blur(30px);

    .setting-box-wrap {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      width: calc(100% + 15px);
      margin-bottom: 10px;

      .setting-item {
        box-sizing: border-box;
        width: calc(33.333% - 15px);
        margin-right: 15px;
        text-align: center;

        .box {
          position: relative;
          box-sizing: border-box;
          display: flex;
          height: 52px;
          overflow: hidden;
          cursor: pointer;
          border: 2px solid var(--default-border);
          border-radius: 8px;
          box-shadow: 0 0 8px 0 rgb(0 0 0 / 10%);
          transition: box-shadow 0.1s;

          &.mt-16 {
            margin-top: 16px;
          }

          &.is-active {
            border: 2px solid var(--el-color-primary);
          }

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .name {
          margin-top: 6px;
          font-size: 14px;
          text-align: center;
        }
      }
    }
  }

  // 去除滚动条
  .el-drawer__body::-webkit-scrollbar {
    width: 0 !important;
  }
}

.dark {
  .setting-modal {
    .el-drawer {
      background: rgba($color: #000, $alpha: 50%) !important;

      .setting-item {
        .box {
          border: 2px solid transparent;
        }
      }
    }
  }
}

// 去除火狐浏览器滚动条
:deep(.el-drawer__body) {
  scrollbar-width: none;
}
</style>
