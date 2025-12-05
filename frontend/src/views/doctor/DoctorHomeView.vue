<template>
  <div class="page">
    <section class="hero">
      <div>
        <p class="eyebrow">Doctor</p>
        <h2>欢迎回来，医生</h2>
        <p class="sub">查看今日待诊与本周排班，快速确认自己的行程。</p>
        <div class="actions">
          <el-button type="primary" @click="$router.push('/doctor/today')">查看今日待诊</el-button>
          <el-button text @click="$router.push('/login')">切换账号</el-button>
        </div>
      </div>
      <el-card shadow="never" class="checklist">
        <div class="card-title">工作小提示</div>
        <ul>
          <li>今日待诊列表汇总病人档案与挂号信息</li>
          <li>排班按 weekday × timeslot 展示，可提前确认</li>
          <li>如需调班，请联系管理员调整排班表</li>
        </ul>
      </el-card>
    </section>
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
  background: linear-gradient(120deg, #f3f8ff, #ffffff);
  border: 1px solid #e8eef7;
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
  margin: 6px 0 12px;
}

.actions {
  display: flex;
  gap: 12px;
}

.checklist {
  border-radius: 12px;
}

.card-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.checklist ul {
  margin: 0;
  padding-left: 16px;
  color: #606266;
}
</style>
