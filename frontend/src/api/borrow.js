import request from '../utils/request'

// 借书
export function borrowBook(bookId, duration, unit) {
  return request.post('/borrow', { bookId, duration, unit })
}

// 申请归还
export function applyReturn(recordId) {
  return request.put(`/borrow/${recordId}/apply-return`)
}

// 管理员确认归还
export function confirmReturn(recordId) {
  return request.put(`/borrow/${recordId}/confirm-return`)
}

// 获取我的借阅记录
export function getMyBorrows(params) {
  return request.get('/borrow/my', { params })
}

// 获取所有借阅记录（管理员）
export function getAllBorrows(params) {
  return request.get('/borrow/all', { params })
}

// 获取借阅记录详情
export function getBorrowDetail(recordId) {
  return request.get(`/borrow/${recordId}`)
}
