<script setup lang="ts">
import { useTheme } from '@/hooks'
import { SystemThemeEnum } from '@/enums/appEnum.ts'

const { switchThemeStyles } = useTheme()
  const { colorMode } = useTheme()

  const clearLocalCache = (): void => {
    localStorage.clear()
  }
</script>
<template>
  <div class="dashboard-console">
    <h2>{{ $t('menus.dashboard.console') }}</h2>
    <p>当前主题: {{ colorMode }}</p>

    <!--清除本地缓存-->
    <el-button type="primary" @click="clearLocalCache"> 清除缓存 </el-button>
    <!--切换主题-->
    <el-button type="primary" @click="switchThemeStyles(SystemThemeEnum.DARK)"> 暗色 </el-button>
    <el-button type="success" @click="switchThemeStyles(SystemThemeEnum.LIGHT)"> 亮色 </el-button>
    <el-button type="primary" @click="switchThemeStyles(SystemThemeEnum.AUTO)"> 自动 </el-button>
  </div>
</template>

<style lang="scss" scoped>
  .dashboard-console {
    padding: 20px;
  }
</style>
