<template>
  <div class="notifications-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Bell /></el-icon>
          通知中心
        </h1>
        <p class="page-subtitle">查看系统消息和预约通知</p>
      </div>
      <el-button
        v-if="notifications.length > 0"
        class="read-all-btn"
        @click="handleMarkAllRead"
      >
        全部已读
      </el-button>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner">
        <div class="spinner-ring" />
        <div class="spinner-ring" />
        <div class="spinner-ring" />
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="notifications.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Bell /></el-icon>
      <h3>暂无通知</h3>
      <p>当有新的消息时，会在这里显示</p>
    </div>

    <!-- Notification list -->
    <div v-else class="notification-list">
      <div
        v-for="(item, index) in notifications"
        :key="item.id"
        class="notification-card"
        :class="{ unread: item.isRead === 0, 'card-enter': isVisible }"
        :style="{ '--delay': index * 0.05 + 's' }"
        @click="handleClick(item)"
      >
        <div class="card-icon" :class="{ 'icon-unread': item.isRead === 0 }">
          <el-icon :size="20"><Bell /></el-icon>
        </div>
        <div class="card-content">
          <p class="notification-text">{{ item.content }}</p>
          <span class="notification-time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div v-if="item.isRead === 0" class="unread-dot" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getNotifications, markAsRead, markAllAsRead } from '../../api/notification'

const router = useRouter()
const isVisible = ref(false)
const loading = ref(false)
const notifications = ref([])

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await getNotifications()
    notifications.value = res.data.list
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleClick(item) {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
    } catch (e) {
      // ignore
    }
  }
  // 如果是预约相关通知，跳转到我的预约
  if (item.type === 'RESERVE_READY') {
    router.push('/my-reserves')
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  fetchNotifications()
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.notifications-page {
  font-family: 'Inter', sans-serif;
  max-width: 800px;
  margin: 0 auto;
}

/* Page header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  color: #e8a87c;
  font-size: 32px;
}

.page-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.read-all-btn {
  border-radius: 10px;
  border-color: #e8e8e8;
  color: #606266;
  transition: all 0.3s ease;
}

.read-all-btn:hover {
  border-color: #e8a87c;
  color: #e8a87c;
}

/* Loading state */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-radius: 50%;
  animation: spin 1.5s linear infinite;
}

.spinner-ring:nth-child(1) { border-top-color: #1a1a2e; }
.spinner-ring:nth-child(2) { inset: 6px; border-right-color: #e8a87c; animation-direction: reverse; }
.spinner-ring:nth-child(3) { inset: 12px; border-bottom-color: #16213e; animation-delay: 0.3s; }

@keyframes spin { to { transform: rotate(360deg); } }

/* Empty state */
.empty-state {
  text-align: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  color: #606266;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

/* Notification list */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
  animation: card-slide 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  animation-delay: var(--delay);
  position: relative;
}

@keyframes card-slide {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.notification-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.notification-card.unread {
  background: linear-gradient(135deg, rgba(232, 168, 124, 0.05), #fff);
  border-left: 3px solid #e8a87c;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #909399;
  flex-shrink: 0;
}

.card-icon.icon-unread {
  background: rgba(232, 168, 124, 0.15);
  color: #e8a87c;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.notification-text {
  font-size: 14px;
  color: #303133;
  margin: 0 0 4px 0;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
