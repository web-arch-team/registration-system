<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>我的排班</h2>
        <p class="sub">展示本周 weekday × timeslot 的排班信息。</p>
      </div>
      <el-button type="primary" @click="load">刷新</el-button>
    </div>

    <el-alert
      v-if="!doctorId"
      type="warning"
      show-icon
      title="请使用医生账号登录，系统将携带 doctorId 调用接口"
      class="mb-12"
    />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="weekday" label="星期" width="100">
        <template #default="{ row }">
          {{ weekdayLabel(row.weekday) }}
        </template>
      </el-table-column>
      <el-table-column prop="timeslot" label="时段" width="120" />
      <el-table-column prop="department" label="科室" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchDoctorWeekSchedule, type DoctorWeekScheduleItem } from '@/api/doctor';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const doctorId = auth.user?.doctorId || '';
const loading = ref(false);
const tableData = ref<DoctorWeekScheduleItem[]>([]);

function weekdayLabel(day: number) {
  const map: Record<number, string> = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五' };
  return map[day] || `周${day}`;
}

async function load() {
  if (!doctorId) return;
  loading.value = true;
  try {
    tableData.value = await fetchDoctorWeekSchedule(doctorId);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.page {
  padding: 12px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.sub {
  margin: 0;
  color: #606266;
}

.mb-12 {
  margin-bottom: 12px;
}
</style>
