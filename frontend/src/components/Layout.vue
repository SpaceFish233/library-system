<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="logo" @click="router.push('/')">
        <el-icon><Reading /></el-icon>
        <span>图书借阅管理系统</span>
      </div>
      <div class="nav-menu">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/books">图书列表</el-menu-item>
          <el-menu-item index="/my-borrows">我的借阅</el-menu-item>
          <el-menu-item index="/my-reserves">我的预约</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/books">图书管理</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/categories">分类管理</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/borrows">借阅管理</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/reserves">预约管理</el-menu-item>
        </el-menu>
      </div>
      <div class="user-info">
        <el-badge :value="notificationCount" :hidden="notificationCount === 0" class="notification-btn" @click="router.push('/notifications')">
          <el-icon :size="20" class="notification-icon"><Bell /></el-icon>
        </el-badge>
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { getUnreadCount } from '../api/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationCount = ref(0)

const activeMenu = computed(() => route.path)

// 获取未读通知数量
async function fetchNotifications() {
  try {
    const res = await getUnreadCount()
    notificationCount.value = res.data.count
  } catch (e) {
    // 忽略错误
  }
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'notifications') {
    router.push('/my-reserves')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.logo .el-icon {
  font-size: 24px;
}

.nav-menu {
  flex: 1;
  margin: 0 20px;
}

.nav-menu .el-menu {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}

.notification-badge {
  margin-left: 8px;
}

.notification-btn {
  margin-right: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.notification-icon {
  color: #606266;
  transition: color 0.3s;
}

.notification-icon:hover {
  color: #409eff;
}
</style>
