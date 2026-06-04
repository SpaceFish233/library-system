<template>
  <div class="category-manage-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon class="title-icon"><FolderOpened /></el-icon>
          分类管理
        </h1>
        <p class="page-subtitle">管理图书的分类体系</p>
      </div>
      <el-button type="primary" class="add-btn" @click="openAddDialog(null)">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <div class="content-layout">
      <!-- Left: Category tree -->
      <div class="tree-panel">
        <div class="panel-header">
          <h3 class="panel-title">分类结构</h3>
          <el-button link @click="fetchCategories">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <div v-if="categories.length === 0" class="empty-tree">
          <el-icon><FolderOpened /></el-icon>
          <p>暂无分类</p>
        </div>

        <div v-else class="tree-content">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="tree-group"
          >
            <!-- First level -->
            <div
              class="tree-item level-1"
              :class="{ active: selectedId === cat.id }"
              @click="selectCategory(cat)"
            >
              <div class="item-left">
                <el-icon class="folder-icon"><Folder /></el-icon>
                <span class="item-name">{{ cat.name }}</span>
                <el-tag size="small" type="info" class="child-count">
                  {{ cat.children?.length || 0 }}
                </el-tag>
              </div>
              <div class="item-actions" @click.stop>
                <el-button link size="small" @click="openAddDialog(cat)">
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button link size="small" @click="openEditDialog(cat)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button link size="small" type="danger" @click="handleDelete(cat)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>

            <!-- Second level -->
            <div v-if="cat.children && cat.children.length > 0" class="tree-children">
              <div
                v-for="child in cat.children"
                :key="child.id"
                class="tree-item level-2"
                :class="{ active: selectedId === child.id }"
                @click="selectCategory(child)"
              >
                <div class="item-left">
                  <el-icon class="doc-icon"><Document /></el-icon>
                  <span class="item-name">{{ child.name }}</span>
                </div>
                <div class="item-actions" @click.stop>
                  <el-button link size="small" @click="openEditDialog(child)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button link size="small" type="danger" @click="handleDelete(child)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Detail panel -->
      <div class="detail-panel">
        <div v-if="!selectedCategory" class="no-selection">
          <el-icon class="no-sel-icon"><InfoFilled /></el-icon>
          <h3>选择一个分类</h3>
          <p>点击左侧分类查看详情或进行操作</p>
        </div>

        <div v-else class="category-detail" :class="{ 'detail-enter': detailVisible }">
          <div class="detail-header">
            <div class="detail-icon" :class="selectedCategory.parentId === 0 ? 'level-1' : 'level-2'">
              <el-icon :size="32">
                <Folder v-if="selectedCategory.parentId === 0" />
                <Document v-else />
              </el-icon>
            </div>
            <div class="detail-info">
              <h2 class="detail-name">{{ selectedCategory.name }}</h2>
              <p class="detail-type">
                {{ selectedCategory.parentId === 0 ? '一级分类' : '二级分类' }}
              </p>
            </div>
          </div>

          <div class="detail-meta">
            <div class="meta-item">
              <span class="meta-label">分类ID</span>
              <span class="meta-value">{{ selectedCategory.id }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">父分类ID</span>
              <span class="meta-value">{{ selectedCategory.parentId || '无（一级分类）' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">排序权重</span>
              <span class="meta-value">{{ selectedCategory.sortOrder || 0 }}</span>
            </div>
          </div>

          <div class="detail-actions">
            <el-button type="primary" @click="openEditDialog(selectedCategory)">
              <el-icon><Edit /></el-icon>
              编辑分类
            </el-button>
            <el-button
              v-if="selectedCategory.parentId === 0"
              @click="openAddDialog(selectedCategory)"
            >
              <el-icon><Plus /></el-icon>
              添加子分类
            </el-button>
            <el-button type="danger" @click="handleDelete(selectedCategory)">
              <el-icon><Delete /></el-icon>
              删除分类
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="450px"
      class="category-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="categoryForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="父分类">
          <el-select
            v-model="categoryForm.parentId"
            placeholder="无（作为一级分类）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number
            v-model="categoryForm.sortOrder"
            :min="0"
            :max="999"
            style="width: 100%"
          />
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
  FolderOpened,
  Folder,
  Document,
  Plus,
  Edit,
  Delete,
  Refresh,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  getFirstLevelCategories,
  getChildrenCategories,
  addCategory,
  updateCategory,
  deleteCategory
} from '../../api/category'

const categories = ref([])
const selectedId = ref(null)
const selectedCategory = ref(null)
const detailVisible = ref(false)

// Dialog state
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)

const categoryForm = reactive({
  name: '',
  parentId: null,
  sortOrder: 0
})

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function fetchCategories() {
  try {
    const res = await getFirstLevelCategories()
    const firstLevel = res.data

    // Fetch children for each first level category
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

function selectCategory(cat) {
  selectedId.value = cat.id
  selectedCategory.value = cat
  detailVisible.value = false
  setTimeout(() => {
    detailVisible.value = true
  }, 50)
}

function openAddDialog(parent) {
  isEdit.value = false
  editingId.value = null
  categoryForm.name = ''
  categoryForm.parentId = parent?.id || null
  categoryForm.sortOrder = 0
  dialogVisible.value = true
}

function openEditDialog(cat) {
  isEdit.value = true
  editingId.value = cat.id
  categoryForm.name = cat.name
  categoryForm.parentId = cat.parentId || null
  categoryForm.sortOrder = cat.sortOrder || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      name: categoryForm.name,
      parentId: categoryForm.parentId || 0,
      sortOrder: categoryForm.sortOrder
    }

    if (isEdit.value) {
      await updateCategory(editingId.value, data)
      ElMessage.success('修改成功')
    } else {
      await addCategory(data)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchCategories()
    selectedCategory.value = null
    selectedId.value = null
  } catch (e) {
    // Error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(cat) {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${cat.name}"吗？`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )

    await deleteCategory(cat.id)
    ElMessage.success('删除成功')
    fetchCategories()
    if (selectedId.value === cat.id) {
      selectedCategory.value = null
      selectedId.value = null
    }
  } catch (e) {
    if (e !== 'cancel') {
      // Error handled by interceptor
    }
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.category-manage-page {
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

/* Content layout */
.content-layout {
  display: flex;
  gap: 24px;
  min-height: 500px;
}

/* Tree panel */
.tree-panel {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(232, 168, 124, 0.1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.empty-tree {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #c0c4cc;
}

.empty-tree .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-tree p {
  font-size: 14px;
  margin: 0;
}

.tree-content {
  padding: 12px;
  max-height: 600px;
  overflow-y: auto;
}

.tree-group {
  margin-bottom: 4px;
}

.tree-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tree-item:hover {
  background: #f8f9fa;
}

.tree-item.active {
  background: rgba(232, 168, 124, 0.1);
  border: 1px solid rgba(232, 168, 124, 0.2);
}

.tree-item.level-1 {
  font-weight: 500;
}

.tree-item.level-2 {
  padding-left: 32px;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.folder-icon {
  color: #e8a87c;
  font-size: 18px;
}

.doc-icon {
  color: #909399;
  font-size: 16px;
}

.item-name {
  font-size: 14px;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.child-count {
  margin-left: 4px;
}

.item-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.tree-item:hover .item-actions {
  opacity: 1;
}

.tree-children {
  margin-left: 20px;
  border-left: 2px solid #f0f0f0;
}

/* Detail panel */
.detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(232, 168, 124, 0.1);
  overflow: hidden;
}

.no-selection {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
  color: #c0c4cc;
}

.no-sel-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.no-selection h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  color: #606266;
  margin: 0 0 8px 0;
}

.no-selection p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* Category detail */
.category-detail {
  padding: 32px;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.category-detail.detail-enter {
  opacity: 1;
  transform: translateY(0);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.detail-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.detail-icon.level-1 {
  background: linear-gradient(135deg, #e8a87c, #d4956b);
}

.detail-icon.level-2 {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

.detail-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.detail-type {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.detail-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 14px;
  margin-bottom: 32px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-label {
  font-size: 12px;
  color: #8c8c8c;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.meta-value {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.detail-actions {
  display: flex;
  gap: 12px;
}

.detail-actions .el-button {
  border-radius: 10px;
}

/* Dialog */
.category-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.category-dialog :deep(.el-dialog__title) {
  font-family: 'Noto Serif SC', serif;
  font-weight: 600;
  color: #1a1a2e;
}

/* Responsive */
@media (max-width: 900px) {
  .content-layout {
    flex-direction: column;
  }

  .tree-panel {
    width: 100%;
  }

  .detail-meta {
    grid-template-columns: 1fr;
  }
}
</style>
