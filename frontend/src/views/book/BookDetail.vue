<template>
  <div class="book-detail-page">
    <!-- Back button -->
    <div class="back-section">
      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
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

    <!-- Book detail -->
    <div v-else-if="book" class="detail-container" :class="{ 'detail-enter': isVisible }">
      <div class="detail-card">
        <!-- Left: Cover image -->
        <div class="cover-section">
          <div class="cover-wrapper">
            <img
              v-if="book.coverImage"
              :src="'/uploads/' + book.coverImage"
              :alt="book.title"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">
              <el-icon :size="80"><Reading /></el-icon>
              <span>暂无封面</span>
            </div>
          </div>

          <!-- Stock status -->
          <div class="stock-status" :class="book.stock > 0 ? 'in-stock' : 'out-stock'">
            <el-icon v-if="book.stock > 0"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            <span>{{ book.stock > 0 ? '可借阅' : '已借完' }}</span>
          </div>
        </div>

        <!-- Right: Book info -->
        <div class="info-section">
          <div class="book-header">
            <h1 class="book-title">{{ book.title }}</h1>
            <div class="book-meta">
              <span v-if="book.isbn" class="meta-item">
                <el-icon><Document /></el-icon>
                ISBN: {{ book.isbn }}
              </span>
            </div>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">
                <el-icon><User /></el-icon>
                作者
              </span>
              <span class="info-value">{{ book.author || '未知' }}</span>
            </div>

            <div class="info-item">
              <span class="info-label">
                <el-icon><OfficeBuilding /></el-icon>
                出版社
              </span>
              <span class="info-value">{{ book.publisher || '未知' }}</span>
            </div>

            <div class="info-item">
              <span class="info-label">
                <el-icon><Folder /></el-icon>
                分类
              </span>
              <span class="info-value">{{ categoryName || '未分类' }}</span>
            </div>

            <div class="info-item">
              <span class="info-label">
                <el-icon><Box /></el-icon>
                库存
              </span>
              <span class="info-value" :class="{ 'stock-zero': book.stock === 0 }">
                {{ book.stock }} 本
              </span>
            </div>
          </div>

          <!-- Description -->
          <div v-if="book.description" class="description-section">
            <h3 class="section-title">
              <el-icon><Memo /></el-icon>
              内容简介
            </h3>
            <p class="description-text">{{ book.description }}</p>
          </div>

          <!-- Reservation info -->
          <div v-if="waitingCount > 0" class="reserve-info">
            <el-icon><InfoFilled /></el-icon>
            <span>当前有 <strong>{{ waitingCount }}</strong> 人预约排队</span>
          </div>

          <!-- Action buttons -->
          <div class="action-section">
            <el-button
              v-if="book.stock > 0"
              type="primary"
              size="large"
              class="borrow-btn"
              :loading="actionLoading"
              @click="handleBorrow"
            >
              <el-icon><Reading /></el-icon>
              立即借阅
            </el-button>

            <el-button
              v-else
              type="warning"
              size="large"
              class="reserve-btn"
              :loading="actionLoading"
              @click="handleReserve"
            >
              <el-icon><Bell /></el-icon>
              预约排队
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Borrow dialog -->
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Reading,
  User,
  OfficeBuilding,
  Folder,
  Box,
  Memo,
  Document,
  CircleCheck,
  CircleClose,
  Bell,
  InfoFilled
} from '@element-plus/icons-vue'
import { getBookDetail } from '../../api/book'
import { getFirstLevelCategories } from '../../api/category'
import { borrowBook } from '../../api/borrow'
import { reserveBook, getMyReserves } from '../../api/reserve'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const actionLoading = ref(false)
const isVisible = ref(false)
const book = ref(null)
const categoryName = ref('')
const waitingCount = ref(0)
const borrowDialogVisible = ref(false)
const borrowForm = reactive({ duration: 30, unit: 'day' })

