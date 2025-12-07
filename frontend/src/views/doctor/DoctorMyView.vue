<template>
  <div class="page">
    <el-card class="card" shadow="hover">
      <h2 class="title">我的信息（医生）</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="医生工号" prop="doctorId">
              <el-input v-model="form.doctorId" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="职称" prop="title">
              <el-input v-model="form.title" disabled />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="18" :max="150" disabled />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-input v-model="form.gender" disabled />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="24">
            <el-form-item label="所属科室">
              <el-input v-model="form.departmentName" disabled />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="24">
            <el-form-item label="可诊治疾病">
              <div v-if="form.diseases && form.diseases.length">
                <el-tag v-for="d in form.diseases" :key="d.id" style="margin-right:6px">{{ d.name }}</el-tag>
              </div>
              <div v-else>暂未设置可治疾病</div>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="actions">
          <el-button type="primary" :loading="saving" @click="onSave">保存修改</el-button>
        </div>
      </el-form>

      <el-divider class="my-12" />

      <h3>修改密码</h3>
      <el-form :model="pwdForm" ref="pwdRef" label-position="top">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <div class="actions">
          <el-button type="primary" :loading="changingPwd" @click="onChangePwd">修改密码</el-button>
        </div>
      </el-form>

      <el-divider class="my-12" />

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { updateDoctorProfile, changeDoctorPassword, getDoctorProfileByUserId } from '@/api/doctor-profile';
import type { DoctorSelfDTO } from './types';

const auth = useAuthStore();
const formRef = ref<FormInstance>();
const pwdRef = ref<FormInstance>();
const saving = ref(false);
const changingPwd = ref(false);

const form = reactive<DoctorSelfDTO>({
  id: 0,
  username: '',
  doctorId: '',
  name: '',
  age: undefined,
  gender: 'male',
  title: '',
  departmentName: '',
  diseases: [],
});

const pwdForm = reactive({ newPassword: '' });

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
};

onMounted(async () => {
  auth.restore();
  if (!auth.user) {
    ElMessage.error('未检测到登录信息');
    return;
  }

  try {
    let data: DoctorSelfDTO | null = null;
    if (auth.user.userId) {
      try {
        const resp = await getDoctorProfileByUserId(auth.user.userId);
        data = resp ?? null;
      } catch (e2) {
        console.warn('getDoctorProfileByUserId failed', e2);
        data = null;
      }
    }

    if (data == null) {
      ElMessage.error('无法获取医生信息');
      return;
    }
    Object.assign(form, data as DoctorSelfDTO);

  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '获取个人信息失败');
  }
});

async function onSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    const payload: any = {
      username: form.username,
    };
    const res = await updateDoctorProfile(form.id!, payload);
    Object.assign(form, res);
    ElMessage.success('保存成功');
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

async function onChangePwd() {
  const valid = await pwdRef.value?.validate().catch(() => false);
  if (!valid) return;
  const profileId = form.id;
  if (!profileId) { ElMessage.error('未检测到医生档案ID，无法修改密码'); return; }
  changingPwd.value = true;
  const newPwd = pwdForm.newPassword;
  try {
    await changeDoctorPassword(profileId, newPwd);
    await ElMessageBox.alert(`新密码：${newPwd}`, '修改密码成功', {
      confirmButtonText: '去登录',
      closeOnClickModal: false,
      closeOnPressEscape: false,
    });
    auth.setUser(null);
    window.location.href = '/login';
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '修改密码失败');
  } finally {
    changingPwd.value = false;
  }
}
</script>

<style scoped>
.page { padding: 24px; display:flex; justify-content:center; background:#f8fafc; }
.card { width:100%; max-width:760px }
.title { margin:0 0 6px }
.actions { margin-top: 12px }
.my-12 { margin-top: 12px }
</style>
