import { defineStore } from 'pinia'
import { ContainerWidthEnum, SystemThemeEnum } from '@/enums/appEnum.ts'
import { setElementThemeColor } from '@/utils/ui'
import { SETTING_DEFAULT_CONFIG } from '@/config/setting.ts'

export const useSettingStore = defineStore(
  'settingStore',
  () => {
    // 主题相关设置
    /** 系统主题类型 */
    const systemThemeType = ref<SystemThemeEnum>(SETTING_DEFAULT_CONFIG.systemThemeType)
    /** 系统主题模式 */
    const systemThemeMode = ref<SystemThemeEnum>(SETTING_DEFAULT_CONFIG.systemThemeMode)
    /** 系统主题颜色 */
    const systemThemeColor = ref<string>(SETTING_DEFAULT_CONFIG.systemThemeColor)

    // 界面显示设置
    /** 是否显示菜单按钮 */
    const showMenuButton = ref(SETTING_DEFAULT_CONFIG.showMenuButton)
    /** 是否显示刷新按钮 */
    const showRefreshButton = ref(SETTING_DEFAULT_CONFIG.showRefreshButton)
    /** 是否显示面包屑 */
    const showCrumbs = ref(SETTING_DEFAULT_CONFIG.showCrumbs)
    /** 是否显示工作台标签 */
    const showWorkTab = ref(SETTING_DEFAULT_CONFIG.showWorkTab)
    /** 是否显示语言切换 */
    const showLanguage = ref(SETTING_DEFAULT_CONFIG.showLanguage)
    /** 是否显示进度条 */
    const showNprogress = ref(SETTING_DEFAULT_CONFIG.showNprogress)
    /** 是否显示设置引导 */
    const showSettingGuide = ref(SETTING_DEFAULT_CONFIG.showSettingGuide)

    // 功能设置
    /** 是否自动关闭 */
    const autoClose = ref(SETTING_DEFAULT_CONFIG.autoClose)
    /** 是否唯一展开 */
    const uniqueOpened = ref(SETTING_DEFAULT_CONFIG.uniqueOpened)
    /** 是否刷新 */
    const refresh = ref(SETTING_DEFAULT_CONFIG.refresh)

    // 样式设置
    /** 页面过渡效果 */
    const pageTransition = ref(SETTING_DEFAULT_CONFIG.pageTransition)
    /** 自定义圆角 */
    const customRadius = ref(SETTING_DEFAULT_CONFIG.customRadius)
    /** 容器宽度 */
    const containerWidth = ref<ContainerWidthEnum>(SETTING_DEFAULT_CONFIG.containerWidth)

    /**
     * 判断是否为暗色模式
     */
    const isDark = computed((): boolean => {
      return systemThemeType.value === SystemThemeEnum.DARK
    })

    /**
     * 获取自定义圆角
     */
    const getCustomRadius = computed((): string => {
      const radius = customRadius.value ?? SETTING_DEFAULT_CONFIG.customRadius
      return radius + 'rem'
    })

    /**
     * 设置全局主题
     * @param theme 主题类型
     * @param themeMode 主题模式
     */
    const setGlopTheme = (theme: SystemThemeEnum, themeMode: SystemThemeEnum) => {
      systemThemeType.value = theme
      systemThemeMode.value = themeMode
    }

    /**
     * 设置Element Plus主题颜色
     * @param theme 主题颜色
     */
    const setElementTheme = (theme: string) => {
      systemThemeColor.value = theme
      setElementThemeColor(theme)
    }

    /**
     * 设置容器宽度
     * @param width 容器宽度枚举值
     */
    const setContainerWidth = (width: ContainerWidthEnum) => {
      containerWidth.value = width
    }

    /**
     * 切换唯一展开模式
     */
    const setUniqueOpened = () => {
      uniqueOpened.value = !uniqueOpened.value
    }

    /**
     * 切换菜单按钮显示
     */
    const setButton = () => {
      showMenuButton.value = !showMenuButton.value
    }

    /**
     * 切换自动关闭
     */
    const setAutoClose = () => {
      autoClose.value = !autoClose.value
    }

    /**
     * 切换刷新按钮显示
     */
    const setShowRefreshButton = () => {
      showRefreshButton.value = !showRefreshButton.value
    }

    /**
     * 切换面包屑显示
     */
    const setCrumbs = () => {
      showCrumbs.value = !showCrumbs.value
    }

    /**
     * 设置工作台标签显示
     * @param show 是否显示
     */
    const setWorkTab = (show: boolean) => {
      showWorkTab.value = show
    }

    /**
     * 切换语言切换显示
     */
    const setLanguage = () => {
      showLanguage.value = !showLanguage.value
    }

    /**
     * 切换进度条显示
     */
    const setNprogress = () => {
      showNprogress.value = !showNprogress.value
    }
    /**
     * 隐藏设置引导
     */
    const hideSettingGuide = () => {
      showSettingGuide.value = false
    }

    /**
     * 显示设置引导
     */
    const openSettingGuide = () => {
      showSettingGuide.value = true
    }

    /**
     * 设置页面过渡效果
     * @param transition 过渡效果名称
     */
    const setPageTransition = (transition: string) => {
      pageTransition.value = transition
    }

    /**
     * 刷新页面
     */
    const reload = () => {
      refresh.value = !refresh.value
    }

    /**
     * 设置自定义圆角
     * @param radius 圆角值
     */
    const setCustomRadius = (radius: string) => {
      customRadius.value = radius
      document.documentElement.style.setProperty('--custom-radius', `${radius}rem`)
    }

    return {
      systemThemeType,
      systemThemeMode,
      systemThemeColor,
      uniqueOpened,
      showMenuButton,
      showRefreshButton,
      showCrumbs,
      autoClose,
      showWorkTab,
      showLanguage,
      showNprogress,
      showSettingGuide,
      pageTransition,
      refresh,
      customRadius,
      containerWidth,
      isDark,
      getCustomRadius,
      setGlopTheme,
      setElementTheme,
      setContainerWidth,
      setUniqueOpened,
      setButton,
      setAutoClose,
      setShowRefreshButton,
      setCrumbs,
      setWorkTab,
      setLanguage,
      setNprogress,
      hideSettingGuide,
      openSettingGuide,
      setPageTransition,
      reload,
      setCustomRadius
    }
  },
  {
    persist: {
      key: 'setting',
      storage: localStorage
    }
  }
)
