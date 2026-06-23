<script setup lang="ts">
type FitMode = 'none' | 'cover' | 'fill' | 'contain' | 'scale-down'

type Props = {
  src: string
  lazy?: boolean // 限制 lazy 属性只能是 true 或 false
  fit?: FitMode // 限制 fit 属性只能是 FitMode 类型中的一个值
  animate?: boolean
}

withDefaults(defineProps<Props>(), {
  lazy: true, // 设置 lazy 属性的默认值
  fit: 'cover', // 设置 fit 属性的默认值
  animate: false, // 设置 animate 属性的默认值
})
</script>

<template>
  <el-image :class="['blog-image', { active: animate }]" :src="src" :lazy="lazy" :fit="fit" v-bind="$attrs">
    <template #placeholder>
      <div class="w-full h-full grid place-items-center">
        <SvgIcon icon="svg-spinners-3-dots-scale" class="w-8 h-8" />
      </div>
    </template>
    <template #error>
      <div class="w-full h-full grid place-items-center">
        <SvgIcon icon="mdi:error-outline" class="w-8 h-8" />
      </div>
    </template>
  </el-image>
</template>

<style lang="scss" scoped>
.blog-image {
  width: 100%;
  height: 100%;
  user-select: none;
  overflow: hidden;
}

.active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.active:hover {
  transform: scale(1.1);
}
</style>
