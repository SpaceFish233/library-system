import request from '../utils/request'

// 预约图书
export function reserveBook(bookId) {
  return request.post(`/reserve/${bookId}`)
}

// 获取我的预约
export function getMyReserves() {
  return request.get('/reserve/my')
}

// 获取待领取预约
export function getReadyReserves() {
  return request.get('/reserve/ready')
}

// 领取预约
export function claimReserve(reserveId, duration, unit) {
  return request.put(`/reserve/${reserveId}/claim`, { duration, unit })
}

// 取消预约
export function cancelReserve(reserveId) {
  return request.put(`/reserve/${reserveId}/cancel`)
}

// 获取所有预约（管理员）
export function getAllReserves() {
  return request.get('/reserve/all')
}

// 获取某本书的预约等待人数（公开接口）
export function getWaitingCount(bookId) {
  return request.get(`/reserve/waiting-count/${bookId}`)
}
