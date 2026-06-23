<script setup lang="ts">
  import imgUrl from '@/assets/images/svg/500.svg'
</script>
<template>
  <Exception
    :data="{
      title: '500',
      desc: $t('exceptionPage.500'),
      btnText: $t('exceptionPage.gohome'),
      imgUrl
    }"
  />
</template>

<style lang="scss" scoped></style>
