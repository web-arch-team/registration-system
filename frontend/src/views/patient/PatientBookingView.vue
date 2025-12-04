<template>
  <div class="page">
    <h2 class="title">挂号排班查询</h2>
    <p class="sub">科室 → 疾病 → 查询可诊疗医生的 8×5 排班表，选择一个医生+时段进行挂号。</p>

    <el-row :gutter="12" class="mt-12">
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="panel">
          <div class="panel-title">1. 选择科室</div>
          <el-skeleton :loading="loading.departments" animated>
            <el-select
              v-model="selected.departmentId"
              placeholder="请选择科室"
              filterable
              style="width: 100%"
              @change="onDepartmentChange"
            >
              <el-option v-for="dept in departments" :key="dept.id" :label="dept.departmentName" :value="dept.id" />
            </el-select>
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="panel">
          <div class="panel-title">2. 选择疾病</div>
          <el-skeleton :loading="loading.diseases" animated>
            <el-select
              v-model="selected.diseaseId"
              placeholder="请先选择科室"
              filterable
              style="width: 100%"
              :disabled="!selected.departmentId"
              @change="onDiseaseChange"
            >
              <el-option v-for="d in diseases" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="panel">
          <div class="panel-title">3. 选择星期</div>
          <el-radio-group v-model="selected.weekday" size="small" @change="loadTimetable">
            <el-radio-button v-for="d in weekdays" :key="d.value" :label="d.value">{{ d.label }}</el-radio-button>
          </el-radio-group>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel mt-12">
      <div class="panel-title">排班表（时段 × 星期）</div>
      <el-alert
        v-if="!selected.diseaseId"
        type="info"
        show-icon
        title="请选择科室和疾病"
        class="mb-12"
      />
      <div v-else v-loading="loading.timetable">
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="timeslotLabel" label="时段" width="140" fixed />
          <el-table-column
            v-for="day in weekdays"
            :key="day.value"
            :label="day.label"
            :prop="day.value.toString()"
          >
            <template #default="{ row }">
              <div class="cell" v-if="row.slots[day.value]?.length">
                <el-tag
                  v-for="slot in row.slots[day.value]"
                  :key="slot.key"
                  :type="slot.available ? 'success' : 'info'"
                  class="slot-tag"
                  @click="selectSlot(slot)"
                >
                  {{ slot.doctorName }}
                  <span v-if="slot.doctorTitle"> / {{ slot.doctorTitle }}</span>
                  <br />
                  <small>剩余 {{ slot.remain }}/{{ slot.max }}</small>
                </el-tag>
              </div>
              <div v-else class="empty-cell">--</div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card shadow="never" class="panel mt-12">
      <div class="panel-title">已选择</div>
      <el-empty v-if="!selectedSlot" description="请在排班表中选择医生和时段" />
      <div v-else class="selection">
        <p>医生：{{ selectedSlot.doctorName }}（{{ selectedSlot.doctorId }}）</p>
        <p>星期：{{ weekdayLabel(selectedSlot.weekday) }} / 时段：{{ selectedSlot.timeslot }}</p>
        <el-button type="primary" @click="onSubmit">提交挂号（示例）</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchDepartments,
  fetchDiseasesByDepartment,
  fetchDiseaseTimetable,
  type Department,
  type Disease,
  type DiseaseTimetableItem,
} from '@/api/patient-schedule';

const departments = ref<Department[]>([]);
const diseases = ref<Disease[]>([]);
const timetableRaw = ref<DiseaseTimetableItem[]>([]);

const loading = reactive({
  departments: false,
  diseases: false,
  timetable: false,
});

const selected = reactive<{
  departmentId: number | null;
  diseaseId: number | null;
  weekday: number;
}>({
  departmentId: null,
  diseaseId: null,
  weekday: 1,
});

const weekdays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
];

const timeslots = [
  { code: 'AM1', label: 'AM1 早晨' },
  { code: 'AM2', label: 'AM2 上午' },
  { code: 'AM3', label: 'AM3 上午' },
  { code: 'AM4', label: 'AM4 上午' },
  { code: 'PM1', label: 'PM1 午后' },
  { code: 'PM2', label: 'PM2 下午' },
  { code: 'PM3', label: 'PM3 下午' },
  { code: 'PM4', label: 'PM4 傍晚' },
];

