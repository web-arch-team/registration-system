<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>科室疾病管理</h2>
        <p class="sub">选择科室后，可新增、编辑该科室下的疾病。</p>
      </div>
      <div>
        <el-select v-model="selectedDeptId" placeholder="请选择科室" style="width:260px" filterable @change="onDeptChange">
          <el-option v-for="d in departments" :key="d.id" :label="d.departmentName" :value="d.id" />
        </el-select>
        <el-button type="primary" plain @click="resetFilter" style="margin-left:8px">显示全部疾病</el-button>
        <el-button type="primary" @click="openCreate" :disabled="!selectedDeptId" style="margin-left:8px">新增疾病</el-button>
      </div>
    </div>

    <el-table :data="diseases" v-loading="loading" border style="background:#fff;" size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="疾病名称" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="departmentName" label="科室" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该疾病？" @confirm="onDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" ref="formRef" label-width="120px">
        <el-form-item label="疾病名称" prop="name" :rules="[{ required: true, message: '请输入疾病名称', trigger: 'blur' }]">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" />
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchDepartments,
  fetchDiseaseByDepartment,
  fetchDiseases,
  createDisease,
  updateDisease,
  deleteDisease,
  type Department,
  type Disease,
} from '@/api/admin-departments';

const loading = ref(false);
const saving = ref(false);
const departments = ref<Department[]>([]);
const diseases = ref<Disease[]>([]);

const selectedDeptId = ref<number | null>(null);

const dialogVisible = ref(false);
const formRef = ref();
const form = reactive<Disease>({ name: '', code: '', description: '', departmentId: undefined });

const isEdit = () => !!(form as any).id;
const dialogTitle = () => (isEdit() ? '编辑疾病' : '新增疾病');

async function load() {
  loading.value = true;
  try {
    departments.value = await fetchDepartments();
    diseases.value = await fetchDiseases(); // 默认加载全部疾病
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载科室失败');
  } finally {
    loading.value = false;
  }
}

async function onDeptChange() {
  if (!selectedDeptId.value) {
    diseases.value = await fetchDiseases();
    return;
  }
  loading.value = true;
  try {
    diseases.value = await fetchDiseaseByDepartment(selectedDeptId.value);
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载疾病失败');
  } finally {
    loading.value = false;
  }
}

function resetFilter() {
  selectedDeptId.value = null;
  onDeptChange();
}

function openCreate() {
  Object.assign(form, { id: undefined, name: '', code: '', description: '', departmentId: selectedDeptId.value } as any);
  dialogVisible.value = true;
}

function openEdit(row: Disease) {
  Object.assign(form, row as any);
  dialogVisible.value = true;
}

async function onSubmit() {
  if (!formRef.value) return;
  try {
    if (!form.name || !form.name.trim()) {
      ElMessage.error('请输入疾病名称');
      return;
    }
    saving.value = true;
    if (isEdit()) {
      await updateDisease((form as any).id, form as any);
      ElMessage.success('更新成功');
    } else {
      // ensure departmentId set
      await createDisease({ ...form as any, departmentId: selectedDeptId.value } as any);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    await onDeptChange();
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function onDelete(row: Disease) {
  if (!(row as any).id) return;
  try {
    await deleteDisease((row as any).id);
    ElMessage.success('已删除');
    await onDeptChange();
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败');
  }
}

onMounted(load);
</script>

<style scoped>
.page { padding: 12px }
.header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px }
.sub { margin:0; color:#606266 }
</style>
