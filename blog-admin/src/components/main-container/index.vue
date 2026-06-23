<script setup lang="ts">
defineProps({
  title: {
    type: String,
    default: '',
  },
})
</script>
<template>
  <el-card class="main-card">
    <template #header>
      <div class="header-content">
        <div class="font-bold">{{ title }}</div>
        <div class="extra">
          <slot name="extra"></slot>
        </div>
      </div>
    </template>

    <slot></slot>

    <template v-if="$slots.footer" #footer>
      <slot name="footer"></slot>
    </template>
  </el-card>
</template>

<style lang="scss" scoped>
.main-card {
  display: flex;
  flex-direction: column;
  height: 100%;

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
