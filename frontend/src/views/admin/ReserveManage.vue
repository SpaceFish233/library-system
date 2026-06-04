<template>
  <div class="reserve-manage-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Bell /></el-icon>
          预约管理
        </h1>
        <p class="page-subtitle">查看和管理所有用户的预约记录</p>
      </div>
    </div>

    <!-- Stats cards -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon waiting">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.waiting }}</span>
          <span class="stat-label">等待中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon ready">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.ready }}</span>
          <span class="stat-label">待领取</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon fulfilled">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.fulfilled }}</span>
          <span class="stat-label">已领取</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cancelled">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.cancelled }}</span>
          <span class="stat-label">已取消</span>
        </div>
      </div>
    </div>

    <!-- Filter tabs -->
    <div class="filter-tabs">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="WAITING">等待中</el-radio-button>
        <el-radio-button label="READY">待领取</el-radio-button>
        <el-radio-button label="FULFILLED">已领取</el-radio-button>
        <el-radio-button label="CANCELLED">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Table -->
    <div class="table-card">
      <el-table
        :data="filteredRecords"
        stripe
        class="reserve-table"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="70" />

        <el-table-column prop="userId" label="用户ID" width="80" />

        <el-table-column label="书名" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="book-title-cell">{{ row.bookTitle || '图书 #' + row.bookId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="预约时间" width="180">
          <template #default="{ row }">
            <span class="time-cell">{{ formatTime(row.reserveTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="plain">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="等待时长" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'WAITING'" class="waiting-time">
              {{ getWaitingTime(row.reserveTime) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div v-if="reserves.length > 0" class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="reserves.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Bell,
  Timer,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { getAllReserves } from '../../api/reserve'

const loading = ref(false)
const reserves = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const stats = reactive({
  waiting: 0,
  ready: 0,
  fulfilled: 0,
  cancelled: 0
})

const filteredRecords = computed(() => {
  let list = reserves.value
  if (statusFilter.value) {
    list = list.filter(r => r.status === statusFilter.value)
  }
  // Paginate
  const start = (currentPage.value - 1) * pageSize.value
  return list.slice(start, start + pageSize.value)
})

function getStatusType(status) {
  const map = {
    WAITING: 'info',
    READY: 'warning',
    FULFILLED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    WAITING: '等待中',
    READY: '待领取',
    FULFILLED: '已领取',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

function getWaitingTime(reserveTime) {
  if (!reserveTime) return ''
  const now = new Date()
  const reserve = new Date(reserveTime)
  const diff = Math.floor((now - reserve) / (1000 * 60 * 60 * 24))
  if (diff === 0) return '今天'
  if (diff < 7) return diff + ' 天'
  if (diff < 30) return Math.floor(diff / 7) + ' 周'
  return Math.floor(diff / 30) + ' 月'
}

function handleFilterChange() {
  currentPage.value = 1
}

async function fetchReserves() {
  loading.value = true
  try {
    const res = await getAllReserves()
    reserves.value = res.data

    // Calculate stats
    stats.waiting = reserves.value.filter(r => r.status === 'WAITING').length
    stats.ready = reserves.value.filter(r => r.status === 'READY').length
    stats.fulfilled = reserves.value.filter(r => r.status === 'FULFILLED').length
    stats.cancelled = reserves.value.filter(r => r.status === 'CANCELLED').length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchReserves()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.reserve-manage-page {
  font-family: 'Inter', sans-serif;
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

/* Stats row */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(232, 168, 124, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-icon.waiting {
  background: linear-gradient(135deg, #909399, #a8a8a8);
}

.stat-icon.ready {
  background: linear-gradient(135deg, #e8a87c, #d4956b);
}

.stat-icon.fulfilled {
  background: linear-gradient(135deg, #67c23a, #5daf34);
}

.stat-icon.cancelled {
  background: linear-gradient(135deg, #f56c6c, #e64e4e);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  font-family: 'Noto Serif SC', serif;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

/* Filter tabs */
.filter-tabs {
  margin-bottom: 20px;
}

.filter-tabs :deep(.el-radio-button__inner) {
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  margin-right: 8px;
  font-weight: 500;
}

.filter-tabs :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-color: #1a1a2e;
}

/* Table card */
.table-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(232, 168, 124, 0.1);
}

.reserve-table {
  border-radius: 10px;
  overflow: hidden;
}

.reserve-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #1a1a2e;
  font-weight: 600;
}

.book-title-cell {
  font-weight: 500;
  color: #1a1a2e;
}

.time-cell {
  font-size: 13px;
  color: #606266;
}

.waiting-time {
  font-size: 13px;
  color: #e8a87c;
  font-weight: 500;
}

.text-muted {
  color: #c0c4cc;
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 0 0;
}

.pagination-section :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

/* Responsive */
@media (max-width: 768px) {
  .stats-row {
    flex-wrap: wrap;
  }

  .stat-card {
    min-width: calc(50% - 8px);
  }
}
</style>
