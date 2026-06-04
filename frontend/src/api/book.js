import request from '../utils/request'

// 搜索图书
export function searchBooks(params) {
  return request.get('/books', { params })
}

// 获取图书详情
export function getBookDetail(id) {
  return request.get(`/books/${id}`)
}

// 添加图书（管理员）
export function addBook(data) {
  return request.post('/books', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 修改图书（管理员）
export function updateBook(id, data) {
  return request.put(`/books/${id}`, data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除图书（管理员）
export function deleteBook(id) {
  return request.delete(`/books/${id}`)
}
