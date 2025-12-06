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
  try {
    const resp = await http.get('/admin/departments');
    const body = resp.data;
    // 如果后端直接返回数组
    if (Array.isArray(body)) {
      return body as Department[];
    }
    // 如果后端返回 ResultDTO 包装形式 { code, data }
    if (body && Array.isArray(body.data)) {
      return body.data as Department[];
    }
    // 其它情况：返回空数组或抛错
    return [] as Department[];
  } catch (err) {
    console.error('fetchDepartments failed', err);
    throw err;
  }
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
