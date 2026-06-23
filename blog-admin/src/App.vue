<script setup lang="ts">
import { useUserStore } from '@/stores'
import zh from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import { initializeTheme } from '@/hooks/core/useTheme.ts'
import { storeToRefs } from 'pinia'
import { onBeforeMount } from 'vue'

const userStore = useUserStore()
  const { language } = storeToRefs(userStore)

  const locales = {
    zh: zh,
    en: en
  }
  onBeforeMount(() => {
    initializeTheme()
  })
</script>

<template>
  <el-config-provider
    :locale="locales[language]"
    :z-index="3000"
    :card="{
      shadow: 'never'
    }"
  >
    <router-view />
  </el-config-provider>
</template>
