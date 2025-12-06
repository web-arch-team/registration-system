<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>排班管理</h2>
        <p class="sub">按星期与时间段管理医生排班，支持新增/编辑/删除/清空</p>
      </div>
    </div>

    <el-form :inline="true" class="filter-form" style="margin: 16px 0;">
      <el-form-item label="选择科室">
        <el-select v-model="selectedDeptId" placeholder="请选择科室" filterable clearable style="min-width:260px" @change="onDeptChange">
          <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="!selectedDeptId" @click="openCreate">新增排班</el-button>
        <el-button type="danger" :disabled="!selectedDeptId || loading" @click="confirmBatchDelete" style="margin-left:8px">清空科室排班</el-button>
      </el-form-item>
    </el-form>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="schedules" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="医生" width="220">
          <template #default="{ row }">
            {{ row.doctorProfile?.name || row.doctorProfile?.doctorId || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="星期" width="100">
          <template #default="{ row }">{{ weekdayLabel(row.weekday) }}</template>
        </el-table-column>
        <el-table-column label="时间段" width="140">
          <template #default="{ row }">{{ timeslotLabel(row.timeslot) }}</template>
        </el-table-column>
        <el-table-column label="每时段最大人数" width="160">
          <template #default="{ row }">{{ row.maxPatientsPerSlot ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该排班？" confirm-button-text="删除" @confirm="onDelete(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!schedules.length && selectedDeptId && !loading" class="empty-note">该科室暂无排班</div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitleLocal" width="520px" :destroy-on-close="true">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model.number="form.departmentId" disabled>
            <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="医生" prop="doctorProfileId">
          <el-select v-model.number="form.doctorProfileId" placeholder="请选择医生">
            <el-option v-for="doc in doctors" :key="doc.id" :label="doc.name + (doc.doctorId ? ' ('+doc.doctorId+')':'')" :value="doc.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="星期（1-5）" prop="weekday">
          <el-select v-model.number="form.weekday" placeholder="选择星期">
            <el-option v-for="n in [1,2,3,4,5]" :key="n" :label="weekdayLabel(n)" :value="n" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间段" prop="timeslot">
          <el-select v-model="form.timeslot" placeholder="选择时间段">
            <el-option v-for="ts in TIME_SLOTS" :key="ts" :label="timeslotLabel(ts)" :value="ts" />
          </el-select>
        </el-form-item>

        <el-form-item label="每时段最大人数" prop="maxPatientsPerSlot">
          <el-input-number v-model.number="form.maxPatientsPerSlot" :min="1" :controls="true" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchDepartments } from '@/api/admin-departments';
import {
  fetchSchedulesByDepartment,
  addSchedule,
  updateSchedule,
  deleteSchedule,
  batchDeleteScheduleByDept,
  batchClearScheduleByDept,
  fetchDoctorsByDepartment,
  type ScheduleDTO,
  type DoctorDepartmentSchedule,
  type DoctorProfile,
} from '@/api/admin-schedule';

const loading = ref(false);
const saving = ref(false);
const departments = ref<any[]>([]);
const doctors = ref<DoctorProfile[]>([]);
const schedules = ref<DoctorDepartmentSchedule[]>([]);

const selectedDeptId = ref<number | null>(null);

const dialogVisible = ref(false);
const formRef = ref();
const form = reactive<Partial<ScheduleDTO>>({ departmentId: undefined, doctorProfileId: undefined, weekday: 1, timeslot: 'AM1', maxPatientsPerSlot: 2 });

const TIME_SLOTS = ['AM1','AM2','AM3','AM4','PM1','PM2','PM3','PM4'];
function timeslotLabel(ts: string) {
  const map: Record<string,string> = { AM1:'上午1',AM2:'上午2',AM3:'上午3',AM4:'上午4',PM1:'下午1',PM2:'下午2',PM3:'下午3',PM4:'下午4' };
  return map[ts] || ts;
}
function weekdayLabel(n: number) { const map = ['周一','周二','周三','周四','周五']; return map[(n||1)-1] || String(n); }

const dialogTitleLocal = computed(() => form && (form as any).id ? '编辑排班' : '新增排班');

const rules = {
  doctorProfileId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  weekday: [{ required: true, message: '请选择星期', trigger: 'change' }],
  timeslot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  maxPatientsPerSlot: [{ type: 'number', min: 1, message: '人数必须 >= 1', trigger: 'change' }],
};

function getErrorMessage(err: any) {
  if (!err) return '操作失败';
  if (err.response && err.response.data) return err.response.data.msg || err.response.data.message || JSON.stringify(err.response.data);
  return err.message || String(err);
}

async function loadDepartments() {
  loading.value = true;
  try {
    departments.value = await fetchDepartments();
  } catch (err: any) {
    ElMessage.error(getErrorMessage(err));
  } finally { loading.value = false; }
}

async function onDeptChange() {
  schedules.value = [];
  doctors.value = [];
  if (!selectedDeptId.value) return;
  loading.value = true;
  try {
    const res = await fetchSchedulesByDepartment(selectedDeptId.value);
    if (res && res.code === 200) {
      // normalize schedule fields (backend may return different key styles)
      schedules.value = (res.data || []).map((s: any) => {
        const sch: any = { ...s };
        sch.maxPatientsPerSlot = s.maxPatientsPerSlot ?? s.max_patients_per_slot ?? s.maxPatients ?? 2;
        // unify timeslot
        if (s.timeslot && typeof s.timeslot === 'string') sch.timeslot = s.timeslot;
        else if (s.timeslot && s.timeslot.name) sch.timeslot = s.timeslot.name;
        else sch.timeslot = String(s.timeslot || '');
        // unify doctorProfile field
        sch.doctorProfile = s.doctorProfile || s.doctor || s.doctor_profile || null;
        // unify department
        sch.department = s.department || s.dept || s.department_name ? { id: s.department?.id ?? s.dept?.id, departmentName: s.department?.departmentName ?? s.department_name } : s.department;
        return sch;
      });
    } else throw new Error(res?.msg || '加载排班失败');

    // fetch doctors for department (public schedule API)
    try {
      const docs = await fetchDoctorsByDepartment(selectedDeptId.value);
      // normalize doctor fields
      doctors.value = (docs || []).map((d: any) => ({ id: d.id ?? d.doctorProfileId ?? d.userId, name: d.name ?? d.fullName ?? d.username, doctorId: d.doctorId ?? d.employeeNumber }));
    } catch (e) {
      doctors.value = [];
    }
  } catch (err: any) {
    ElMessage.error(getErrorMessage(err));
  } finally {
    loading.value = false;
  }
}

async function openCreate() {
  if (!selectedDeptId.value) return ElMessage.warning('请先选择科室');
  // ensure doctors loaded
  if (!doctors.value || doctors.value.length === 0) {
    try {
      const docs = await fetchDoctorsByDepartment(selectedDeptId.value);
      doctors.value = (docs || []).map((d: any) => ({ id: d.id ?? d.doctorProfileId ?? d.userId, name: d.name ?? d.fullName ?? d.username, doctorId: d.doctorId ?? d.employeeNumber }));
    } catch (e) {
      doctors.value = [];
    }
  }
  Object.assign(form, { id: undefined, departmentId: selectedDeptId.value, doctorProfileId: undefined, weekday: 1, timeslot: 'AM1', maxPatientsPerSlot: 2 });
  dialogVisible.value = true;
}

async function openEdit(row: DoctorDepartmentSchedule) {
  // set selectedDeptId so UI consistent
  if (row.department?.id) selectedDeptId.value = row.department.id;
  // ensure doctors loaded for this dept
  try {
    const docs = await fetchDoctorsByDepartment(selectedDeptId.value as number);
    doctors.value = (docs || []).map((d: any) => ({ id: d.id ?? d.doctorProfileId ?? d.userId, name: d.name ?? d.fullName ?? d.username, doctorId: d.doctorId ?? d.employeeNumber }));
  } catch (e) {
    doctors.value = [];
  }
  Object.assign(form, { id: row.id, departmentId: row.department?.id, doctorProfileId: row.doctorProfile?.id, weekday: row.weekday, timeslot: row.timeslot, maxPatientsPerSlot: row.maxPatientsPerSlot });
  dialogVisible.value = true;
}

async function submitForm() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    const payload: ScheduleDTO = {
      id: (form as any).id,
      doctorProfileId: Number(form.doctorProfileId),
      departmentId: Number(form.departmentId ?? selectedDeptId.value),
      weekday: Number(form.weekday),
      timeslot: String(form.timeslot).toUpperCase(),
      maxPatientsPerSlot: form.maxPatientsPerSlot ? Number(form.maxPatientsPerSlot) : undefined,
    };

    console.debug('[Schedule] submit payload:', payload);
    if ((form as any).id) {
      const res = await updateSchedule(payload);
      console.debug('[Schedule] update response:', res);
      if (!res || res.code !== 200) throw new Error(res?.msg || '更新失败');
      ElMessage.success('更新成功');
    } else {
      const res = await addSchedule(payload);
      console.debug('[Schedule] add response:', res);
      if (!res || res.code !== 200) throw new Error(res?.msg || '创建失败');
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    // reset form to default after success
    Object.assign(form, { id: undefined, departmentId: undefined, doctorProfileId: undefined, weekday: 1, timeslot: 'AM1', maxPatientsPerSlot: 2 });
    await onDeptChange();
  } catch (err: any) {
    console.error('[Schedule] submit error:', err);
    ElMessage.error(getErrorMessage(err));
  } finally { saving.value = false; }
}

