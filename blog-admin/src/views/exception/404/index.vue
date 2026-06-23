<script setup lang="ts">
  import imgUrl from '@/assets/images/svg/404.svg'
</script>
<template>
  <Exception
    :data="{
      title: '404',
      desc: $t('exceptionPage.404'),
      btnText: $t('exceptionPage.gohome'),
      imgUrl
    }"
  />
</template>

<style lang="scss" scoped></style>
