<script setup lang="ts">
import { getEnabledGlobalComponents } from '@/config/modules/component'

defineOptions({ name: 'GlobalComponent' })
const enabledComponents = computed(() => getEnabledGlobalComponents())
</script>

<template>
  <!-- 全局组件 -->
  <component v-for="componentConfig in enabledComponents" :key="componentConfig.key" :is="componentConfig.component" />
</template>
