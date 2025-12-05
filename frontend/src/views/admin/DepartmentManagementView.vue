<template>
  <div class="page">
    <div class="header">
      <div>
        <h2>科室管理</h2>
        <p class="sub">新增、编辑或删除科室，删除前会检查是否有关联疾病。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增科室</el-button>
    </div>

    <el-table :data="departments" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="departmentName" label="科室名称" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该科室？" @confirm="onDelete(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="科室名称" prop="departmentName" :rules="[{ required: true, message: '请输入科室名称', trigger: 'blur' }]">
          <el-input v-model="form.departmentName" />
        </el-form-item>

        <el-divider>科室相关疾病（可选）</el-divider>

        <el-table :data="form.diseases" style="width:100%" size="small" border>
          <el-table-column prop="name" label="名称">
            <template #default="{ row }">
              <el-input v-model="row.name" placeholder="疾病名称" />
            </template>
          </el-table-column>
          <el-table-column prop="code" label="编码" width="180">
            <template #default="{ row }">
              <el-input v-model="row.code" placeholder="编码（可选）" />
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述">
            <template #default="{ row }">
              <el-input v-model="row.description" placeholder="描述（可选）" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="removeDiseaseByRow(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top:12px;">
          <el-button type="primary" @click="addDisease">新增一行疾病</el-button>
        </div>

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
import { ElMessage } from 'element-plus';
import {
  fetchDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  fetchDepartment,
  type Department,
} from '@/api/admin-departments';

const loading = ref(false);
const saving = ref(false);
const departments = ref<Department[]>([]);

const dialogVisible = ref(false);
const formRef = ref();
const form = reactive<Department>({ departmentName: '', diseases: [] as any[] });

const isEdit = computed(() => Boolean((form as any).id));
const dialogTitle = computed(() => (isEdit.value ? '编辑科室' : '新增科室'));

async function load() {
  loading.value = true;
  try {
    departments.value = await fetchDepartments();
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  Object.assign(form, { id: undefined, departmentName: '', diseases: [] } as any);
  dialogVisible.value = true;
}

async function openEdit(row: Department) {
  // fetch full details including diseases
  try {
    loading.value = true;
    const full = await fetchDepartment((row as any).id);
    Object.assign(form, full as any);
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载科室失败');
    return;
  } finally {
    loading.value = false;
  }
  dialogVisible.value = true;
}

function addDisease() {
  (form.diseases = form.diseases || []).push({ name: '', code: '', description: '' });
}

function removeDisease(idx: number) {
  form.diseases?.splice(idx, 1);
}

function removeDiseaseByRow(row: any) {
  const list = form.diseases || [];
  const idx = list.indexOf(row);
  if (idx !== -1) removeDisease(idx);
}

async function onSubmit() {
  if (!formRef.value) return;
  try {
    // simple validation
    if (!form.departmentName || !form.departmentName.trim()) {
      ElMessage.error('请输入科室名称');
      return;
    }
    saving.value = true;
    const payload = {
      departmentName: form.departmentName,
      diseases: form.diseases?.filter((d: any) => d.name && d.name.trim()).map((d: any) => ({ name: d.name, code: d.code, description: d.description })) || [],
    } as any;

    if (isEdit.value && (form as any).id) {
      await updateDepartment((form as any).id, payload);
      ElMessage.success('更新成功');
    } else {
      await createDepartment(payload);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    await load();
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function onDelete(row: Department) {
  if (!(row as any).id) return;
  try {
    await deleteDepartment((row as any).id);
    ElMessage.success('已删除');
    await load();
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败：可能存在关联疾病');
  }
}

onMounted(load);
</script>

<style scoped>
.page { padding: 12px; }
.header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px }
.sub { margin:0; color:#606266 }
</style>
