<template>
  <div class="book-list-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Collection /></el-icon>
          探索书海
        </h1>
        <p class="page-subtitle">发现您下一本书的旅程从这里开始</p>
      </div>
    </div>

    <!-- Search bar -->
    <div class="search-section" :class="{ 'search-enter': isVisible }">
      <div class="search-card">
        <div class="search-fields">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="选择分类"
            clearable
            class="search-select"
            @change="handleCategoryChange"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>

          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索书名..."
            clearable
            class="search-input"
            :prefix-icon="Search"
          />

          <el-input
            v-model="searchForm.author"
            placeholder="作者"
            clearable
            class="search-input small"
          />

          <el-input
            v-model="searchForm.publisher"
            placeholder="出版社"
            clearable
            class="search-input small"
          />

          <el-button
            type="primary"
            class="search-btn"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>

          <el-button
            class="reset-btn"
            @click="handleReset"
          >
            重置
          </el-button>
        </div>
      </div>
    </div>

    <!-- Book grid -->
    <div class="book-grid-section">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner-ring" />
          <div class="spinner-ring" />
          <div class="spinner-ring" />
        </div>
        <p>正在加载图书...</p>
      </div>

      <div v-else-if="books.length === 0" class="empty-state">
        <el-icon class="empty-icon"><FolderOpened /></el-icon>
        <h3>暂无图书</h3>
        <p>没有找到匹配的图书，请尝试其他搜索条件</p>
      </div>

      <div v-else class="book-grid">
        <div
          v-for="(book, index) in books"
          :key="book.id"
          class="book-card"
          :style="{ '--delay': index * 0.05 + 's' }"
          @click="goToDetail(book.id)"
        >
          <div class="card-cover">
            <img
              v-if="book.coverImage"
              :src="'/uploads/' + book.coverImage"
              :alt="book.title"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">
              <el-icon :size="48"><Reading /></el-icon>
              <span>暂无封面</span>
            </div>
            <div class="card-overlay">
              <span class="view-detail">查看详情</span>
            </div>
            <div class="stock-badge" :class="book.stock > 0 ? 'in-stock' : 'out-stock'">
              {{ book.stock > 0 ? '可借' : '已借完' }}
            </div>
          </div>

          <div class="card-info">
            <h3 class="book-title">{{ book.title }}</h3>
            <p class="book-author">
              <el-icon><User /></el-icon>
              {{ book.author || '未知作者' }}
            </p>
            <div class="card-footer">
              <span class="book-publisher">
                <el-icon><OfficeBuilding /></el-icon>
                {{ book.publisher || '未知出版社' }}
              </span>
              <span class="book-stock" :class="{ 'stock-zero': book.stock === 0 }">
                库存: {{ book.stock }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="total > 0" class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[8, 12, 16, 24]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="fetchBooks"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Collection,
  Search,
  Reading,
  User,
  OfficeBuilding,
  FolderOpened
} from '@element-plus/icons-vue'
import { searchBooks } from '../../api/book'
import { getFirstLevelCategories } from '../../api/category'

const router = useRouter()
const isVisible = ref(false)
const loading = ref(false)
const books = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  author: '',
  publisher: '',
  categoryId: null
})

async function fetchCategories() {
  try {
    const res = await getFirstLevelCategories()
    categories.value = res.data
  } catch (e) {
    console.error('Failed to fetch categories:', e)
  }
}

async function fetchBooks() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.author) params.author = searchForm.author
    if (searchForm.publisher) params.publisher = searchForm.publisher
    if (searchForm.categoryId) params.categoryId = searchForm.categoryId

    const res = await searchBooks(params)
    books.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error('Failed to fetch books:', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchBooks()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.author = ''
  searchForm.publisher = ''
  searchForm.categoryId = null
  currentPage.value = 1
  fetchBooks()
}

function handleCategoryChange() {
  handleSearch()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchBooks()
}

function goToDetail(id) {
  router.push(`/books/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchBooks()
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.book-list-page {
  min-height: calc(100vh - 80px);
  font-family: 'Inter', sans-serif;
}

/* Page header */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #e8a87c;
  font-size: 36px;
}

.page-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

/* Search section */
.search-section {
  margin-bottom: 32px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.search-section.search-enter {
  opacity: 1;
  transform: translateY(0);
}

.search-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(232, 168, 124, 0.1);
  max-width: 1400px;
  margin: 0 auto;
}

.search-fields {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.search-select {
  width: 160px;
}

.search-input {
  flex: 1;
  min-width: 180px;
}

.search-input.small {
  max-width: 160px;
}

.search-select :deep(.el-input__wrapper),
.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.search-select :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper:hover) {
  border-color: #e8a87c;
  box-shadow: 0 4px 12px rgba(232, 168, 124, 0.15);
}

.search-btn {
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(26, 26, 46, 0.3);
}

.reset-btn {
  height: 40px;
  border-radius: 10px;
  border-color: #e8e8e8;
  color: #606266;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  border-color: #e8a87c;
  color: #e8a87c;
}

/* Loading state */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
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
  animation-delay: 0s;
}

.spinner-ring:nth-child(2) {
  inset: 6px;
  border-right-color: #e8a87c;
  animation-delay: 0.15s;
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

.loading-state p {
  color: #8c8c8c;
  font-size: 14px;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 80px 0;
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
}

/* Book grid */
.book-grid-section {
  max-width: 1400px;
  margin: 0 auto;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

/* Book card */
.book-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  animation: card-enter 0.6s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  animation-delay: var(--delay);
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
}

.card-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.book-card:hover .cover-img {
  transform: scale(1.08);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  gap: 8px;
}

.cover-placeholder span {
  font-size: 12px;
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.7), rgba(22, 33, 62, 0.7));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.book-card:hover .card-overlay {
  opacity: 1;
}

.view-detail {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 20px;
  border: 2px solid #e8a87c;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.view-detail:hover {
  background: #e8a87c;
}

.stock-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(8px);
}

.stock-badge.in-stock {
  background: rgba(103, 194, 58, 0.9);
  color: #fff;
}

.stock-badge.out-stock {
  background: rgba(245, 108, 108, 0.9);
  color: #fff;
}

.card-info {
  padding: 16px 20px 20px;
}

.book-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.book-publisher {
  font-size: 12px;
  color: #bfbfbf;
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-stock {
  font-size: 12px;
  font-weight: 500;
  color: #67c23a;
}

.book-stock.stock-zero {
  color: #f56c6c;
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.pagination-section :deep(.el-pagination) {
  --el-pagination-bg-color: #fff;
  --el-pagination-hover-color: #e8a87c;
}

.pagination-section :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }

  .search-fields {
    flex-direction: column;
  }

  .search-select,
  .search-input,
  .search-input.small {
    width: 100%;
    max-width: 100%;
  }

  .book-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .book-grid {
    grid-template-columns: 1fr;
  }
}
</style>
