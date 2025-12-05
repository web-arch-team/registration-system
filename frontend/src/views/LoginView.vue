<template>
  <div class="login-page">
    <div class="login-inner">
      <div class="intro">
        <p class="eyebrow">Registration System</p>
        <h1>统一登录</h1>
        <p class="sub">
          这是 Vue + Element Plus 的前端框架，现阶段以演示为主。
          示例账号可直接跳转到对应角色欢迎页，后续可无缝对接后端登录接口。
        </p>
      </div>

      <el-card class="login-card" shadow="hover">
        <h3 class="title">账号登录</h3>
        <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="例如：admin" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              autocomplete="current-password"
            />
          </el-form-item>
          <el-form-item label="身份">
            <el-radio-group v-model="form.role">
              <el-radio-button label="PATIENT">病人</el-radio-button>
              <el-radio-button label="DOCTOR">医生</el-radio-button>
              <el-radio-button label="ADMIN">管理员</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-alert
            v-if="loginError"
            type="error"
            :closable="false"
            show-icon
            class="mb-12"
            :title="loginError"
          />

          <div class="actions">
            <el-button type="primary" :loading="loading" class="submit" @click="onSubmit">登录</el-button>
            <el-button class="submit" plain @click="goRegister">患者注册</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api/auth';
import type { LoginResult } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

type UserRole = 'ADMIN' | 'DOCTOR' | 'PATIENT';

interface DemoUser {
  label: string;
  username: string;
  password: string;
  role: UserRole;
}

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const loginError = ref('');
const form = reactive({
  username: '',
  password: '',
  role: 'PATIENT' as UserRole,
});

function navigateByRole(role: UserRole) {
  if (role === 'ADMIN') router.push('/admin/home');
  else if (role === 'DOCTOR') router.push('/doctor/home');
  else router.push('/patient/home');
}

function handleLoginSuccess(data: LoginResult) {
  authStore.setUser(data);
  ElMessage.success('登录成功');
  navigateByRole(data.role);
}

async function onSubmit() {
  if (!form.username || !form.password) {
    loginError.value = '请输入用户名和密码';
    return;
  }
  loading.value = true;
  loginError.value = '';

  try {
    const result = await login({
      username: form.username,
      password: form.password,
    });
    handleLoginSuccess(result);
  } catch (error) {
    loginError.value = '登录失败，请检查账号密码或稍后再试';
  } finally {
    loading.value = false;
  }
}

function goRegister() {
  router.push('/patient/register');
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  background: radial-gradient(circle at 20% 20%, #f0f6ff, #ffffff 45%), #f5f7fb;
}

.login-inner {
  width: 100%;
  max-width: 1040px; /* constrain overall content */
  display: grid;
  grid-template-columns: 1fr 440px; /* left intro and right card fixed width */
  gap: 32px;
  align-items: center;
}

.intro {
  max-width: 560px;
}

.eyebrow {
  font-size: 13px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #409eff;
  margin: 0 0 8px 0;
}

h1 {
  margin: 6px 0 10px;
  font-size: 32px;
  line-height: 1.1;
}

.sub {
  color: #606266;
  margin: 0 0 12px;
  font-size: 14px;
}

.login-card {
  width: 100%;
  max-width: 440px;
  box-sizing: border-box;
}

.title {
  margin: 0 0 12px;
  font-weight: 600;
}

/* Actions: inline on wide screens, stacked on small screens */
.actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.submit {
  flex: 1 1 0;
}

/* make buttons side-by-side on wider viewports */
@media (min-width: 720px) {
  .actions { flex-direction: row; }
  .submit { width: auto; }
}

/* for small screens stack vertically */
@media (max-width: 719px) {
  .login-inner { grid-template-columns: 1fr; }
  .intro { order: 1; }
  .login-card { order: 2; }
  .actions { flex-direction: column; }
  .submit { width: 100%; }
}

.mb-12 { margin-bottom: 12px; }
</style>
