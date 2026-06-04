import request from '../utils/request'

// 获取一级分类
export function getFirstLevelCategories() {
  return request.get('/categories')
}

// 获取子分类
export function getChildrenCategories(parentId) {
  return request.get(`/categories/${parentId}/children`)
}

// 根据ID获取分类详情
export function getCategoryById(id) {
  return request.get(`/categories/${id}`)
}

// 添加分类（管理员）
export function addCategory(data) {
  return request.post('/categories', data)
}

// 修改分类（管理员）
export function updateCategory(id, data) {
  return request.put(`/categories/${id}`, data)
}

// 删除分类（管理员）
export function deleteCategory(id) {
  return request.delete(`/categories/${id}`)
}
