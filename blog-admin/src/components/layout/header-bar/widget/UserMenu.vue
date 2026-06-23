<script setup lang="ts">
import avatar from '@/assets/avatar.jpg'
import { ArrowRight, Crop, EditPen, SwitchButton, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logOut()
    return
  }
  ElMessage.info(`click on item ${command}`)
}
</script>

<template>
  <el-dropdown placement="bottom-end" @command="handleCommand" trigger="click">
    <div class="user-profile">
      <el-avatar :size="36" :src="avatar" />
      <span class="user-name">Admin</span>
      <el-icon class="el-icon--right">
        <ArrowRight />
      </el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="profile" :icon="User">基本资料</el-dropdown-item>
        <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
        <el-dropdown-item command="password" :icon="EditPen">重置密码</el-dropdown-item>
        <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped lang="scss">
.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--el-fill-color-light);

  &:hover {
    background: var(--el-fill-color);
  }

  .user-name {
    font-weight: 500;
  }
}
</style>
