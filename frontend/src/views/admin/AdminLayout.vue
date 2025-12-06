<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand">Registration Admin</div>
      <el-menu router :default-active="activeMenu">
        <el-menu-item index="/admin/home">首页</el-menu-item>
        <el-menu-item index="/admin/patients">病人管理</el-menu-item>
        <el-menu-item index="/admin/doctors">医生管理</el-menu-item>
        <el-menu-item index="/admin/departments">科室管理</el-menu-item>
        <el-menu-item index="/admin/department-diseases">科室疾病</el-menu-item>
        <el-menu-item index="/admin/schedules">排班管理</el-menu-item>
        <el-menu-item index="/admin/duty-schedule">值班管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">管理员后台</div>
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

const activeMenu = computed(() => (route.path.startsWith('/admin') ? route.path : '/admin/home'));

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
  font-size: 16px;
  color: #303133;
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
  font-size: 16px;
}
</style>
