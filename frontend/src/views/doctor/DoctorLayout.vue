<template>
  <el-container class="layout">
    <el-aside width="200px" class="sidebar">
      <div class="brand">Doctor Desk</div>
      <el-menu router :default-active="activeMenu">
        <el-menu-item index="/doctor/home" @click="go('/doctor/home')">欢迎页</el-menu-item>
        <el-menu-item index="/doctor/today" @click="go('/doctor/today')">今日待诊</el-menu-item>
        <el-menu-item index="/doctor/schedule" @click="go('/doctor/schedule')">我的排班</el-menu-item>
        <el-menu-item index="/doctor/my" @click="go('/doctor/my')">我的信息</el-menu-item>
        <el-menu-item index="/doctor/weekend" @click="go('/doctor/weekend')">周末值班</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">医生工作台</div>
        <el-button type="primary" link @click="goLogin">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => (route.path.startsWith('/doctor') ? route.path : '/doctor/home'));

function go(path: string) {
  router.push(path);
}

function goLogin() {
  router.push('/login');
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.sidebar {
  border-right: 1px solid #ebeef5;
  padding-top: 12px;
}

.brand {
  padding: 12px 16px;
  font-weight: 700;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  font-weight: 600;
}
</style>
