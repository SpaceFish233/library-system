import request from '../utils/request'

// 获取用户信息
export function getUserInfo() {
  return request.get('/user/info')
}

// 修改用户信息
export function updateUserInfo(data) {
  return request.put('/user/info', data)
}

// 修改密码
export function changePassword(data) {
  return request.put('/user/password', data)
}

// 获取通知
export function getNotifications() {
  return request.get('/user/notifications')
}
