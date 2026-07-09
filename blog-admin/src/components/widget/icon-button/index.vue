<script setup lang="ts">
  interface Props {
    /** 图标名称 */
    icon: string
    /** 圆角按钮 */
    circle?: boolean
  }

  withDefaults(defineProps<Props>(), {})
</script>

<template>
  <div class="icon-button size-8.5 text-g-600 dark:text-g-800" :class="{ 'rounded-full': circle }">
    <SvgIcon :icon="icon" />
    <slot></slot>
  </div>
</template>

<style scoped lang="scss">
  .icon-button {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.25rem;
    cursor: pointer;
    border-radius: 0.25rem;
    transition: all 0.3s ease;

    &:hover {
      background-color: var(--hover-color);
    }
  }
</style>
