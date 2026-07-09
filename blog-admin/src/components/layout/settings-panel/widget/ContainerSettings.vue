<script setup lang="ts">
import SectionTitle from './SectionTitle.vue'
import { useSettingStore } from '@/stores'
import { useSettingsConfig } from '../composables/useSettingsConfig'
import { useSettingsHandlers } from '../composables/useSettingsHandlers'
import { storeToRefs } from 'pinia'

const settingStore = useSettingStore()
const { containerWidth } = storeToRefs(settingStore)
const { containerWidthOptions } = useSettingsConfig()
const { containerHandlers } = useSettingsHandlers()
</script>

<template>
  <div>
    <SectionTitle :title="$t('setting.container.title')" class="mt-12.5" />
    <div class="flex">
      <div
        v-for="option in containerWidthOptions"
        :key="option.value"
        class="container-item text-g-800!"
        :class="{
          'border-theme [&_i]:text-theme!': containerWidth === option.value,
          'border-full-d': containerWidth !== option.value,
        }"
        @click="containerHandlers.setWidth(option.value)"
      >
        <SvgIcon :icon="option.icon" class="mr-2! text-lg" />
        <span class="text-sm">{{ option.label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container-item {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  height: 4rem;
  margin: 1.25rem 0.875rem 0.875rem 0;
  cursor: pointer;
  border: 2px solid var(--default-border);
  border-radius: 8px;
}

.container-item:last-child {
  margin-right: 0;
}
</style>
