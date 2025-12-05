import http from './http';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  userId: number;
  username: string;
  role: 'ADMIN' | 'DOCTOR' | 'PATIENT';
  doctorId?: string;
  patientId?: number;
}

export async function login(payload: LoginPayload) {
  const { data } = await http.post<LoginResult>('/auth/login', payload);
  return data;
}

export interface RegisterPatientPayload {
  username: string;
  password: string;
  name: string;
  idCard: string;
  phoneNumber: string;
  age?: number | null;
  gender: 'male' | 'female';
}

export interface RegisterPatientResult {
  userId: number;
  patientId: number;
  username: string;
  role: 'PATIENT';
}

export async function registerPatient(payload: RegisterPatientPayload) {
  const { data } = await http.post<RegisterPatientResult>('/auth/register', payload);
  return data;
}
