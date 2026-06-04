<template>
  <div class="borrow-manage-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Document /></el-icon>
          借阅管理
        </h1>
        <p class="page-subtitle">查看和管理所有用户的借阅记录</p>
      </div>
    </div>

    <!-- Search bar -->
    <div class="search-card">
      <div class="search-fields">
        <el-input
          v-model="searchForm.username"
          placeholder="搜索用户名..."
          clearable
          class="search-input"
          :prefix-icon="User"
        />
        <el-input
          v-model="searchForm.bookName"
          placeholder="搜索书名..."
          clearable
          class="search-input"
          :prefix-icon="Reading"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="状态筛选"
          clearable
          class="search-select"
        >
          <el-option label="借阅中" value="BORROWED" />
          <el-option label="待确认归还" value="PENDING_RETURN" />
          <el-option label="已归还" value="RETURNED" />
          <el-option label="已逾期" value="OVERDUE" />
        </el-select>
        <el-button type="primary" class="search-btn" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button class="reset-btn" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- Stats cards -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon borrowed">
          <el-icon><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.borrowed }}</span>
          <span class="stat-label">借阅中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待确认</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon returned">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.returned }}</span>
          <span class="stat-label">已归还</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.overdue }}</span>
          <span class="stat-label">已逾期</span>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card">
      <el-table
        :data="records"
        stripe
        class="borrow-table"
        v-loading="loading"
      >
        <el-table-column prop="userId" label="用户ID" width="80" />

        <el-table-column label="书名" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="book-title-cell">{{ row.bookTitle || '图书 #' + row.bookId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="借阅日期" width="120">
          <template #default="{ row }">
            <span class="date-cell">{{ row.borrowDate }}</span>
          </template>
        </el-table-column>

        <el-table-column label="应还日期" width="120">
          <template #default="{ row }">
            <span class="date-cell" :class="{ 'overdue-date': row.status === 'OVERDUE' }">
              {{ row.dueDate }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="归还日期" width="120">
          <template #default="{ row }">
            <span class="date-cell">{{ row.returnDate || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="plain">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="逾期天数" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'overdue-days': row.overdueDays > 0 }">
              {{ row.overdueDays || 0 }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="罚金" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'fine-amount': row.fineAmount > 0 }">
              {{ row.fineAmount > 0 ? '¥' + row.fineAmount : '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING_RETURN'"
              type="primary"
              size="small"
              class="confirm-btn"
              @click="handleConfirmReturn(row)"
            >
              确认归还
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  User,
  Reading,
  Search,
  CircleCheck,
  WarningFilled,
  Clock
} from '@element-plus/icons-vue'
import { getAllBorrows, confirmReturn } from '../../api/borrow'

const loading = ref(false)
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const stats = reactive({
  borrowed: 0,
  pending: 0,
  returned: 0,
  overdue: 0
})

const searchForm = reactive({
  username: '',
  bookName: '',
  status: ''
})

function getStatusType(status) {
  const map = {
    BORROWED: '',
    PENDING_RETURN: 'warning',
    RETURNED: 'success',
    OVERDUE: 'danger'
  }
  return map[status] || 'info'
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
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.bookName) params.bookName = searchForm.bookName
    if (searchForm.status) params.status = searchForm.status

    const res = await getAllBorrows(params)
    records.value = res.data.records
    total.value = res.data.total

    // Calculate stats
    stats.borrowed = records.value.filter(r => r.status === 'BORROWED').length
    stats.pending = records.value.filter(r => r.status === 'PENDING_RETURN').length
    stats.returned = records.value.filter(r => r.status === 'RETURNED').length
    stats.overdue = records.value.filter(r => r.status === 'OVERDUE').length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchRecords()
}

function handleReset() {
  searchForm.username = ''
  searchForm.bookName = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchRecords()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchRecords()
}

async function handleConfirmReturn(record) {
  try {
    await ElMessageBox.confirm(
      `确认用户已归还《${record.bookTitle || '图书 #' + record.bookId}》？此操作不可撤销。`,
      '确认归还',
      { confirmButtonText: '确认归还', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await confirmReturn(record.id)
    if (res.data && res.data.overdueDays > 0) {
      ElMessage.success(`确认归还成功！逾期 ${res.data.overdueDays} 天，罚金 ¥${res.data.fine}，信用分 -${res.data.creditDeducted}`)
    } else {
      ElMessage.success('确认归还成功！')
    }
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel') {
      // Error handled by interceptor
    }
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.borrow-manage-page {
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

/* Search card */
.search-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(232, 168, 124, 0.1);
}

.search-fields {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 160px;
}

.search-select {
  width: 140px;
}

.search-input :deep(.el-input__wrapper),
.search-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover),
.search-select :deep(.el-input__wrapper:hover) {
  border-color: #e8a87c;
}

.search-btn {
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.reset-btn {
  height: 40px;
  border-radius: 10px;
}

/* Stats row */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
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

.stat-icon.borrowed {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

.stat-icon.returned {
  background: linear-gradient(135deg, #67c23a, #5daf34);
}

.stat-icon.overdue {
  background: linear-gradient(135deg, #f56c6c, #e64e4e);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #e6a23c, #d4956b);
}

.confirm-btn {
  border-radius: 6px;
  background: linear-gradient(135deg, #67c23a, #5daf34);
  border: none;
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

/* Table card */
.table-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(232, 168, 124, 0.1);
}

.borrow-table {
  border-radius: 10px;
  overflow: hidden;
}

.borrow-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #1a1a2e;
  font-weight: 600;
}

.book-title-cell {
  font-weight: 500;
  color: #1a1a2e;
}

.date-cell {
  font-size: 13px;
  color: #606266;
}

.date-cell.overdue-date {
  color: #f56c6c;
  font-weight: 500;
}

.overdue-days {
  color: #f56c6c;
  font-weight: 600;
}

.fine-amount {
  color: #e6a23c;
  font-weight: 600;
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
    flex-direction: column;
  }

  .search-fields {
    flex-direction: column;
  }

  .search-input,
  .search-select {
    width: 100%;
    max-width: 100%;
  }
}
</style>
