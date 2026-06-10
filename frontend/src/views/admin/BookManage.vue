<template>
  <div class="book-manage-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon class="title-icon"><Management /></el-icon>
          图书管理
        </h1>
        <p class="page-subtitle">管理图书馆的所有藏书</p>
      </div>
      <el-button type="primary" class="add-btn" @click="openDialog(null)">
        <el-icon><Plus /></el-icon>
        新增图书
      </el-button>
    </div>

    <!-- Search bar -->
    <div class="search-card">
      <div class="search-fields">
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
        <el-select
          v-model="searchForm.categoryId"
          placeholder="选择分类"
          clearable
          class="search-select"
        >
          <el-option-group
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
          >
            <el-option :label="cat.name" :value="cat.id" />
            <el-option
              v-for="child in cat.children"
              :key="child.id"
              :label="'　' + child.name"
              :value="child.id"
            />
          </el-option-group>
        </el-select>
        <el-button type="primary" class="search-btn" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button class="reset-btn" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- Book table -->
    <div class="table-card">
      <el-table
        :data="books"
        stripe
        class="book-table"
        v-loading="loading"
      >
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <div class="table-cover">
              <img
                v-if="row.coverImage"
                :src="'/uploads/' + row.coverImage"
                :alt="row.title"
                class="cover-thumb"
              />
              <el-icon v-else class="cover-icon"><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="book-title-cell">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="author" label="作者" min-width="160" show-overflow-tooltip />

        <el-table-column prop="isbn" label="ISBN" width="150" show-overflow-tooltip />

        <el-table-column prop="publisher" label="出版社" width="150" show-overflow-tooltip />

        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stock > 0 ? 'success' : 'danger'" size="small">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
          @current-change="fetchBooks"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑图书' : '新增图书'"
      width="600px"
      class="book-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="bookForm"
        :rules="formRules"
        label-width="80px"
        class="book-form"
      >
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名" />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="ISBN" prop="isbn" class="form-item-half">
            <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
          </el-form-item>
          <el-form-item label="作者" prop="author" class="form-item-half">
            <el-input v-model="bookForm.author" placeholder="请输入作者" />
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="出版社" prop="publisher" class="form-item-half">
            <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
          </el-form-item>
          <el-form-item label="库存" prop="stock" class="form-item-half">
            <el-input-number v-model="bookForm.stock" :min="0" style="width: 100%" />
          </el-form-item>
        </div>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="bookForm.categoryId" placeholder="选择分类" clearable style="width: 100%">
            <el-option-group
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
            >
              <el-option :label="cat.name" :value="cat.id" />
              <el-option
                v-for="child in cat.children"
                :key="child.id"
                :label="'　' + child.name"
                :value="child.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="简介">
          <el-input
            v-model="bookForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图书简介"
          />
        </el-form-item>

        <el-form-item label="封面">
          <el-upload
            class="cover-upload"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleCoverChange"
            accept="image/*"
          >
            <div v-if="coverPreview" class="upload-preview">
              <img :src="coverPreview" class="preview-img" />
              <div class="preview-overlay">
                <el-icon><Edit /></el-icon>
                <span>更换</span>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传封面</span>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Management,
  Plus,
  Search,
  Edit,
  Delete,
  Picture
} from '@element-plus/icons-vue'
import { searchBooks, addBook, updateBook, deleteBook } from '../../api/book'
import { getFirstLevelCategories, getChildrenCategories } from '../../api/category'

const loading = ref(false)
const submitLoading = ref(false)
const books = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  author: '',
  categoryId: null
})

// Dialog state
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const coverFile = ref(null)
const coverPreview = ref('')

const bookForm = reactive({
  title: '',
  isbn: '',
  author: '',
  publisher: '',
  stock: 0,
  categoryId: null,
  description: ''
})

const formRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }]
}

async function fetchCategories() {
  try {
    const res = await getFirstLevelCategories()
    const firstLevel = res.data
    for (const cat of firstLevel) {
      try {
        const childRes = await getChildrenCategories(cat.id)
        cat.children = childRes.data
      } catch (e) {
        cat.children = []
      }
    }
    categories.value = firstLevel
  } catch (e) {
    console.error(e)
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
    if (searchForm.categoryId) params.categoryId = searchForm.categoryId

    const res = await searchBooks(params)
    books.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
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
  searchForm.categoryId = null
  currentPage.value = 1
  fetchBooks()
}

function handleSizeChange() {
  currentPage.value = 1
  fetchBooks()
}

function openDialog(book) {
  isEdit.value = !!book
  editingId.value = book?.id || null
  coverFile.value = null
  coverPreview.value = book?.coverImage ? '/uploads/' + book.coverImage : ''

  bookForm.title = book?.title || ''
  bookForm.isbn = book?.isbn || ''
  bookForm.author = book?.author || ''
  bookForm.publisher = book?.publisher || ''
  bookForm.stock = book?.stock || 0
  bookForm.categoryId = book?.categoryId || null
  bookForm.description = book?.description || ''

  dialogVisible.value = true
}

function handleCoverChange(file) {
  coverFile.value = file.raw
  coverPreview.value = URL.createObjectURL(file.raw)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const formData = new FormData()
    const bookData = { ...bookForm }
    formData.append('book', new Blob([JSON.stringify(bookData)], { type: 'application/json' }))
    if (coverFile.value) {
      formData.append('coverImage', coverFile.value)
    }

    if (isEdit.value) {
      await updateBook(editingId.value, formData)
      ElMessage.success('修改成功')
    } else {
      await addBook(formData)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchBooks()
  } catch (e) {
    // Error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(book) {
  try {
    await ElMessageBox.confirm(
      `确定要删除《${book.title}》吗？此操作不可恢复。`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )

    await deleteBook(book.id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (e) {
    if (e !== 'cancel') {
      // Error handled by interceptor
    }
  }
}

onMounted(() => {
  fetchCategories()
  fetchBooks()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.book-manage-page {
  font-family: 'Inter', sans-serif;
}

/* Page header */
.page-header {
  display: flex;
  align-items: flex-start;
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

.add-btn {
  height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(26, 26, 46, 0.3);
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

.search-input.small {
  max-width: 140px;
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

/* Table card */
.table-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(232, 168, 124, 0.1);
}

.book-table {
  border-radius: 10px;
  overflow: hidden;
}

.book-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #1a1a2e;
  font-weight: 600;
}

.table-cover {
  width: 50px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-icon {
  color: #c0c4cc;
  font-size: 20px;
}

.book-title-cell {
  font-weight: 500;
  color: #1a1a2e;
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

/* Dialog */
.book-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.book-dialog :deep(.el-dialog__title) {
  font-family: 'Noto Serif SC', serif;
  font-weight: 600;
  color: #1a1a2e;
}

.book-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4a4a4a;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-item-half {
  flex: 1;
}

/* Cover upload */
.cover-upload {
  width: 120px;
  height: 150px;
}

.upload-placeholder {
  width: 120px;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8c8c8c;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-placeholder:hover {
  border-color: #e8a87c;
  color: #e8a87c;
}

.upload-placeholder .el-icon {
  font-size: 24px;
}

.upload-placeholder span {
  font-size: 12px;
}

.upload-preview {
  width: 120px;
  height: 150px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(26, 26, 46, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.upload-preview:hover .preview-overlay {
  opacity: 1;
}

.preview-overlay .el-icon {
  font-size: 20px;
}

.preview-overlay span {
  font-size: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .search-fields {
    flex-direction: column;
  }

  .search-input,
  .search-input.small,
  .search-select {
    width: 100%;
    max-width: 100%;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
