<script setup lang="ts">
interface Props {
  title: string
  style?: Record<string, any>
}

defineProps<Props>()
</script>

<template>
  <p class="sectionTitle" :style="style">
    {{ title }}
  </p>
</template>

<style scoped lang="scss">
.sectionTitle {
  position: relative;
  margin-top: 1.875rem;
  margin-bottom: 1.375rem;
  font-size: 0.875rem;
  color: var(--color-g-800);
  text-align: center;

  &::before,
  &::after {
    position: absolute;
    top: 10px;
    width: 50px;
    margin: auto;
    content: '';
    border: 1px solid var(--gray-300);
  }

  &::before {
    left: 0;
  }

  &::after {
    right: 0;
  }
}
</style>
