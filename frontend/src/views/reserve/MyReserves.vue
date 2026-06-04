<template>
  <div class="my-reserves-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Bell /></el-icon>
          我的预约
        </h1>
        <p class="page-subtitle">管理您的图书预约</p>
      </div>
    </div>

    <!-- Notification banner -->
    <div v-if="readyReserves.length > 0" class="notification-banner">
      <div class="banner-content">
        <el-icon class="banner-icon"><Bell /></el-icon>
        <div class="banner-text">
          <h4>您有图书可领取！</h4>
          <p>{{ readyReserves.map(r => r.bookTitle || '图书').join('、') }} 已可借阅，请尽快领取</p>
        </div>
      </div>
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
    <div v-else-if="reserves.length === 0" class="empty-state">
      <el-icon class="empty-icon"><FolderOpened /></el-icon>
      <h3>暂无预约记录</h3>
      <p>当图书库存不足时，您可以预约排队</p>
      <el-button type="primary" class="go-browse-btn" @click="router.push('/books')">
        浏览图书
      </el-button>
    </div>

    <!-- Reserves list -->
    <div v-else class="reserves-list">
      <div
        v-for="(reserve, index) in reserves"
        :key="reserve.id"
        class="reserve-card"
        :class="[`status-${reserve.status.toLowerCase()}`, { 'card-enter': isVisible }]"
        :style="{ '--delay': index * 0.08 + 's' }"
      >
        <div class="card-left">
          <div class="status-icon" :class="getStatusClass(reserve.status)">
            <el-icon :size="24">
              <component :is="getStatusIcon(reserve.status)" />
            </el-icon>
          </div>
          <div class="card-info">
            <h3 class="book-title">{{ reserve.bookTitle || '图书 #' + reserve.bookId }}</h3>
            <div class="card-meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                预约时间: {{ formatTime(reserve.reserveTime) }}
              </span>
            </div>
          </div>
        </div>

        <div class="card-right">
          <div class="status-badge" :class="getStatusClass(reserve.status)">
            <template v-if="reserve.status === 'READY'">
              库存：{{ reserve.stock ?? '-' }}
            </template>
            <template v-else>
              {{ getStatusText(reserve.status) }}
            </template>
          </div>

          <div class="card-actions">
            <el-button
              v-if="reserve.status === 'READY'"
              type="primary"
              size="small"
              class="claim-btn"
              :loading="actionLoading"
              @click="handleBorrow(reserve)"
            >
              <el-icon><Check /></el-icon>
              借阅
            </el-button>

            <el-button
              v-if="reserve.status === 'WAITING'"
              size="small"
              class="cancel-btn"
              :loading="actionLoading"
              @click="handleCancel(reserve)"
            >
              取消预约
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Borrow duration dialog -->
    <el-dialog v-model="borrowDialogVisible" title="借阅设置" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="借阅时长">
          <el-input-number v-model="borrowForm.duration" :min="1" :max="30" style="width: 160px" />
        </el-form-item>
        <el-form-item label="时长单位">
          <el-select v-model="borrowForm.unit" style="width: 160px">
            <el-option label="秒" value="second" />
            <el-option label="天" value="day" />
            <el-option label="周" value="week" />
            <el-option label="月" value="month" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBorrow">确认借阅</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Clock,
  Check,
  FolderOpened,
  Timer,
  CircleCheck,
  WarningFilled
} from '@element-plus/icons-vue'
import { getMyReserves, claimReserve, cancelReserve } from '../../api/reserve'

const router = useRouter()
const isVisible = ref(false)
const loading = ref(false)
const actionLoading = ref(false)
const reserves = ref([])
const readyReserves = ref([])
const borrowDialogVisible = ref(false)
const borrowForm = reactive({ duration: 30, unit: 'day' })
const currentReserve = ref(null)

function getStatusClass(status) {
  const map = {
    WAITING: 'status-waiting',
    READY: 'status-ready',
    CANCELLED: 'status-cancelled',
    FULFILLED: 'status-fulfilled'
  }
  return map[status] || ''
}

