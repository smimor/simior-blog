<script setup lang="ts">
  import imgUrl from '@/assets/images/svg/403.svg'
</script>
<template>
  <Exception
    :data="{
      title: '403',
      desc: $t('exceptionPage.403'),
      btnText: $t('exceptionPage.gohome'),
      imgUrl
    }"
  />
</template>

<style lang="scss" scoped></style>
