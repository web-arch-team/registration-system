import http from './http';

export interface Patient {
  id?: number;
  userId?: number;
  username: string;
  idCard: string;
  name: string;
  phoneNumber: string;
  age?: number | null;
  gender: 'male' | 'female';
  isActive?: boolean;
}

export interface PatientQuery {
  name?: string;
  idCard?: string;
  phoneNumber?: string;
  gender?: 'male' | 'female';
  pageNum?: number;
  pageSize?: number;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// 仅把有值的查询条件传给后端，避免空字符串参与过滤
export async function fetchPatients(params: PatientQuery) {
  const cleaned: PatientQuery = {};
  if (params.name && params.name.trim().length > 0) cleaned.name = params.name.trim();
  if (params.idCard && params.idCard.trim().length > 0) cleaned.idCard = params.idCard.trim();
  if (params.phoneNumber && params.phoneNumber.trim().length > 0) cleaned.phoneNumber = params.phoneNumber.trim();
  if (params.gender) cleaned.gender = params.gender;
  cleaned.pageNum = params.pageNum ?? 1;
  cleaned.pageSize = params.pageSize ?? 10;

  const { data } = await http.get<PageResult<Patient>>('/admin/patients', { params: cleaned });
  return data;
}

export async function createPatient(payload: Patient) {
  const { data } = await http.post<Patient>('/admin/patients', payload);
  return data;
}

export async function updatePatient(id: number, payload: Partial<Patient>) {
  const { data } = await http.put<Patient>(`/admin/patients/${id}`, payload);
  return data;
}

export async function deletePatient(id: number) {
  await http.delete(`/admin/patients/${id}`);
}

export async function fetchPatient(id: number) {
  const { data } = await http.get<Patient>(`/admin/patients/${id}`);
  return data;
}