async function onDelete(row: DoctorDepartmentSchedule) {
  try {
    await ElMessageBox.confirm('确认删除该排班？', '删除确认', { type: 'warning' });
  } catch { return; }
  try {
    const res = await deleteSchedule(row.id);
    if (!res || res.code !== 200) throw new Error(res?.msg || '删除失败');
    ElMessage.success('删除成功');
    await onDeptChange();
  } catch (err: any) { ElMessage.error(getErrorMessage(err)); }
}

async function confirmBatchDelete() {
  try {
    await ElMessageBox.confirm('确认清空该科室所有排班？此操作不可逆。', '清空确认', { type: 'warning' });
  } catch { return; }

  if (!selectedDeptId.value) return ElMessage.error('请选择科室后再操作');

  loading.value = true;
  try {
    // 首选：POST 清空（兼容性更好）
    try {
      const res = await batchClearScheduleByDept(selectedDeptId.value as number);
      if (!res || res.code !== 200) throw new Error(res?.msg || '清空失败');
      ElMessage.success('已清空该科室排班');
      await onDeptChange();
      return;
    } catch (err: any) {
      // 如果返回 HTML 错误页或其它非 JSON 响应，尝试回退到 DELETE
      // 尝试解析 HTML 错误以便展示更友好的错误信息
      if (err && err.response && err.response.headers && typeof err.response.data === 'string') {
        const ct = err.response.headers['content-type'] || err.response.headers['Content-Type'];
        if (ct && ct.indexOf && ct.indexOf('text/html') !== -1) {
          // 展示简短错误信息（避免注入大段 HTML）
          const status = err.response.status;
          ElMessage.error(`服务器返回错误（HTTP ${status}），请检查后端日志`);
          return;
        }
      }

      // 回退：尝试 DELETE 端点
      try {
        const res2 = await batchDeleteScheduleByDept(selectedDeptId.value as number);
        if (!res2 || res2.code !== 200) throw new Error(res2?.msg || '清空失败');
        ElMessage.success('已清空该科室排班（使用 DELETE 回退）');
        await onDeptChange();
        return;
      } catch (err2: any) {
        throw err2 || err;
      }
    }
  } catch (err: any) {
    console.error('[Schedule] clear error:', err);
    ElMessage.error(getErrorMessage(err));
  } finally {
    loading.value = false;
  }
}

// initial load
loadDepartments();
</script>

<style scoped>
.page { padding: 12px; }
.header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px }
.sub { margin:0; color:#606266 }
.table-card { padding:12px }
.empty-note { margin-top:12px; color:#909399 }
</style>
