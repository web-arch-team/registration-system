<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>医生管理</h2>
        <p class="sub">维护医生账号、科室、可诊断疾病，支持软删除。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增医生</el-button>
    </div>

    <!-- 过滤表单 -->
    <div style="margin-bottom:12px;">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="ID">
          <el-input v-model.number="filters.id" style="width:120px" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="filters.doctorId" style="width:140px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filters.name" style="width:140px" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="filters.gender" placeholder="全部" style="width:120px">
            <el-option label="全部" :value="''" />
            <el-option label="男" value="male" />
            <el-option label="女" value="female" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="filters.title" style="width:160px" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="filters.departmentId" placeholder="全部" filterable clearable style="min-width:260px">
            <el-option :key="0" label="全部" :value="0" />
            <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="已删除">
          <el-select v-model="filters.deleted" placeholder="全部" style="width:120px">
            <el-option label="全部" :value="null" />
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="doctors" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="doctorId" label="工号" width="140" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 'male' ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="departmentName" label="科室" min-width="140" />
      <el-table-column prop="diseaseIds" label="疾病数" width="90">
        <template #default="{ row }">
          {{ row.diseaseIds?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="isActive" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该医生？（逻辑删除）" @confirm="onDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="密码" prop="password" v-if="!isEdit">
              <el-input v-model="form.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="密码" prop="password" v-else>
              <el-input v-model="form.password" type="password" show-password placeholder="留空则不改" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="工号" prop="doctorId">
              <el-input v-model="form.doctorId" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model.number="form.age" type="number" min="0" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio-button label="male">男</el-radio-button>
                <el-radio-button label="female">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="职称" prop="title">
              <el-input v-model="form.title" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="科室" prop="departmentId">
              <el-select v-model="form.departmentId" placeholder="请选择科室" filterable clearable style="min-width:260px">
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.departmentName" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="可诊断疾病" prop="diseaseIds">
              <el-select
                v-model="form.diseaseIds"
                multiple
                filterable
                collapse-tags
                placeholder="请选择疾病"
                :disabled="!form.departmentId"
              >
                <el-option
                  v-for="d in diseases"
                  :key="d.id"
                  :label="d.name"
                  :value="d.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" v-if="isEdit">
            <el-form-item label="状态">
              <el-switch v-model="form.isActive" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import {
  fetchDoctors,
  searchDoctors,
  createDoctor,
  updateDoctor,
  deleteDoctor,
  fetchDepartments,
  fetchDiseasesByDepartment,
  type Doctor,
  type Department,
  type Disease,
} from '@/api/admin-doctors';

const loading = ref(false);
const saving = ref(false);
const doctors = ref<Doctor[]>([]);

const departments = ref<Department[]>([]);
const diseases = ref<Disease[]>([]);

const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const form = reactive<Doctor>({
  username: '',
  password: '',
  doctorId: '',
  name: '',
  age: undefined,
  gender: 'male',
  title: '',
  departmentId: 0,
  diseaseIds: [],
  isActive: true,
});

const isEdit = computed(() => Boolean(form.id));
const dialogTitle = computed(() => (isEdit.value ? '编辑医生' : '新增医生'));

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', validator: (_, value, cb) => {
    if (isEdit.value && !value) return cb();
    if (!value) return cb(new Error('请输入密码'));
    cb();
  } }],
  doctorId: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
};

const paging = reactive({ page: 0, size: 20, total: 0 });

const filters = reactive<any>({
  id: undefined,
  doctorId: '',
  name: '',
  gender: '',
  title: '',
  departmentId: 0,
  deleted: null,
});

watch(
  () => form.departmentId,
  async (deptId) => {
    if (!deptId) {
      diseases.value = [];
      form.diseaseIds = [];
      return;
    }
    try {
      diseases.value = await fetchDiseasesByDepartment(deptId);
      // 如果当前选中的疾病不属于该科室，需清空
      form.diseaseIds = form.diseaseIds?.filter((id) => diseases.value.some((d) => d.id === id)) || [];
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.message || '加载疾病失败');
    }
  },
);

async function load() {
  loading.value = true;
  try {
    const [list, depts] = await Promise.all([searchDoctors({ page: paging.page, size: paging.size }), fetchDepartments()]);
    // normalize boolean property (backend may use `active` or `isActive`)
    doctors.value = list.content.map((d: any) => ({ ...d, isActive: typeof d.isActive === 'boolean' ? d.isActive : !!d.active }));
    paging.total = list.totalElements;
    departments.value = depts;
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    doctorId: '',
    name: '',
    age: undefined,
    gender: 'male' as const,
    title: '',
    departmentId: 0,
    diseaseIds: [] as number[],
    isActive: true,
  });
  diseases.value = [];
  dialogVisible.value = true;
}

function openEdit(row: Doctor) {
  Object.assign(form, row, { password: '' }); // 编辑时密码留空表示不修改
  dialogVisible.value = true;
  // 预先加载该科室的疾病
  if (row.departmentId) {
    fetchDiseasesByDepartment(row.departmentId)
      .then((list) => {
        diseases.value = list;
      })
      .catch(() => {});
  }
}

async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    if (isEdit.value && form.id) {
      const payload = { ...form };
      if (!payload.password) delete (payload as any).password; // 不改密码时不传
      await updateDoctor(form.id, payload);
      ElMessage.success('更新成功');
    } else {
      await createDoctor(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    // reload with current filters
    onSearch();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function onDelete(row: Doctor) {
  if (!row.id) return;
  try {
    await deleteDoctor(row.id);
    ElMessage.success('已删除');
    onSearch();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '删除失败');
  }
}

async function onSearch() {
  loading.value = true;
  try {
    const params: any = {
      page: paging.page,
      size: paging.size,
      sort: 'id,desc',
    };
    if (filters.id) params.id = filters.id;
    if (filters.doctorId) params.doctorId = filters.doctorId;
    if (filters.name) params.name = filters.name;
    if (filters.gender) params.gender = filters.gender;
    if (filters.title) params.title = filters.title;
    if (filters.departmentId && filters.departmentId !== 0) params.departmentId = filters.departmentId;
    if (filters.deleted !== null) params.deleted = filters.deleted;

    const res = await searchDoctors(params);
    doctors.value = res.content.map((d: any) => ({ ...d, isActive: typeof d.isActive === 'boolean' ? d.isActive : !!d.active }));
    paging.total = res.totalElements;
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '查询失败');
  } finally {
    loading.value = false;
  }
}

function onReset() {
  Object.assign(filters, { id: undefined, doctorId: '', name: '', gender: '', title: '', departmentId: 0, deleted: null });
  paging.page = 0;
  onSearch();
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
</style>
