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
}

export async function login(payload: LoginPayload) {
  const { data } = await http.post<LoginResult>('/auth/login', payload);
  return data;
}
