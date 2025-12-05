import http from './http';

export interface RegistrationPayload {
  patientProfileId: number;
  doctorProfileId: number;
  diseaseId: number;
  weekday: number; // 1-5
  timeslot: string; // AM1..PM4
}

export interface RegistrationResult {
  id: number;
  patientProfileId: number;
  doctorProfileId: number;
  diseaseId: number;
  weekday: number;
  timeslot: string;
  status: string;
}

export async function createRegistration(payload: RegistrationPayload) {
  const { data } = await http.post<RegistrationResult>('/registration', payload);
  return data;
}
