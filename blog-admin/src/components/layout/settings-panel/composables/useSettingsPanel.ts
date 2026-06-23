import { ref, watch } from 'vue'
import { mittBus } from '@/utils/sys'
import { useMenuStore } from '@/stores'

/**
 * 设置面板核心逻辑管理
 */
export function useSettingsPanel() {
  const menuStore = useMenuStore()
  // 响应式状态
  const showDrawer = ref(false)

  // 使用 VueUse breakpoints 优化性能
  const breakpoints = useBreakpoints({ tablet: 1000 })
  const isMobile = breakpoints.smaller('tablet')

  // 响应式布局处理
  const useResponsiveLayout = () => {
    // 使用 watch 监听断点变化，性能更优
    const stopWatch = watch(
      isMobile,
      (mobile: boolean) => {
        if (mobile) {
          // 切换到移动端布局
          menuStore.setMenuOpen(false)
        } else {
          // 恢复桌面端布局
          menuStore.setMenuOpen(true)
        }
      },
      { immediate: true },
    )

    return { stopWatch }
  }

  // 抽屉控制
  const useDrawerControl = () => {
    // 打开设置
    const openSetting = () => {
      showDrawer.value = true
    }

    // 关闭设置
    const closeDrawer = () => {
      showDrawer.value = false
    }

    return {
      openSetting,
      closeDrawer,
    }
  }

  // Props 变化监听
  const usePropsWatcher = (props: { open?: boolean }) => {
    watch(
      () => props.open,
      (val: boolean | undefined) => {
        if (val !== undefined) {
          showDrawer.value = val
        }
      },
    )
  }

  // 初始化设置
  const useSettingsInitializer = () => {
    const { openSetting } = useDrawerControl()
    const { stopWatch } = useResponsiveLayout()

    const initializeSettings = () => {
      mittBus.on('openSetting', openSetting)
    }

    const cleanupSettings = () => {
      stopWatch()
    }

    return {
      initializeSettings,
      cleanupSettings,
    }
  }

  return {
    // 状态
    showDrawer,

    // 方法组合
    useResponsiveLayout,
    useDrawerControl,
    usePropsWatcher,
    useSettingsInitializer,
  }
}
