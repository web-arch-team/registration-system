import http from './http';

export interface Department {
  id?: number;
  departmentName: string;
  diseases?: Disease[];
}

export interface Disease {
  id?: number;
  name: string;
  code?: string;
  description?: string;
  departmentId?: number;
}

export async function fetchDepartments() {
  const { data } = await http.get<Department[]>('/admin/departments');
  return data;
}

export async function fetchDepartment(id: number) {
  const { data } = await http.get<Department>(`/admin/departments/${id}`);
  return data;
}

export async function createDepartment(payload: Department) {
  const { data } = await http.post<Department>('/admin/departments', payload);
  return data;
}

export async function updateDepartment(id: number, payload: Partial<Department>) {
  const { data } = await http.put<Department>(`/admin/departments/${id}`, payload);
  return data;
}

export async function deleteDepartment(id: number) {
  await http.delete(`/admin/departments/${id}`);
}

// Diseases under department
export async function fetchDiseases() {
  const { data } = await http.get<Disease[]>('/admin/diseases');
  return data;
}

export async function fetchDiseaseByDepartment(departmentId: number) {
  const { data } = await http.get<Disease[]>(`/admin/diseases/department/${departmentId}`);
  return data;
}

export async function fetchDisease(id: number) {
  const { data } = await http.get<Disease>(`/admin/diseases/${id}`);
  return data;
}

export async function createDisease(payload: Disease) {
  const { data } = await http.post<Disease>('/admin/diseases', payload);
  return data;
}

export async function updateDisease(id: number, payload: Partial<Disease>) {
  const { data } = await http.put<Disease>(`/admin/diseases/${id}`, payload);
  return data;
}

export async function deleteDisease(id: number) {
  await http.delete(`/admin/diseases/${id}`);
}