async function fetchBookDetail() {
  loading.value = true
  try {
    const res = await getBookDetail(route.params.id)
    book.value = res.data

    // Fetch category name
    if (res.data.categoryId) {
      const catRes = await getFirstLevelCategories()
      const cat = catRes.data.find(c => c.id === res.data.categoryId)
      if (cat) categoryName.value = cat.name
    }

    // Fetch waiting count from my reserves (simplified)
    try {
      const reservesRes = await getMyReserves()
      const waiting = reservesRes.data.filter(
        r => r.bookId === res.data.id && r.status === 'WAITING'
      )
      waitingCount.value = waiting.length
    } catch (e) {
      // Ignore
    }
  } catch (e) {
    ElMessage.error('获取图书详情失败')
  } finally {
    loading.value = false
    setTimeout(() => {
      isVisible.value = true
    }, 100)
  }
}

async function handleBorrow() {
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
    await borrowBook(book.value.id, borrowForm.duration, borrowForm.unit)
    ElMessage.success('借阅成功！')
    fetchBookDetail()
  } catch (e) {
    // Error handled by interceptor
  } finally {
    actionLoading.value = false
  }
}

async function handleReserve() {
  try {
    await ElMessageBox.confirm(
      `确定要预约《${book.value.title}》吗？有库存时会通知您。`,
      '确认预约',
      { confirmButtonText: '确认预约', cancelButtonText: '取消', type: 'info' }
    )

    actionLoading.value = true
    await reserveBook(book.value.id)
    ElMessage.success('预约成功！有库存时会通知您')
    fetchBookDetail() // Refresh
  } catch (e) {
    if (e !== 'cancel') {
      // Error handled by interceptor
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  fetchBookDetail()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.book-detail-page {
  min-height: calc(100vh - 80px);
  font-family: 'Inter', sans-serif;
  max-width: 1200px;
  margin: 0 auto;
}

/* Back button */
.back-section {
  margin-bottom: 24px;
}

.back-btn {
  border-radius: 10px;
  border-color: #e8e8e8;
  color: #606266;
  transition: all 0.3s ease;
}

.back-btn:hover {
  border-color: #e8a87c;
  color: #e8a87c;
  transform: translateX(-4px);
}

/* Loading state */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 100px 0;
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

.spinner-ring:nth-child(1) {
  border-top-color: #1a1a2e;
}

.spinner-ring:nth-child(2) {
  inset: 6px;
  border-right-color: #e8a87c;
  animation-direction: reverse;
}

.spinner-ring:nth-child(3) {
  inset: 12px;
  border-bottom-color: #16213e;
  animation-delay: 0.3s;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Detail container */
.detail-container {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.detail-container.detail-enter {
  opacity: 1;
  transform: translateY(0);
}

.detail-card {
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(232, 168, 124, 0.1);
  display: flex;
  min-height: 500px;
}

/* Cover section */
.cover-section {
  width: 400px;
  padding: 32px;
  background: linear-gradient(135deg, #f8f9fa, #f0f2f5);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.cover-wrapper {
  width: 100%;
  height: 360px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e8e8e8, #dcdfe6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  gap: 12px;
}

.cover-placeholder span {
  font-size: 14px;
}

.stock-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.stock-status.in-stock {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.stock-status.out-stock {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

/* Info section */
.info-section {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
}

.book-header {
  margin-bottom: 32px;
}

.book-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.book-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8c8c8c;
}

/* Info grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 4px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.info-value {
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 500;
}

.info-value.stock-zero {
  color: #f56c6c;
}

/* Description */
.description-section {
  margin-bottom: 24px;
  flex: 1;
}

.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title .el-icon {
  color: #e8a87c;
}

.description-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 3px solid #e8a87c;
}

/* Reserve info */
.reserve-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(230, 162, 60, 0.1);
  border-radius: 10px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #e6a23c;
}

.reserve-info strong {
  color: #d4956b;
}

/* Action section */
.action-section {
  margin-top: auto;
}

.borrow-btn,
.reserve-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.borrow-btn {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
}

.borrow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(26, 26, 46, 0.3);
}

.reserve-btn {
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  border: none;
}

.reserve-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(232, 168, 124, 0.4);
}

/* Responsive */
@media (max-width: 900px) {
  .detail-card {
    flex-direction: column;
  }

  .cover-section {
    width: 100%;
    padding: 24px;
  }

  .cover-wrapper {
    height: 280px;
  }

  .info-section {
    padding: 24px;
  }

  .book-title {
    font-size: 24px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
