<template>
  <div class="profile-page">
    <!-- Page header -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon class="title-icon"><User /></el-icon>
        个人中心
      </h1>
      <p class="page-subtitle">管理您的个人信息</p>
    </div>

    <div class="profile-layout">
      <!-- Left: User card -->
      <div class="user-card" :class="{ 'card-enter': isVisible }">
        <div class="card-header">
          <div class="avatar-wrapper">
            <div class="avatar">
              <span>{{ userStore.userInfo?.nickname?.[0] || userStore.userInfo?.username?.[0] || '?' }}</span>
            </div>
            <div class="role-badge" :class="userStore.isAdmin ? 'admin' : 'user'">
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </div>
          </div>

          <h2 class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h2>
          <p class="user-username">@{{ userStore.userInfo?.username }}</p>
        </div>

        <div class="card-body">
          <!-- Credit score -->
          <div class="credit-section">
            <div class="credit-header">
              <span class="credit-label">信用分</span>
              <span class="credit-value" :class="creditLevel">
                {{ userStore.userInfo?.creditScore || 0 }}
              </span>
            </div>
            <div class="credit-bar">
              <div
                class="credit-fill"
                :class="creditLevel"
                :style="{ width: (userStore.userInfo?.creditScore || 0) + '%' }"
              />
            </div>
            <p class="credit-hint" :class="creditLevel">
              {{ creditHint }}
            </p>
          </div>

          <!-- User info -->
          <div class="info-list">
            <div class="info-item">
              <el-icon><Iphone /></el-icon>
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userStore.userInfo?.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatTime(userStore.userInfo?.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Forms -->
      <div class="forms-section">
        <!-- Edit profile -->
        <div class="form-card" :class="{ 'card-enter': isVisible }" style="--delay: 0.1s">
          <h3 class="form-title">
            <el-icon><Edit /></el-icon>
            修改个人信息
          </h3>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="80px"
            class="profile-form"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                class="save-btn"
                :loading="profileLoading"
                @click="handleUpdateProfile"
              >
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Change password -->
        <div class="form-card" :class="{ 'card-enter': isVisible }" style="--delay: 0.2s">
          <h3 class="form-title">
            <el-icon><Lock /></el-icon>
            修改密码
          </h3>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="80px"
            class="password-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                class="save-btn"
                :loading="passwordLoading"
                @click="handleChangePassword"
              >
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import {
  User,
  Iphone,
  Clock,
  Edit,
  Lock
} from '@element-plus/icons-vue'
import { updateUserInfo, changePassword } from '../../api/user'

const userStore = useUserStore()
const isVisible = ref(false)
const profileLoading = ref(false)
const passwordLoading = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  nickname: userStore.userInfo?.nickname || '',
  phone: userStore.userInfo?.phone || ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const creditLevel = computed(() => {
  const score = userStore.userInfo?.creditScore || 0
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  return 'danger'
})

const creditHint = computed(() => {
  const score = userStore.userInfo?.creditScore || 0
  if (score >= 80) return '信用优秀，可正常借阅'
  if (score >= 60) return '信用良好，请保持'
  return '信用分不足60，无法借阅'
})

function formatTime(time) {
  if (!time) return '未知'
  return time.replace('T', ' ').substring(0, 16)
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    await updateUserInfo({
      nickname: profileForm.nickname,
      phone: profileForm.phone
    })
    await userStore.fetchUserInfo()
    ElMessage.success('个人信息更新成功')
  } catch (e) {
    // Error handled by interceptor
  } finally {
    profileLoading.value = false
  }
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    // Error handled by interceptor
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Inter:wght@300;400;500;600&display=swap');

.profile-page {
  font-family: 'Inter', sans-serif;
  max-width: 1100px;
  margin: 0 auto;
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

/* Profile layout */
.profile-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* User card */
.user-card {
  width: 340px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(232, 168, 124, 0.1);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.user-card.card-enter {
  opacity: 1;
  transform: translateY(0);
}

.card-header {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  padding: 32px 24px;
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  font-family: 'Noto Serif SC', serif;
  box-shadow: 0 4px 16px rgba(232, 168, 124, 0.4);
}

.role-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  border: 2px solid #1a1a2e;
}

.role-badge.admin {
  background: #e8a87c;
  color: #1a1a2e;
}

.role-badge.user {
  background: #67c23a;
  color: #fff;
}

.user-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 4px 0;
}

.user-username {
  font-size: 14px;
  color: rgba(232, 168, 124, 0.8);
  margin: 0;
}

.card-body {
  padding: 24px;
}

/* Credit score */
.credit-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.credit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.credit-label {
  font-size: 14px;
  color: #606266;
}

.credit-value {
  font-size: 28px;
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
}

.credit-value.excellent { color: #67c23a; }
.credit-value.good { color: #e8a87c; }
.credit-value.danger { color: #f56c6c; }

.credit-bar {
  height: 8px;
  background: #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.credit-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 1s cubic-bezier(0.16, 1, 0.3, 1);
}

.credit-fill.excellent { background: linear-gradient(90deg, #67c23a, #85ce61); }
.credit-fill.good { background: linear-gradient(90deg, #e8a87c, #d4956b); }
.credit-fill.danger { background: linear-gradient(90deg, #f56c6c, #f89898); }

.credit-hint {
  font-size: 12px;
  margin: 0;
}

.credit-hint.excellent { color: #67c23a; }
.credit-hint.good { color: #e8a87c; }
.credit-hint.danger { color: #f56c6c; }

/* Info list */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 10px;
}

.info-item .el-icon {
  color: #e8a87c;
  font-size: 18px;
}

.info-label {
  font-size: 13px;
  color: #909399;
  min-width: 60px;
}

.info-value {
  font-size: 14px;
  color: #1a1a2e;
  font-weight: 500;
}

/* Forms section */
.forms-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(232, 168, 124, 0.1);
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  transition-delay: var(--delay, 0s);
}

.form-card.card-enter {
  opacity: 1;
  transform: translateY(0);
}

.form-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-title .el-icon {
  color: #e8a87c;
}

.profile-form :deep(.el-form-item__label),
.password-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4a4a4a;
}

.save-btn {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
  border-radius: 10px;
  padding: 10px 32px;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(26, 26, 46, 0.3);
}

/* Responsive */
@media (max-width: 900px) {
  .profile-layout {
    flex-direction: column;
  }

  .user-card {
    width: 100%;
  }
}
</style>
