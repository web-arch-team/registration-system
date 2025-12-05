<template>
  <div class="page">
    <section class="hero">
      <div class="text">
        <p class="eyebrow">Patient</p>
        <h2>您好，这里是挂号服务首页</h2>
        <p class="sub">
          先完成账号注册，再按“科室 → 疾病 → 排班 → 选择时段”完成预约挂号。
        </p>
        <div class="actions">
          <el-button type="primary" @click="$router.push('/patient/booking')">开始挂号</el-button>
          <el-button text @click="$router.push('/patient/register')">注册新账号</el-button>
        </div>
      </div>
      <el-card shadow="never" class="guide">
        <div class="card-title">就诊流程</div>
        <ol>
          <li>注册账号，完善身份证/手机号等信息</li>
          <li>选择科室与疾病，查看本周排班</li>
          <li>锁定医生与时间段，提交挂号</li>
          <li>就诊当天凭手机号/身份证核验</li>
        </ol>
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
  background: linear-gradient(135deg, #fef6e4, #ffffff);
  border: 1px solid #faecd8;
}

.eyebrow {
  font-size: 13px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #e6a23c;
  margin: 0;
}

.sub {
  color: #606266;
  margin: 8px 0 14px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.guide {
  border-radius: 12px;
  background: #fffdf6;
}

.card-title {
  font-weight: 600;
  margin-bottom: 6px;
}

ol {
  margin: 0;
  padding-left: 18px;
  color: #606266;
}
</style>
