<template>
  <div class="my-borrows-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Timer /></el-icon>
          我的借阅
        </h1>
        <p class="page-subtitle">管理您的借阅记录</p>
      </div>
    </div>

    <!-- Status tabs -->
    <div class="status-tabs" :class="{ 'tabs-enter': isVisible }">
      <div
        v-for="tab in statusTabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentStatus === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
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
    <div v-else-if="records.length === 0" class="empty-state">
      <el-icon class="empty-icon"><FolderOpened /></el-icon>
      <h3>暂无借阅记录</h3>
      <p>快去图书馆探索好书吧</p>
      <el-button type="primary" class="go-browse-btn" @click="router.push('/books')">
        浏览图书
      </el-button>
    </div>

    <!-- Records list -->
    <div v-else class="records-list">
      <div
        v-for="(record, index) in records"
        :key="record.id"
        class="record-card"
        :class="[`status-${record.status.toLowerCase()}`, { 'card-enter': isVisible }]"
        :style="{ '--delay': index * 0.08 + 's' }"
      >
        <div class="record-left">
          <div class="book-icon" :class="getStatusClass(record.status)">
            <el-icon :size="24"><Reading /></el-icon>
          </div>
          <div class="record-info">
            <h3 class="book-title">{{ record.bookTitle || '图书 #' + record.bookId }}</h3>
            <div class="record-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                借阅: {{ record.borrowDate }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                应还: {{ record.dueDate }}
              </span>
              <span v-if="record.returnDate" class="meta-item">
                <el-icon><CircleCheck /></el-icon>
                归还: {{ record.returnDate }}
              </span>
            </div>
          </div>
        </div>

        <div class="record-right">
          <!-- Status badge -->
          <div class="status-badge" :class="getStatusClass(record.status)">
            {{ getStatusText(record.status) }}
          </div>

          <!-- Overdue info -->
          <div v-if="record.overdueDays > 0" class="overdue-info">
            <span class="overdue-days">逾期 {{ record.overdueDays }} 天</span>
            <span v-if="record.fineAmount > 0" class="overdue-fine">
              罚金 ¥{{ record.fineAmount }}
            </span>
          </div>

          <!-- Action button -->
          <el-button
            v-if="record.status === 'BORROWED' || record.status === 'OVERDUE'"
            type="primary"
            size="small"
            class="return-btn"
            :class="{ 'overdue-btn': record.status === 'OVERDUE' }"
            @click="handleReturn(record)"
          >
            <el-icon><RefreshRight /></el-icon>
            申请归还
          </el-button>

          <!-- Pending status -->
          <div v-if="record.status === 'PENDING_RETURN'" class="pending-hint">
            <el-icon><Clock /></el-icon>
            等待管理员确认
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="total > 0" class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="fetchRecords"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Timer,
  Reading,
  Calendar,
  Clock,
  CircleCheck,
  RefreshRight,
  WarningFilled,
  FolderOpened,
  Check,
  Filter
} from '@element-plus/icons-vue'
import { getMyBorrows, applyReturn } from '../../api/borrow'

const router = useRouter()
const isVisible = ref(false)
const loading = ref(false)
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentStatus = ref('')


const statusTabs = computed(() => [
  { label: '全部', value: '', icon: 'Filter', count: 0 },
  { label: '借阅中', value: 'BORROWED', icon: 'Reading', count: records.value.filter(r => r.status === 'BORROWED').length },
  { label: '待确认', value: 'PENDING_RETURN', icon: 'Timer', count: records.value.filter(r => r.status === 'PENDING_RETURN').length },
  { label: '已归还', value: 'RETURNED', icon: 'Check', count: 0 },
  { label: '逾期', value: 'OVERDUE', icon: 'WarningFilled', count: records.value.filter(r => r.status === 'OVERDUE').length }
])

function getStatusClass(status) {
  const map = {
    BORROWED: 'status-borrowed',
    PENDING_RETURN: 'status-pending',
    RETURNED: 'status-returned',
    OVERDUE: 'status-overdue'
  }
  return map[status] || ''
}

function getStatusText(status) {
  const map = {
    BORROWED: '借阅中',
    PENDING_RETURN: '待确认归还',
    RETURNED: '已归还',
    OVERDUE: '已逾期'
  }
  return map[status] || status
}

async function fetchRecords() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (currentStatus.value) {
      params.status = currentStatus.value
    }
    const res = await getMyBorrows(params)
    records.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleTabChange(status) {
  currentStatus.value = status
  currentPage.value = 1
  fetchRecords()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchRecords()
}

function handleReturn(record) {
  ElMessageBox.confirm(
    '确定要申请归还这本图书吗？管理员确认后将完成还书。',
    '申请归还',
    { confirmButtonText: '确认申请', cancelButtonText: '取消', type: 'info' }
  ).then(async () => {
    try {
      await applyReturn(record.id)
      ElMessage.success('归还申请已提交，等待管理员确认')
      fetchRecords()
    } catch (e) {
      // Error handled by interceptor
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchRecords()
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.my-borrows-page {
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

/* Status tabs */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.5s ease;
}

.status-tabs.tabs-enter {
  opacity: 1;
  transform: translateY(0);
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #fff;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.tab-item:hover {
  border-color: #e8a87c;
  color: #e8a87c;
}

.tab-item.active {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #fff;
  border-color: transparent;
}

.tab-count {
  background: rgba(232, 168, 124, 0.2);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.tab-item.active .tab-count {
  background: rgba(232, 168, 124, 0.4);
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

/* Records list */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
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
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.record-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.record-card.status-overdue {
  border-left: 4px solid #f56c6c;
}

.record-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.book-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.book-icon.status-borrowed {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

.book-icon.status-returned {
  background: linear-gradient(135deg, #67c23a, #5daf34);
}

.book-icon.status-overdue {
  background: linear-gradient(135deg, #f56c6c, #e64e4e);
}

.book-icon.status-pending {
  background: linear-gradient(135deg, #e6a23c, #d4956b);
}

.record-info {
  min-width: 0;
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

.record-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.record-right {
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

.status-badge.status-borrowed {
  background: rgba(26, 26, 46, 0.1);
  color: #1a1a2e;
}

.status-badge.status-returned {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-badge.status-overdue {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.status-badge.status-pending {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.pending-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #e6a23c;
}

.overdue-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.overdue-days {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.overdue-fine {
  font-size: 12px;
  color: #e6a23c;
}

.return-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
}

.return-btn.overdue-btn {
  background: linear-gradient(135deg, #e8a87c, #d4956b);
}

/* Overdue dialog */
.overdue-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
}

.overdue-content {
  text-align: center;
  padding: 20px 0;
}

.overdue-icon {
  color: #e6a23c;
  margin-bottom: 16px;
}

.overdue-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  color: #1a1a2e;
  margin: 0 0 24px 0;
}

.overdue-details {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.detail-row:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.detail-row.total {
  border-top: 2px solid #e8a87c;
  margin-top: 8px;
  padding-top: 12px;
}

.detail-label {
  font-size: 14px;
  color: #606266;
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a2e;
}

.detail-value.highlight {
  color: #f56c6c;
  font-weight: 600;
}

.detail-value.price {
  color: #e8a87c;
  font-size: 18px;
  font-weight: 700;
}

.overdue-note {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.confirm-pay-btn {
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  border: none;
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.pagination-section :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

/* Responsive */
@media (max-width: 768px) {
  .status-tabs {
    flex-wrap: wrap;
  }

  .record-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .record-right {
    width: 100%;
    justify-content: flex-end;
  }

  .record-meta {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
