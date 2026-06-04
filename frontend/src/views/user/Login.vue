<template>
  <div class="login-page">
    <!-- Animated background particles -->
    <div class="bg-particles">
      <div v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)" />
    </div>

    <!-- Main card -->
    <div class="login-card" :class="{ 'card-enter': isVisible }">
      <!-- Left decorative panel -->
      <div class="card-left">
        <div class="book-decoration">
          <div class="book-page page-1"></div>
          <div class="book-page page-2"></div>
          <div class="book-page page-3"></div>
        </div>
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon :size="48"><Reading /></el-icon>
          </div>
          <h1 class="brand-title">书海拾贝</h1>
          <p class="brand-subtitle">Library Management System</p>
          <div class="brand-quote">
            <span class="quote-mark">"</span>
            <p>书籍是人类进步的阶梯</p>
            <span class="quote-author">—— 高尔基</span>
          </div>
        </div>
        <div class="decorative-dots">
          <span v-for="i in 5" :key="i" class="dot" />
        </div>
      </div>

      <!-- Right form panel -->
      <div class="card-right">
        <div class="form-header">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请登录您的账户以继续</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="btn-text">登 录</span>
              <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span class="footer-text">还没有账户？</span>
          <router-link to="/register" class="register-link">
            立即注册
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <!-- Test account hint -->
        <div class="test-hint">
          <el-divider>
            <span class="divider-text">测试账号</span>
          </el-divider>
          <div class="hint-content">
            <el-tag
              class="hint-tag"
              effect="plain"
              @click="fillTestAccount('admin')"
            >
              管理员: admin / 123456
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom wave -->
    <div class="bottom-wave">
      <svg viewBox="0 0 1440 120" preserveAspectRatio="none">
        <path d="M0,60 C360,120 720,0 1080,60 C1260,90 1380,30 1440,60 L1440,120 L0,120 Z" fill="rgba(255,255,255,0.05)" />
        <path d="M0,80 C480,20 960,100 1440,40 L1440,120 L0,120 Z" fill="rgba(255,255,255,0.03)" />
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight, Reading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isVisible = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
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

function fillTestAccount(type) {
  if (type === 'admin') {
    form.username = 'admin'
    form.password = '123456'
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success({
      message: '登录成功，欢迎回来！',
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

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
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

/* Login card */
.login-card {
  display: flex;
  width: 900px;
  min-height: 560px;
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

.login-card.card-enter {
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

/* Book decoration */
.book-decoration {
  position: relative;
  width: 80px;
  height: 100px;
  margin-bottom: 32px;
}

.book-page {
  position: absolute;
  width: 60px;
  height: 80px;
  background: linear-gradient(135deg, #f5e6d3, #e8d5c4);
  border-radius: 0 4px 4px 0;
  transform-origin: left center;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
}

.page-1 {
  transform: rotate(-5deg);
  animation: page-flutter 3s ease-in-out infinite;
}

.page-2 {
  transform: rotate(0deg);
  animation: page-flutter 3s ease-in-out infinite 0.2s;
}

.page-3 {
  transform: rotate(5deg);
  animation: page-flutter 3s ease-in-out infinite 0.4s;
}

@keyframes page-flutter {
  0%, 100% {
    transform: rotate(var(--rotation, -5deg));
  }
  50% {
    transform: rotate(calc(var(--rotation, -5deg) + 3deg));
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
  margin: 0 0 32px 0;
}

.brand-quote {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(232, 168, 124, 0.2);
  position: relative;
}

.quote-mark {
  font-family: 'Noto Serif SC', serif;
  font-size: 48px;
  color: #e8a87c;
  position: absolute;
  top: -10px;
  left: 12px;
  line-height: 1;
  opacity: 0.6;
}

.brand-quote p {
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 8px 0;
  padding-left: 24px;
}

.quote-author {
  font-size: 12px;
  color: rgba(232, 168, 124, 0.7);
  display: block;
  text-align: right;
}

.decorative-dots {
  display: flex;
  gap: 8px;
  margin-top: 32px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(232, 168, 124, 0.4);
  animation: dot-pulse 2s ease-in-out infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
.dot:nth-child(4) { animation-delay: 0.6s; }
.dot:nth-child(5) { animation-delay: 0.8s; }

@keyframes dot-pulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

/* Right panel */
.card-right {
  flex: 1;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 36px;
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
.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
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

.login-btn {
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

.login-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #e8a87c, #d4956b);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(26, 26, 46, 0.3);
}

.login-btn:hover::before {
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

.login-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* Footer */
.form-footer {
  text-align: center;
  margin-bottom: 24px;
}

.footer-text {
  color: #8c8c8c;
  font-size: 14px;
}

.register-link {
  color: #e8a87c;
  text-decoration: none;
  font-weight: 500;
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #d4956b;
  transform: translateX(4px);
}

/* Test hint */
.test-hint {
  padding: 0 20px;
}

.test-hint :deep(.el-divider__text) {
  background: rgba(255, 255, 255, 0.95);
}

.divider-text {
  font-size: 12px;
  color: #bfbfbf;
}

.hint-content {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.hint-tag {
  cursor: pointer;
  border-radius: 8px;
  font-size: 12px;
  transition: all 0.3s ease;
}

.hint-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* Bottom wave */
.bottom-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  pointer-events: none;
}

.bottom-wave svg {
  display: block;
  width: 100%;
  height: 80px;
}

/* Responsive */
@media (max-width: 900px) {
  .login-card {
    flex-direction: column;
    width: 90%;
    max-width: 420px;
    min-height: auto;
  }

  .card-left {
    width: 100%;
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 24px;
  }

  .brand-quote {
    display: none;
  }

  .card-right {
    padding: 32px 24px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 20px;
  }

  .login-card {
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
}
</style>