const tableData = computed(() =>
  timeslots.map((slot) => {
    const slotsByDay: Record<number, Array<{
      key: string;
      doctorId: string;
      doctorName: string;
      doctorTitle?: string;
      weekday: number;
      timeslot: string;
      remain: number;
      max: number;
      available: boolean;
    }>> = {};

    weekdays.forEach((day) => {
      const items = timetableRaw.value.filter(
        (t) => t.timeslot === slot.code && t.weekday === day.value,
      );
      slotsByDay[day.value] = items.map((t, idx) => ({
        key: `${t.doctorProfileId}-${t.timeslot}-${day.value}-${idx}`,
        doctorId: t.doctorId,
        doctorName: t.doctorName,
        doctorTitle: t.doctorTitle || '',
        weekday: day.value,
        timeslot: t.timeslot,
        remain: (t.maxPatients ?? 16) - (t.currentPatients ?? 0),
        max: t.maxPatients ?? 16,
        available: t.available ?? true,
      }));
    });

    return {
      timeslot: slot.code,
      timeslotLabel: slot.label,
      slots: slotsByDay,
    };
  }),
);

const selectedSlot = ref<{
  doctorId: string;
  doctorName: string;
  weekday: number;
  timeslot: string;
} | null>(null);

function weekdayLabel(day: number) {
  const map: Record<number, string> = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五' };
  return map[day] || `周${day}`;
}

async function loadDepartments() {
  loading.departments = true;
  try {
    departments.value = await fetchDepartments();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载科室失败');
  } finally {
    loading.departments = false;
  }
}

async function onDepartmentChange(deptId: number) {
  selected.diseaseId = null;
  timetableRaw.value = [];
  diseases.value = [];
  if (!deptId) return;
  loading.diseases = true;
  try {
    diseases.value = await fetchDiseasesByDepartment(deptId);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载疾病失败');
  } finally {
    loading.diseases = false;
  }
}

async function onDiseaseChange(diseaseId: number) {
  timetableRaw.value = [];
  selectedSlot.value = null;
  if (!diseaseId) return;
  await loadTimetable();
}

async function loadTimetable() {
  timetableRaw.value = [];
  selectedSlot.value = null;
  if (!selected.diseaseId) return;
  loading.timetable = true;
  try {
    // 按当前 weekday 查询；如需全周，传 undefined
    timetableRaw.value = await fetchDiseaseTimetable(selected.diseaseId, selected.weekday);
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载排班失败');
  } finally {
    loading.timetable = false;
  }
}

function selectSlot(slot: {
  doctorId: string;
  doctorName: string;
  weekday: number;
  timeslot: string;
}) {
  selectedSlot.value = { ...slot };
}

function onSubmit() {
  if (!selectedSlot.value || !selected.diseaseId) {
    ElMessage.info('请先选择一个医生的时段');
    return;
  }
  // TODO: 接入挂号提交接口（未提供）。这里先做提示。
  ElMessage.success(
    `已选择 ${selectedSlot.value.doctorName} - ${weekdayLabel(selectedSlot.value.weekday)} ${selectedSlot.value.timeslot}，后续对接挂号提交接口`,
  );
}

onMounted(() => {
  const today = new Date().getDay();
  selected.weekday = today >= 1 && today <= 5 ? today : 1;
  loadDepartments();
});
</script>

<style scoped>
.page {
  padding: 12px;
}

.title {
  margin: 0;
}

.sub {
  color: #606266;
  margin: 4px 0 0;
}

.mt-12 {
  margin-top: 12px;
}

.panel {
  min-height: 120px;
}

.panel-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.mb-12 {
  margin-bottom: 12px;
}

.slot-tag {
  margin: 4px 0;
  cursor: pointer;
  width: 100%;
}

.cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-cell {
  color: #999;
  text-align: center;
}

.selection p {
  margin: 4px 0;
}
</style>
