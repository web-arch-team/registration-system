import http from './http';

export interface Doctor {
  id?: number;
  username: string;
  password?: string; // 仅新增或修改时传
  doctorId: string;
  name: string;
  age?: number | null;
  gender: 'male' | 'female';
  title?: string;
  departmentId: number;
  departmentName?: string;
  diseaseIds?: number[];
  isActive?: boolean;
}

export interface Department {
  id: number;
  departmentName: string;
}

export interface Disease {
  id: number;
  name: string;
  code?: string;
  description?: string;
  departmentId: number;
}

export async function fetchDoctors() {
  const { data } = await http.get<Doctor[]>('/admin/doctors');
  return data;
}

export async function fetchDoctor(id: number) {
  const { data } = await http.get<Doctor>(`/admin/doctors/${id}`);
  return data;
}

export async function createDoctor(payload: Doctor) {
  const { data } = await http.post<Doctor>('/admin/doctors', payload);
  return data;
}

export async function updateDoctor(id: number, payload: Partial<Doctor>) {
  const { data } = await http.put<Doctor>(`/admin/doctors/${id}`, payload);
  return data;
}

export async function deleteDoctor(id: number) {
  await http.delete(`/admin/doctors/${id}`);
}

// 辅助：科室/疾病下拉
export async function fetchDepartments() {
  const { data } = await http.get<Department[]>('/schedule/departments');
  return data;
}

export async function fetchDiseasesByDepartment(departmentId: number) {
  // 后端接口为 /api/schedule/department/{id}/diseases（department 为单数）
  const { data } = await http.get<Disease[]>(`/schedule/department/${departmentId}/diseases`);
  return data;
}
