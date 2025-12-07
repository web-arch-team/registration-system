<template>
  <div class="page">
    <el-card class="card" shadow="hover">
      <div class="header">
        <h2>值班医生</h2>
        <div>
          <el-button type="text" @click="fetchList">刷新</el-button>
        </div>
      </div>

      <div style="margin-bottom:12px; display:flex; gap:8px; align-items:center">
        <el-select v-model="selectedDept" placeholder="全部科室" clearable style="min-width:220px">
          <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
        </el-select>

        <el-select v-model="selectedWeekend" placeholder="全部星期" clearable style="width:140px">
          <el-option :label="'周六'" :value="6" />
          <el-option :label="'周日'" :value="7" />
        </el-select>

        <el-select v-model="selectedTimeslot" placeholder="全部时段" clearable style="width:140px">
          <el-option label="早班" value="MORNING" />
          <el-option label="中班" value="AFTERNOON" />
          <el-option label="夜班" value="NIGHT" />
        </el-select>

        <el-button type="primary" @click="applyFilters">筛选</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <div class="meta" v-if="list.length">共 {{ list.length }} 条</div>
      <template v-if="list.length">
        <el-table :data="list" style="width: 100%">
          <el-table-column prop="departmentName" label="科室" />
          <el-table-column label="值班星期" :formatter="formatWeekend" />
          <el-table-column label="值班时段" :formatter="formatTimeslot" />
          <el-table-column prop="doctorName" label="医生">
            <template #default="{ row }">
              {{ row.doctorName }}{{ row.doctorTitle ? ' ' + row.doctorTitle : '' }}
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template v-else>
        <div class="empty-note">
          <el-empty description="当前暂无值班医生安排。" />
        </div>
      </template>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { listDutySchedules, listDutySchedulesAll } from '@/api/patient-duty';
import { fetchDepartments } from '@/api/admin-departments';

const list = ref<any[]>([]);
const departments = ref<any[]>([]);
const selectedDept = ref<number | null>(null);
const selectedWeekend = ref<number | null>(null);
const selectedTimeslot = ref<string | null>(null);

function formatWeekend(row: any) {
  const w = Number(row?.weekendType);
  if (Number.isNaN(w)) return '';
  if (w === 6) return '周六';
  if (w === 7) return '周日';
  return String(w);
}

function formatTimeslot(row: any) {
  const t = row?.dutyTimeslot;
  if (!t) return '';
  if (t === 'MORNING') return '早班';
  if (t === 'AFTERNOON') return '中班';
  if (t === 'NIGHT') return '夜班';
  return String(t);
}

onMounted(async () => {
  await fetchList();
  await loadDepartments();
});

async function fetchList() {
  try {
    const filters: any = {};
    if (selectedDept.value) filters.departmentId = selectedDept.value;
    if (selectedWeekend.value) filters.weekendType = selectedWeekend.value;
    if (selectedTimeslot.value) filters.dutyTimeslot = selectedTimeslot.value;
    const hasFilters = Object.keys(filters).length > 0;
    console.debug('fetchList - sending filters:', filters, 'hasFilters:', hasFilters);
    const data = await listDutySchedules(hasFilters ? filters : undefined);
    console.debug('duty schedules fetched', data, 'filters:', filters);
    list.value = data ?? [];
    // Only fallback to /all when there were no filters (unfiltered root request may 404 in some deployments)
    if (!hasFilters && (!list.value || list.value.length === 0)) {
      try {
        const all = await listDutySchedulesAll();
        console.debug('duty schedules fallback /all fetched', all);
        if (all && all.length > 0) {
          list.value = all;
          // @ts-ignore
          ElMessage?.info('已回退到全部值班数据（可能存在筛选/鉴权问题）');
        }
      } catch (e2) {
        console.debug('fallback /all failed', e2);
      }
    }
  } catch (e: any) {
    console.error('fetch duty schedules error', e);
    list.value = [];
    // show user-facing message
    const msg = e?.response?.data?.message || '获取值班信息失败';
    // Use Element Plus global message if available
    // @ts-ignore
    ElMessage?.error(msg);
  }
}

async function loadDepartments() {
  try {
    const ds = await fetchDepartments();
    departments.value = ds || [];
  } catch (e) {
    console.warn('load departments failed', e);
    departments.value = [];
  }
}

function applyFilters() {
  fetchList();
}

function resetFilters() {
  selectedDept.value = null;
  selectedWeekend.value = null;
  selectedTimeslot.value = null;
  fetchList();
}
</script>

<style scoped>
.page { padding: 24px; background: #f8fafc; }
.card { width: 100%; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px }
.empty-note { text-align: center; padding: 50px 0; }
</style>
