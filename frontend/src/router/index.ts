import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

import LoginView from '@/views/LoginView.vue';
import AdminLayout from '@/views/admin/AdminLayout.vue';
import AdminHomeView from '@/views/admin/AdminHomeView.vue';
import DoctorLayout from '@/views/doctor/DoctorLayout.vue';
import DoctorHomeView from '@/views/doctor/DoctorHomeView.vue';
import PatientLayout from '@/views/patient/PatientLayout.vue';
import PatientHomeView from '@/views/patient/PatientHomeView.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', redirect: { name: 'admin-home' } },
      { path: 'home', name: 'admin-home', component: AdminHomeView },
      {
        path: 'patients',
        name: 'admin-patients',
        component: () => import('@/views/admin/PatientManagementView.vue'),
      },
      {
        path: 'doctors',
        name: 'admin-doctors',
        component: () => import('@/views/admin/DoctorManagementView.vue'),
      },
      {
        path: 'departments',
        name: 'admin-departments',
        component: () => import('@/views/admin/DepartmentManagementView.vue'),
      },
      {
        path: 'department-diseases',
        name: 'admin-department-diseases',
        component: () => import('@/views/admin/DepartmentDiseaseManagementView.vue'),
      },
      {
        path: 'schedules',
        name: 'admin-schedules',
        component: () => import('@/views/admin/ScheduleManagementView.vue'),
      },
    ],
  },
  {
    path: '/doctor',
    component: DoctorLayout,
    children: [
      { path: '', redirect: { name: 'doctor-home' } },
      { path: 'home', name: 'doctor-home', component: DoctorHomeView },
      {
        path: 'schedule',
        name: 'doctor-schedule',
        component: () => import('@/views/doctor/DoctorScheduleView.vue'),
      },
      {
        path: 'today',
        name: 'doctor-today',
        component: () => import('@/views/doctor/DoctorTodayTodoView.vue'),
      },
    ],
  },
  {
    path: '/patient',
    component: PatientLayout,
    children: [
      { path: '', redirect: { name: 'patient-home' } },
      { path: 'home', name: 'patient-home', component: PatientHomeView },
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
  { path: '/:pathMatch(.*)*', redirect: '/login' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
