import http from './http';

export interface ScheduleDTO {
  id?: number;
  doctorProfileId: number;
  departmentId: number;
  weekday: number; // 1-5
  timeslot: string; // AM1..PM4
  maxPatientsPerSlot?: number;
}

export interface DoctorProfile {
  id: number;
  name?: string;
  doctorId?: string;
}

export interface Department {
  id: number;
  departmentName?: string;
}

export interface DoctorDepartmentSchedule {
  id: number;
  doctorProfile: DoctorProfile;
  department: Department;
  weekday: number;
  timeslot: string;
  maxPatientsPerSlot: number;
}

export interface ResultDTO<T> {
  code: number;
  msg?: string;
  data?: T;
}

export async function fetchSchedulesByDepartment(deptId: number) {
  const { data } = await http.get<ResultDTO<DoctorDepartmentSchedule[]>>(`/admin/schedule/department/${deptId}`);
  return data;
}

export async function addSchedule(payload: ScheduleDTO) {
  const { data } = await http.post<ResultDTO<DoctorDepartmentSchedule>>('/admin/schedule', payload);
  return data;
}

export async function updateSchedule(payload: ScheduleDTO) {
  const { data } = await http.put<ResultDTO<DoctorDepartmentSchedule>>('/admin/schedule', payload);
  return data;
}

export async function deleteSchedule(scheduleId: number) {
  const { data } = await http.delete<ResultDTO<void>>(`/admin/schedule/${scheduleId}`);
  return data;
}

export async function batchDeleteScheduleByDept(deptId: number) {
  const { data } = await http.delete<ResultDTO<void>>(`/admin/schedule/batch/department/${deptId}`);
  return data;
}

export async function batchClearScheduleByDept(deptId: number) {
  const { data } = await http.post<ResultDTO<void>>(`/admin/schedule/batch/department/${deptId}/clear`);
  return data;
}

// fetch doctors for a department (public schedule API)
export async function fetchDoctorsByDepartment(deptId: number) {
  const { data } = await http.get<DoctorProfile[]>(`/schedule/department/${deptId}/doctors`);
  return data;
}
