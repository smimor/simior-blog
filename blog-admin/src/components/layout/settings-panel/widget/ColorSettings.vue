<script setup lang="ts">
import SectionTitle from './SectionTitle.vue'
import { useSettingStore } from '@/stores'
import { useSettingsConfig } from '../composables/useSettingsConfig'
import { useSettingsHandlers } from '../composables/useSettingsHandlers'
import { storeToRefs } from 'pinia'

const settingStore = useSettingStore()
const { systemThemeColor } = storeToRefs(settingStore)
const { configOptions } = useSettingsConfig()
const { colorHandlers } = useSettingsHandlers()
</script>

<template>
  <div>
    <SectionTitle :title="$t('setting.color.title')" class="mt-10" />
    <div class="systemColor">
      <div
        v-for="color in configOptions.mainColors"
        :key="color"
        class="color-item size-5.75 rounded-full"
        :style="{ background: `${color} !important` }"
        @click="colorHandlers.selectColor(color)"
      >
        <SvgIcon icon="ri:check-fill" class="text-base text-white!" v-show="color === systemThemeColor" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.systemColor {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.color-item {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease-in-out;

  &:hover {
    opacity: 85%;
  }
}
</style>
