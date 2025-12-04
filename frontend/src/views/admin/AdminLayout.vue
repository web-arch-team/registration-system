<template>
  <el-container style="min-height: 100vh">
    <el-aside width="200px">
      <el-menu router default-active="/admin/patients">
        <el-menu-item index="/admin/patients">病人管理</el-menu-item>
        <el-menu-item index="/admin/doctors">医生管理（TODO）</el-menu-item>
        <el-menu-item index="/admin/schedule">排班管理（TODO）</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>管理员后台</el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts"></script>

