<template>
  <div class="login-page">
    <div class="intro">
      <p class="eyebrow">Registration System</p>
      <h1>统一登录</h1>
      <p class="sub">
        这是 Vue + Element Plus 的前端框架，现阶段以演示为主。
        示例账号可直接跳转到对应角色欢迎页，后续可无缝对接后端登录接口。
      </p>
      <div class="chips">
        <el-tag v-for="item in demoUsers" :key="item.username" @click="applyDemo(item)" class="chip" effect="plain">
          {{ item.label }}：{{ item.username }} / {{ item.password }}
        </el-tag>
      </div>
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

        <el-button type="primary" :loading="loading" class="submit" @click="onSubmit">登录</el-button>
      </el-form>
    </el-card>
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

const demoUsers: DemoUser[] = [
  { label: '病人', username: 'patient', password: 'patient123', role: 'PATIENT' },
  { label: '医生', username: 'doctor', password: 'doctor123', role: 'DOCTOR' },
  { label: '管理员', username: 'admin', password: 'admin123', role: 'ADMIN' },
];

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const loginError = ref('');
const form = reactive({
  username: demoUsers[0].username,
  password: demoUsers[0].password,
  role: demoUsers[0].role as UserRole,
});

function navigateByRole(role: UserRole) {
  if (role === 'ADMIN') router.push('/admin/home');
  else if (role === 'DOCTOR') router.push('/doctor/home');
  else router.push('/patient/home');
}

function applyDemo(user: DemoUser) {
  form.username = user.username;
  form.password = user.password;
  form.role = user.role;
  loginError.value = '';
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
    // 后端未连通时，允许使用示例账号演示跳转
    const demo = demoUsers.find(
      (item) => item.username === form.username && item.password === form.password,
    );
    if (demo) {
      handleLoginSuccess({
        userId: -1,
        username: demo.username,
        role: demo.role,
      });
    } else {
      loginError.value = '登录失败，请检查账号密码或稍后再试';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
  align-items: center;
  padding: 48px 32px;
  background: radial-gradient(circle at 20% 20%, #f0f6ff, #ffffff 45%), #f5f7fb;
}

.intro {
  max-width: 460px;
}

.eyebrow {
  font-size: 13px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #409eff;
  margin: 0;
}

h1 {
  margin: 10px 0 6px;
  font-size: 28px;
}

.sub {
  color: #606266;
  margin: 0 0 12px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  cursor: pointer;
}

.login-card {
  max-width: 420px;
  margin-left: auto;
}

.title {
  margin: 0 0 12px;
}

.submit {
  width: 100%;
}

.mb-12 {
  margin-bottom: 12px;
}
</style>
