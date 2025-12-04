<template>
  <el-container style="min-height: 100vh">
    <el-aside width="200px">
      <el-menu router default-active="/doctor/today">
        <el-menu-item index="/doctor/today">今日待诊</el-menu-item>
        <el-menu-item index="/doctor/schedule">我的排班</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>医生工作台</el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts"></script>

