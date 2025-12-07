<template>
  <div class="page">
    <el-card class="card" shadow="hover">
      <h2 class="title">我的周末值班</h2>

      <div v-if="loading">加载中...</div>

      <template v-else>
        <el-table :data="duties" style="width: 100%" v-if="duties && duties.length">
          <el-table-column label="值班星期" width="120">
            <template #default="{ row }">{{ formatWeekendType(row.weekendType) }}</template>
          </el-table-column>
          <el-table-column label="值班时段" width="120">
            <template #default="{ row }">{{ formatTimeslot(row.dutyTimeslot) }}</template>
          </el-table-column>
          <el-table-column label="科室">
            <template #default="{ row }">{{ row.departmentName || '-' }}</template>
          </el-table-column>
        </el-table>

        <div v-else>暂无周末值班信息</div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { getDoctorProfileByUserId, fetchDoctorWeekendDuties } from '@/api/doctor-profile';
import type { DoctorDutySimpleDTO } from '@/views/doctor/types';

const duties = ref<DoctorDutySimpleDTO[]>([]);
const loading = ref(false);
const auth = useAuthStore();

function formatWeekendType(w?: number) {
  if (w === 6) return '周六';
  if (w === 7) return '周日';
  return '';
}
function formatTimeslot(t?: string) {
  if (!t) return '';
  if (t === 'MORNING') return '早班';
  if (t === 'AFTERNOON') return '中班';
  if (t === 'NIGHT') return '夜班';
  return t;
}

onMounted(async () => {
  auth.restore();
  if (!auth.user) return;
  loading.value = true;
  try {
    const profile = await getDoctorProfileByUserId(auth.user.userId!);
    if (profile && profile.id) {
      const ds = await fetchDoctorWeekendDuties(profile.id);
      duties.value = (ds as unknown as DoctorDutySimpleDTO[]) ?? [];
    }
  } catch (e) {
    console.warn('load doctor weekend duties failed', e);
    duties.value = [];
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.page { padding: 24px; display:flex; justify-content:center; background:#f8fafc; }
.card { width:100%; max-width:900px }
.title { margin:0 0 6px }
</style>
