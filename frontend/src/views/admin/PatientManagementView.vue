<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>病人管理</h2>
        <p class="sub">支持按姓名/身份证/手机号/性别查询，新增、编辑、逻辑删除。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增患者</el-button>
    </div>

    <el-form :inline="true" :model="query" class="mb-12" @submit.prevent>
      <el-form-item label="姓名">
        <el-input v-model="query.name" placeholder="模糊查询姓名" clearable />
      </el-form-item>
      <el-form-item label="身份证">
        <el-input v-model="query.idCard" placeholder="精确身份证" clearable />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="query.phoneNumber" placeholder="精确手机号" clearable />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="query.gender" placeholder="全部" clearable style="width: 120px">
          <el-option label="男" value="male" />
          <el-option label="女" value="female" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="patients" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 'male' ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="idCard" label="身份证" min-width="200" />
      <el-table-column prop="phoneNumber" label="手机号" width="140" />
      <el-table-column prop="isActive" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该患者？（逻辑删除）" @confirm="onDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, jumper, total"
        :total="total"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="身份证" prop="idCard">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="form.age" type="number" min="0" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio-button label="male">男</el-radio-button>
            <el-radio-button label="female">女</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-switch v-model="form.isActive" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import {
  fetchPatients,
  createPatient,
  updatePatient,
  deletePatient,
  type Patient,
  type PatientQuery,
} from '@/api/admin-patients';

const loading = ref(false);
const saving = ref(false);
const patients = ref<Patient[]>([]);
const total = ref(0);

const query = reactive<PatientQuery>({
  name: '',
  idCard: '',
  phoneNumber: '',
  gender: undefined,
  pageNum: 1,
  pageSize: 10,
});

const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const form = reactive<Patient>({
  username: '',
  name: '',
  idCard: '',
  phoneNumber: '',
  age: undefined,
  gender: 'male',
  isActive: true,
});

const isEdit = computed(() => Boolean(form.id));
const dialogTitle = computed(() => (isEdit.value ? '编辑患者' : '新增患者'));

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{16}(\d|X|x)$/, message: '请输入合法18位身份证号', trigger: 'blur' },
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ type: 'number', min: 0, max: 150, message: '年龄应在0-150之间', trigger: 'blur' }],
};

function resetQuery() {
  query.name = '';
  query.idCard = '';
  query.phoneNumber = '';
  query.gender = undefined;
  query.pageNum = 1;
  load();
}

async function load() {
  loading.value = true;
  try {
    const data = await fetchPatients(query);
    patients.value = data.content || [];
    total.value = data.totalElements || 0;
    // Spring Data page number is 0-based; query.pageNum is 1-based
    if (typeof data.number === 'number') {
      query.pageNum = data.number + 1;
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function onPageChange(page: number) {
  query.pageNum = page;
  load();
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    userId: undefined,
    username: '',
    name: '',
    idCard: '',
    phoneNumber: '',
    age: undefined,
    gender: 'male' as const,
    isActive: true,
  });
  dialogVisible.value = true;
}

function openEdit(row: Patient) {
  Object.assign(form, row);
  dialogVisible.value = true;
}

async function onSubmit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    if (isEdit.value && form.id) {
      await updatePatient(form.id, form);
      ElMessage.success('更新成功');
    } else {
      await createPatient(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    load();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function onDelete(row: Patient) {
  if (!row.id) return;
  try {
    await deletePatient(row.id);
    ElMessage.success('已删除');
    if (patients.value.length === 1 && query.pageNum && query.pageNum > 1) {
      query.pageNum -= 1;
    }
    load();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '删除失败');
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
