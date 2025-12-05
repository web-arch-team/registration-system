<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">患者服务</div>
      <div class="nav">
        <el-button text @click="$router.push('/patient/home')">首页</el-button>
        <el-button text @click="$router.push('/patient/booking')">挂号</el-button>
        <el-button text @click="$router.push('/patient/register')">注册</el-button>
        <el-divider direction="vertical" />
        <el-button text @click="$router.push('/login')">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts"></script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f8fafc;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.brand {
  font-weight: 700;
}

.nav :deep(.el-button + .el-button) {
  margin-left: 8px;
}
</style>
