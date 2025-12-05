<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>今日待诊</h2>
        <p class="sub">按时段展示今日已预约的患者列表。</p>
      </div>
      <el-select v-model="weekday" placeholder="选择工作日" style="width: 140px" @change="load">
        <el-option v-for="d in weekdays" :key="d.value" :label="d.label" :value="d.value" />
      </el-select>
    </div>

    <el-alert
      v-if="!doctorId"
      type="warning"
      show-icon
      title="请使用医生账号登录，系统将携带 doctorId 调用接口"
      class="mb-12"
    />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="timeslot" label="时段" width="120" />
      <el-table-column prop="patientName" label="患者姓名" width="140" />
      <el-table-column prop="patientIdCard" label="身份证" />
      <el-table-column prop="patientPhone" label="手机号" width="150" />
      <el-table-column prop="patientAge" label="年龄" width="80" />
      <el-table-column prop="patientGender" label="性别" width="90" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchDoctorDaySchedule, type DoctorDayScheduleItem } from '@/api/doctor';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const doctorId = auth.user?.doctorId || '';
const loading = ref(false);
const weekday = ref<number>(new Date().getDay());
const tableData = ref<DoctorDayScheduleItem[]>([]);

const weekdays = reactive([
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
]);

async function load() {
  if (!doctorId) return;
  loading.value = true;
  try {
    const wd = weekday.value < 1 || weekday.value > 5 ? 1 : weekday.value;
    tableData.value = await fetchDoctorDaySchedule(doctorId, wd);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  // 将周末映射为周一
  if (weekday.value === 0 || weekday.value === 6) weekday.value = 1;
  load();
});
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