function getStatusIcon(status) {
  const map = {
    WAITING: 'Timer',
    READY: 'Bell',
    CANCELLED: 'WarningFilled',
    FULFILLED: 'CircleCheck'
  }
  return map[status] || 'Timer'
}

function getStatusText(status) {
  const map = {
    WAITING: '等待中',
    READY: '待领取',
    CANCELLED: '已取消',
    FULFILLED: '已领取'
  }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function fetchReserves() {
  loading.value = true
  try {
    const res = await getMyReserves()
    reserves.value = res.data
    readyReserves.value = res.data.filter(r => r.status === 'READY')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleBorrow(reserve) {
  currentReserve.value = reserve
  borrowForm.duration = 30
  borrowForm.unit = 'day'
  borrowDialogVisible.value = true
}

async function confirmBorrow() {
  if (!borrowForm.duration || borrowForm.duration <= 0) {
    ElMessage.warning('请输入有效的借阅时长')
    return
  }

  borrowDialogVisible.value = false
  actionLoading.value = true
  try {
    await claimReserve(currentReserve.value.id, borrowForm.duration, borrowForm.unit)
    ElMessage.success('借阅成功！')
    fetchReserves()
  } catch (e) {
    // Error handled by interceptor
  } finally {
    actionLoading.value = false
  }
}

async function handleCancel(reserve) {
  try {
    await ElMessageBox.confirm(
      '确定要取消此预约吗？',
      '取消预约',
      { confirmButtonText: '确认取消', cancelButtonText: '保留', type: 'warning' }
    )

    actionLoading.value = true
    await cancelReserve(reserve.id)
    ElMessage.success('预约已取消')
    fetchReserves()
  } catch (e) {
    if (e !== 'cancel') {
      // Error handled by interceptor
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  fetchReserves()
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.my-reserves-page {
  font-family: 'Inter', sans-serif;
  max-width: 1000px;
  margin: 0 auto;
}

/* Page header */
.page-header {
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

/* Notification banner */
.notification-banner {
  background: linear-gradient(135deg, rgba(232, 168, 124, 0.15), rgba(212, 149, 107, 0.1));
  border: 1px solid rgba(232, 168, 124, 0.3);
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 24px;
  animation: banner-pulse 2s ease-in-out infinite;
}

@keyframes banner-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(232, 168, 124, 0.2); }
  50% { box-shadow: 0 0 0 8px rgba(232, 168, 124, 0); }
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-icon {
  font-size: 32px;
  color: #e8a87c;
  animation: bell-ring 1s ease-in-out infinite;
}

@keyframes bell-ring {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(10deg); }
  75% { transform: rotate(-10deg); }
}

.banner-text h4 {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.banner-text p {
  font-size: 13px;
  color: #606266;
  margin: 0;
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
  margin: 0 0 24px 0;
}

.go-browse-btn {
  border-radius: 10px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
}

/* Reserves list */
.reserves-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reserve-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.4s ease;
  animation: card-slide 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  animation-delay: var(--delay);
}

@keyframes card-slide {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.reserve-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.reserve-card.status-ready {
  border-left: 4px solid #e8a87c;
  background: linear-gradient(135deg, rgba(232, 168, 124, 0.05), #fff);
}

.card-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.status-icon.status-waiting {
  background: linear-gradient(135deg, #909399, #a8a8a8);
}

.status-icon.status-ready {
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  animation: ready-pulse 2s ease-in-out infinite;
}

@keyframes ready-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.status-icon.status-cancelled {
  background: linear-gradient(135deg, #c0c4cc, #b0b4bc);
}

.status-icon.status-fulfilled {
  background: linear-gradient(135deg, #67c23a, #5daf34);
}

.book-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.status-waiting {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.status-badge.status-ready {
  background: rgba(232, 168, 124, 0.15);
  color: #e8a87c;
  font-weight: 600;
}

.status-badge.status-cancelled {
  background: rgba(192, 196, 204, 0.2);
  color: #909399;
}

.status-badge.status-fulfilled {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.claim-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  border: none;
}

.cancel-btn {
  border-radius: 8px;
  border-color: #e8e8e8;
  color: #909399;
}

.cancel-btn:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

/* Responsive */
@media (max-width: 768px) {
  .reserve-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .card-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
