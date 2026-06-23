<script setup lang="ts">
import { computed } from 'vue'
import type { AppRouteRecord } from '@/types'
import { formatMenuTitle } from '@/utils'

interface Props {
  /** 菜单标题 */
  title?: string
  /** 菜单列表 */
  list?: AppRouteRecord[]
}

interface Emits {
  /** 关闭菜单事件 */
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  list: () => [],
})

const emit = defineEmits<Emits>()

/**
 * 过滤后的菜单项列表
 * 只显示未隐藏的菜单项
 */
const filteredMenuItems = computed(() => filterRoutes(props.list))

/**
 * 关闭菜单
 * 触发父组件的关闭事件
 */
const closeMenu = (): void => {
  emit('close')
}

/**
 * 递归过滤菜单路由，移除隐藏的菜单项
 * 如果一个父菜单的所有子菜单都被隐藏，则父菜单也会被隐藏
 * @param items 菜单项数组
 * @returns 过滤后的菜单项数组
 */
const filterRoutes = (items: AppRouteRecord[]): AppRouteRecord[] => {
  return items
    .filter((item) => {
      // 如果当前项被隐藏，直接过滤掉
      if (item.meta.isHide) {
        return false
      }

      // 如果有子菜单，递归过滤子菜单
      if (item.children && item.children.length > 0) {
        const filteredChildren = filterRoutes(item.children)
        // 如果所有子菜单都被过滤掉了，则隐藏父菜单
        return filteredChildren.length > 0
      }

      // 叶子节点且未被隐藏，保留
      return true
    })
    .map((item) => ({
      ...item,
      children: item.children ? filterRoutes(item.children) : undefined,
    }))
}

/**
 * 判断菜单项是否包含可见的子菜单
 * @param item 菜单项数据
 * @returns 是否包含可见的子菜单
 */
const hasChildren = (item: AppRouteRecord): boolean => {
  if (!item.children || item.children.length === 0) {
    return false
  }
  // 递归检查是否有可见的子菜单
  const filteredChildren = filterRoutes(item.children)
  return filteredChildren.length > 0
}
</script>

<template>
  <template v-for="menu in filteredMenuItems" :key="menu.path">
    <el-sub-menu v-if="hasChildren(menu)" :index="menu.path">
      <template #title>
        <el-icon><SvgIcon :icon="menu.meta.icon" /></el-icon>
        <span>{{ formatMenuTitle(menu.meta.title) }}</span>
      </template>
      <el-menu-item v-for="submenu in menu.children" :key="submenu.path" :index="submenu.path" @click="closeMenu">
        <el-icon><SvgIcon :icon="submenu.meta.icon" /></el-icon>
        <span>{{ formatMenuTitle(submenu.meta.title) }}</span>
      </el-menu-item>
    </el-sub-menu>

    <el-menu-item v-else :index="menu.path" @click="closeMenu">
      <el-icon><SvgIcon :icon="menu.meta.icon" /></el-icon>
      <span>{{ formatMenuTitle(menu.meta.title) }}</span>
    </el-menu-item>
  </template>
</template>

<style scoped lang="scss"></style>
