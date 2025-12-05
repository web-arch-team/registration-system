<template>
  <div class="page">
    <section class="hero">
      <div class="hero-text">
        <p class="eyebrow">Administrator</p>
        <h2>欢迎来到挂号系统管理台</h2>
        <p class="sub">
          统一入口管理患者档案、医生信息与排班。先从核心任务开始，后续再按模块细化。
        </p>
        <div class="actions">
          <el-button type="primary" @click="$router.push('/admin/patients')">进入病人管理</el-button>
          <el-button text @click="$router.push('/login')">切换账号</el-button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="panel-title">今日待办</div>
        <ul>
          <li>核对新注册患者信息</li>
          <li>为新医生分配科室/疾病</li>
          <li>调整一周排班，保持时段均衡</li>
        </ul>
      </div>
    </section>

    <el-row :gutter="16" class="cards">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover">
          <div class="card-title">病人管理</div>
          <p class="card-sub">查询 / 新增 / 编辑 / 停用患者账号</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover">
          <div class="card-title">医生管理</div>
          <p class="card-sub">分配科室、疾病，可随时软删除或恢复</p>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover">
          <div class="card-title">排班管理</div>
          <p class="card-sub">按 weekday + timeslot 管理一周班表</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.page {
  padding: 16px 20px 32px;
}

.hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: radial-gradient(circle at 20% 20%, #f2f7ff, #ffffff 45%), #f7f9fb;
  border: 1px solid #e8eef7;
}

.hero-text h2 {
  margin: 8px 0;
  font-size: 24px;
}

.eyebrow {
  font-size: 13px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #409eff;
  margin: 0;
}

.sub {
  color: #606266;
  margin: 0 0 12px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-panel {
  border: 1px dashed #d6e4ff;
  border-radius: 12px;
  padding: 14px;
  background: #fff;
}

.panel-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.hero-panel ul {
  margin: 0;
  padding-left: 16px;
  color: #606266;
}

.cards {
  margin-top: 18px;
}

.card-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.card-sub {
  margin: 0;
  color: #666;
}
</style>
