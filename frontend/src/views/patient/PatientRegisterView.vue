<template>
  <div class="page">
    <el-card class="card" shadow="hover">
      <h2 class="title">患者注册</h2>
      <p class="sub">参考后端 `/api/auth/register` 接口，完成账号创建与档案同步。</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="身份证" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="手机号" prop="phoneNumber">
              <el-input v-model="form.phoneNumber" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model.number="form.age" type="number" min="0" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio-button label="male">male</el-radio-button>
                <el-radio-button label="female">female</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="actions">
          <el-button type="primary" :loading="submitting" @click="onSubmit">注册</el-button>
          <el-button @click="goLogin">返回登录</el-button>
        </div>
      </el-form>
      <el-alert v-if="resultText" :title="resultText" type="success" show-icon class="mt-12" :closable="false" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { registerPatient } from '@/api/auth';

const router = useRouter();
const formRef = ref<FormInstance>();
const submitting = ref(false);
const resultText = ref('');

const form = reactive({
  username: '',
  password: '',
  name: '',
  idCard: '',
  phoneNumber: '',
  age: undefined as number | undefined,
  gender: 'male',
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  phoneNumber: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
};

function goLogin() {
  router.push('/login');
}

async function onSubmit() {
  resultText.value = '';
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    const payload = {
      username: form.username,
      password: form.password,
      name: form.name,
      idCard: form.idCard,
      phoneNumber: form.phoneNumber,
      age: form.age ?? undefined,
      gender: form.gender as 'male' | 'female',
    };
    const res = await registerPatient(payload);
    resultText.value = `注册成功：用户名 ${res.username}，患者ID ${res.patientId}`;
    ElMessage.success('注册成功');
  } catch (error: any) {
    const msg = error?.response?.data?.message || '注册失败，请稍后再试';
    ElMessage.error(msg);
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.page {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: #f8fafc;
  min-height: 100%;
}

.card {
  width: 100%;
  max-width: 760px;
}

.title {
  margin: 0 0 6px;
}

.sub {
  margin: 0 0 16px;
  color: #606266;
}

.actions {
  margin-top: 4px;
  display: flex;
  gap: 10px;
}

.mt-12 {
  margin-top: 12px;
}
</style>
