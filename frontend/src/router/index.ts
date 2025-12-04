        component: () => import('@/views/doctor/DoctorTodayTodoView.vue'),
      },
    ],
  },
  {
    path: '/patient',
    component: PatientLayout,
    children: [
      {
        path: 'register',
        name: 'patient-register',
        component: () => import('@/views/patient/PatientRegisterView.vue'),
      },
      {
        path: 'booking',
        name: 'patient-booking',
        component: () => import('@/views/patient/PatientBookingView.vue'),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import LoginView from '@/views/LoginView.vue';
import AdminLayout from '@/views/admin/AdminLayout.vue';
import DoctorLayout from '@/views/doctor/DoctorLayout.vue';
import PatientLayout from '@/views/patient/PatientLayout.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: 'patients',
        name: 'admin-patients',
        component: () => import('@/views/admin/PatientManagementView.vue'),
      },
      // TODO: 医生管理、排班管理
    ],
  },
  {
    path: '/doctor',
    component: DoctorLayout,
    children: [
      {
        path: 'schedule',
        name: 'doctor-schedule',
        component: () => import('@/views/doctor/DoctorScheduleView.vue'),
      },
      {
        path: 'today',
        name: 'doctor-today',

