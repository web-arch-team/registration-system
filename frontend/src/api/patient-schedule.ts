import http from './http';

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
  departmentName?: string;
}

export interface DiseaseTimetableItem {
  doctorProfileId: number;
  doctorId: string;
  doctorName: string;
  doctorTitle?: string;
  departmentName?: string;
  weekday: number; // 1-5
  timeslot: string; // AM1..PM4
  timeDescription?: string;
  currentPatients?: number;
  maxPatients?: number;
  available?: boolean;
}

export async function fetchDepartments() {
  const { data } = await http.get<Department[]>('/schedule/departments');
  return data;
}

export async function fetchDiseasesByDepartment(departmentId: number) {
  const { data } = await http.get<Disease[]>(`/schedule/department/${departmentId}/diseases`);
  return data;
}

export async function fetchDiseaseTimetable(diseaseId: number, weekday?: number) {
  const { data } = await http.get<DiseaseTimetableItem[]>(`/schedule/disease/${diseaseId}/timetable`, {
    params: { weekday },
  });
  return data;
}
