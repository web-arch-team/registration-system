<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">统一登录</h2>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" :loading="loading" block>登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

const form = reactive({
  username: '',
  password: '',
});

const loading = ref(false);

async function onSubmit() {
  if (!form.username || !form.password) return;
  loading.value = true;
  try {
    const res = await axios.post('/api/login', form);
    const role = res.data?.role;
    if (role === 'ADMIN') router.push('/admin/patients');
    else if (role === 'DOCTOR') router.push('/doctor/today');
    else if (role === 'PATIENT') router.push('/patient/booking');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}

.login-card {
  width: 360px;
}

.title {
  text-align: center;
  margin-bottom: 20px;
}
</style>

