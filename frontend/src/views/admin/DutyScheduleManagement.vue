<template>
    <div class="page">
      <div class="header">
        <div>
          <h2>周末值班管理</h2>
          <p class="sub">管理科室周六、周日值班安排，每天分早班、中班、夜班各1名医生</p>
        </div>
      </div>

      <el-form :inline="true" class="filter-form" style="margin: 16px 0;">
        <el-form-item label="选择科室">
          <el-select v-model="selectedDeptId" placeholder="请选择科室" filterable clearable style="min-width:260px" @change="handleDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!selectedDeptId" @click="openAddDialog">新增值班</el-button>
          <el-button type="danger" :disabled="!selectedDeptId || loading" @click="handleBatchDelete" style="margin-left:8px">清空该科室值班</el-button>
        </el-form-item>
      </el-form>

    <!-- 值班表格 -->
    <el-card class="table-card" v-loading="loading">
      <el-table
          :data="dutyScheduleList"
          border
          stripe
          style="width: 100%;"
      >
        <el-table-column prop="weekendType" label="值班日期" width="120">
          <template #default="scope">
            {{ scope.row.weekendType === 6 ? '周六' : '周日' }}
          </template>
        </el-table-column>
        <el-table-column prop="dutyTimeslot" label="值班时段" width="120">
          <template #default="scope">
            <span v-if="scope.row.dutyTimeslot === 'MORNING'">早班</span>
            <span v-else-if="scope.row.dutyTimeslot === 'AFTERNOON'">中班</span>
            <span v-else>夜班</span>
          </template>
        </el-table-column>
        <el-table-column label="值班医生" width="180">
          <template #default="scope">
            {{ displayDoctorName(scope.row.doctorProfile) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="200">
          <template #default="scope">
            {{ formatTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!dutyScheduleList.length && selectedDeptId && !loading" class="empty-note">该科室暂无值班</div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑值班' : '新增值班'"
        width="500px"
        destroy-on-close
    >
      <el-form
          ref="dutyFormRef"
          :model="dutyForm"
          :rules="formRules"
          label-width="100px"
      >
        <el-form-item label="值班日期" prop="weekendType">
          <el-select v-model="dutyForm.weekendType" placeholder="请选择">
            <el-option label="周六" :value="WeekendType.SATURDAY" />
            <el-option label="周日" :value="WeekendType.SUNDAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="值班时段" prop="dutyTimeslot">
          <el-select v-model="dutyForm.dutyTimeslot" placeholder="请选择">
            <el-option label="早班" :value="DutyTimeslot.MORNING" />
            <el-option label="中班" :value="DutyTimeslot.AFTERNOON" />
            <el-option label="夜班" :value="DutyTimeslot.NIGHT" />
          </el-select>
        </el-form-item>
        <el-form-item label="值班医生" prop="doctorProfileId">
          <el-select
              v-model="dutyForm.doctorProfileId"
              placeholder="请选择医生"
              filterable
          >
            <el-option
                v-for="doctor in doctors"
                :key="doctor.id"
                :label="displayDoctorName(doctor)"
                :value="doctor.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted, nextTick} from 'vue';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import dayjs from 'dayjs';
import {
  WeekendType,
  DutyTimeslot,
  DutyScheduleDTO,
  getDutyScheduleByDeptId,
  addDutySchedule,
  updateDutySchedule,
  deleteDutySchedule,
  batchDeleteDutyByDeptId,
  fetchDepartments,
  fetchDoctorsByDeptId,
  DoctorDutyScheduleVO,
} from '@/api/admin-duty-schedule';
import type { Department, DoctorProfile } from '@/api/admin-schedule';

// helper: format doctor label safely (some API responses may not contain `title`)
const doctorLabel = (d: DoctorProfile | any) => `${d?.name ?? ''}${d && d.title ? ' ' + d.title : ''}`;
const displayDoctorName = (d: DoctorProfile | any) => doctorLabel(d);
// 状态定义
const loading = ref(false);
const selectedDeptId = ref<number | null>(null);
const departments = ref<Department[]>([]);
const doctors = ref<DoctorProfile[]>([]);
const dutyScheduleList = ref<DoctorDutyScheduleVO[]>([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const dutyFormRef = ref<InstanceType<typeof ElForm>>();

// 表单数据
const dutyForm = reactive<DutyScheduleDTO>({
  departmentId: 0,
  doctorProfileId: 0,
  weekendType: WeekendType.SATURDAY,
  dutyTimeslot: DutyTimeslot.MORNING,
});

// 表单校验规则
const formRules = ref({
  weekendType: [{ required: true, message: '请选择值班日期', trigger: 'change' }],
  dutyTimeslot: [{ required: true, message: '请选择值班时段', trigger: 'change' }],
  doctorProfileId: [{ required: true, message: '请选择值班医生', trigger: 'change' }],
});

// 初始化科室列表
onMounted(async () => {
  try {
    const list = await fetchDepartments();
    departments.value = list || [];
    // 若存在科室，自动选中第一个并加载其医生和值班记录
    if (departments.value.length > 0) {
      selectedDeptId.value = departments.value[0].id ?? null;
      await handleDeptChange(selectedDeptId.value as number);
    }
  } catch (error) {
    ElMessage.error('加载科室列表失败');
    console.error(error);
  }
});

// 格式化时间
const formatTime = (timeStr: string) => {
  return dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss');
};

// helper to refresh current dept data if a department is selected
const refreshIfSelected = async () => {
  if (!selectedDeptId.value) return;
  await handleDeptChange(selectedDeptId.value);
};

// 切换科室：加载医生列表和值班记录
const handleDeptChange = async (deptId: number) => {
  if (!deptId) return;
  loading.value = true;
  try {
    // 加载科室医生
    const doctorRes = await fetchDoctorsByDeptId(deptId);
    // 支持返回两种格式：ResultDTO 或 直接数组
    if (Array.isArray(doctorRes)) {
      doctors.value = doctorRes as any;
    } else if (doctorRes && Array.isArray((doctorRes as any).data)) {
      doctors.value = (doctorRes as any).data;
    } else {
      doctors.value = [];
    }

    // 若存在医生，预设表单中的 doctorProfileId 为第一个医生的 id（避免默认 0）
    dutyForm.doctorProfileId = doctors.value.length > 0 ? doctors.value[0].id : 0;

    // 加载值班记录（同样兼容两种返回格式）
    const dutyRes = await getDutyScheduleByDeptId(deptId);
    if (Array.isArray(dutyRes)) {
      dutyScheduleList.value = dutyRes as any;
    } else if (dutyRes && Array.isArray((dutyRes as any).data)) {
      dutyScheduleList.value = (dutyRes as any).data;
    } else {
      dutyScheduleList.value = [];
    }
  } catch (error) {
    ElMessage.error('加载数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false;
  // 重置表单
  dutyForm.id = undefined;
  dutyForm.departmentId = Number(selectedDeptId.value);
  // 预设医生为当前科室第一个医生（如果存在），避免默认 0
  dutyForm.doctorProfileId = doctors.value && doctors.value.length > 0 ? doctors.value[0].id : 0;
  dutyForm.weekendType = WeekendType.SATURDAY;
  dutyForm.dutyTimeslot = DutyTimeslot.MORNING;
  dialogVisible.value = true;
  // 重置校验状态
  nextTick(() => {
    dutyFormRef.value?.clearValidate();
  });
};

// 打开编辑弹窗
const openEditDialog = (row: DoctorDutyScheduleVO) => {
  isEdit.value = true;
  // 赋值表单
  dutyForm.id = row.id;
  dutyForm.departmentId = row.department.id;
  dutyForm.doctorProfileId = row.doctorProfile.id;
  dutyForm.weekendType = row.weekendType as WeekendType;
  dutyForm.dutyTimeslot = row.dutyTimeslot as DutyTimeslot;
  dialogVisible.value = true;
  // 重置校验状态
  nextTick(() => {
    dutyFormRef.value?.clearValidate();
  });
};

// 提交表单（新增/编辑）
const handleSubmit = async () => {
  try {
    // 表单校验
    await dutyFormRef.value?.validate();
    // 构建 payload：将 numeric WeekendType 转为字符串枚举名，例如 6 -> 'SATURDAY'
    const payload: any = {
      id: dutyForm.id,
      departmentId: Number(dutyForm.departmentId),
      doctorProfileId: Number(dutyForm.doctorProfileId),
      // WeekendType is a numeric enum in TS, convert to its key name
      weekendType: (WeekendType as any)[dutyForm.weekendType] ?? dutyForm.weekendType,
      dutyTimeslot: dutyForm.dutyTimeslot,
    };
    // 提交请求
    if (isEdit.value) {
      await updateDutySchedule(payload);
      ElMessage.success('编辑值班记录成功');
    } else {
      await addDutySchedule(payload);
      ElMessage.success('新增值班记录成功');
    }
    // 关闭弹窗+刷新数据
    dialogVisible.value = false;
    await refreshIfSelected();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
    console.error(error);
  }
};

// 删除单条值班记录
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该值班记录吗？', '提示', {
      type: 'warning',
    });
    await deleteDutySchedule(id);
    ElMessage.success('删除成功');
    await refreshIfSelected();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
      console.error(error);
    }
  }
};

// 批量删除科室所有值班记录
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要清空该科室所有值班记录吗？此操作不可恢复！', '警告', {
      type: 'error',
    });
    if (!selectedDeptId.value) {
      ElMessage.warning('请先选择科室');
      return;
    }
    await batchDeleteDutyByDeptId(selectedDeptId.value);
    ElMessage.success('清空成功');
    await refreshIfSelected();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '清空失败');
      console.error(error);
    }
  }
};
</script>

<style scoped>
.page { padding: 12px; }
.header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px }
.sub { margin:0; color:#606266 }
.table-card { padding:12px }
.empty-note { margin-top:12px; color:#909399 }
</style>