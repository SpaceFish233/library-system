<template>
  <div class="register-page">
    <!-- Animated background particles -->
    <div class="bg-particles">
      <div v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)" />
    </div>

    <!-- Main card -->
    <div class="register-card" :class="{ 'card-enter': isVisible }">
      <!-- Left decorative panel -->
      <div class="card-left">
        <div class="book-stack">
          <div class="stack-book" v-for="i in 4" :key="i" :style="{ '--delay': i * 0.15 + 's' }" />
        </div>
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon :size="48"><Notebook /></el-icon>
          </div>
          <h1 class="brand-title">加入书海</h1>
          <p class="brand-subtitle">Start Your Reading Journey</p>
          <div class="features">
            <div class="feature-item" v-for="(feature, index) in features" :key="index">
              <el-icon class="feature-icon"><component :is="feature.icon" /></el-icon>
              <span>{{ feature.text }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right form panel -->
      <div class="card-right">
        <div class="form-header">
          <h2 class="form-title">创建账户</h2>
          <p class="form-subtitle">填写以下信息开始您的阅读之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <div class="form-row">
            <el-form-item prop="username" label="用户名" class="form-item-half">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="nickname" label="昵称" class="form-item-half">
              <el-input
                v-model="form.nickname"
                placeholder="请输入昵称"
                size="large"
                :prefix-icon="UserFilled"
                class="custom-input"
              />
            </el-form-item>
          </div>

          <el-form-item prop="password" label="密码">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword" label="确认密码">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="phone" label="手机号（可选）">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              size="large"
              :prefix-icon="Phone"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              <span class="btn-text">注 册</span>
              <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span class="footer-text">已有账户？</span>
          <router-link to="/login" class="login-link">
            立即登录
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  Lock,
  Phone,
  ArrowRight,
  Notebook,
  Reading,
  Star,
  Timer
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isVisible = ref(false)

const features = [
  { icon: 'Reading', text: '海量图书随时借阅' },
  { icon: 'Star', text: '信用体系公平公正' },
  { icon: 'Timer', text: '预约排队便捷高效' }
]

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
}

// Particle styles
function particleStyle(i) {
  const size = Math.random() * 4 + 2
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 20}s`,
    animationDuration: `${Math.random() * 20 + 15}s`,
    opacity: Math.random() * 0.3 + 0.1
  }
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone || undefined
    })
    ElMessage.success({
      message: '注册成功，欢迎加入！',
      duration: 2000
    })
    router.push('/')
  } catch (error) {
    // Error handled by interceptor
  } finally {
    loading.value = false
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

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
  padding: 40px 20px;
}

/* Animated background particles */
.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: rgba(232, 168, 124, 0.4);
  border-radius: 50%;
  animation: float-particle linear infinite;
}

@keyframes float-particle {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(100px, -50px) scale(1.2);
  }
  50% {
    transform: translate(-50px, 100px) scale(0.8);
  }
  75% {
    transform: translate(80px, 50px) scale(1.1);
  }
}

/* Register card */
.register-card {
  display: flex;
  width: 1000px;
  min-height: 640px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow:
    0 25px 50px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  opacity: 0;
  transform: translateY(30px) scale(0.95);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  backdrop-filter: blur(20px);
}

.register-card.card-enter {
  opacity: 1;
  transform: translateY(0) scale(1);
}

/* Left panel */
.card-left {
  width: 380px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.card-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(232, 168, 124, 0.1) 0%, transparent 60%);
  animation: rotate-glow 20s linear infinite;
}

@keyframes rotate-glow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Book stack decoration */
.book-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  margin-bottom: 32px;
}

.stack-book {
  width: 120px;
  height: 20px;
  background: linear-gradient(90deg, #e8a87c, #d4956b);
  border-radius: 2px 6px 6px 2px;
  transform: perspective(200px) rotateY(-5deg);
  animation: book-float 3s ease-in-out infinite;
  animation-delay: var(--delay);
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
}

.stack-book:nth-child(2) {
  width: 110px;
  background: linear-gradient(90deg, #d4956b, #c4845a);
}

.stack-book:nth-child(3) {
  width: 100px;
  background: linear-gradient(90deg, #c4845a, #b47349);
}

.stack-book:nth-child(4) {
  width: 90px;
  background: linear-gradient(90deg, #b47349, #a46238);
}

@keyframes book-float {
  0%, 100% {
    transform: perspective(200px) rotateY(-5deg) translateX(0);
  }
  50% {
    transform: perspective(200px) rotateY(-5deg) translateX(5px);
  }
}

.brand-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.brand-icon {
  color: #e8a87c;
  margin-bottom: 16px;
  animation: icon-pulse 2s ease-in-out infinite;
}

@keyframes icon-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.brand-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-family: 'Inter', sans-serif;
  font-size: 12px;
  color: rgba(232, 168, 124, 0.8);
  text-transform: uppercase;
  letter-spacing: 3px;
  margin: 0 0 40px 0;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(232, 168, 124, 0.15);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(4px);
}

.feature-icon {
  color: #e8a87c;
  font-size: 20px;
}

.feature-item span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

/* Right panel */
.card-right {
  flex: 1;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow-y: auto;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

/* Form styles */
.register-form {
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-item-half {
  flex: 1;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #4a4a4a;
  padding-bottom: 4px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8e8e8;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #e8a87c;
  box-shadow: 0 4px 12px rgba(232, 168, 124, 0.15);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #e8a87c;
  box-shadow: 0 4px 12px rgba(232, 168, 124, 0.2);
}

.custom-input :deep(.el-input__prefix) {
  color: #bfbfbf;
}

.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(26, 26, 46, 0.3);
}

.register-btn:hover::before {
  opacity: 1;
}

.btn-text,
.btn-icon {
  position: relative;
  z-index: 1;
}

.btn-icon {
  transition: transform 0.3s ease;
}

.register-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* Footer */
.form-footer {
  text-align: center;
}

.footer-text {
  color: #8c8c8c;
  font-size: 14px;
}

.login-link {
  color: #e8a87c;
  text-decoration: none;
  font-weight: 500;
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.login-link:hover {
  color: #d4956b;
  transform: translateX(4px);
}

/* Responsive */
@media (max-width: 1000px) {
  .register-card {
    flex-direction: column;
    width: 90%;
    max-width: 480px;
    min-height: auto;
  }

  .card-left {
    width: 100%;
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 24px;
  }

  .features {
    display: none;
  }

  .card-right {
    padding: 32px 24px;
  }
}

@media (max-width: 480px) {
  .register-page {
    padding: 20px;
  }

  .register-card {
    border-radius: 16px;
  }

  .card-left {
    padding: 24px 20px;
  }

  .card-right {
    padding: 24px 20px;
  }

  .form-title {
    font-size: 24px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
