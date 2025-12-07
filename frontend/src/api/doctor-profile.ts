import http from './http';
import type { DoctorSelfDTO, DoctorDutySimpleDTO } from '@/views/doctor/types';

export async function getDoctorProfile(profileId: number) {
  const { data } = await http.get<DoctorSelfDTO>(`/doctor/profile/${profileId}`);
  return data;
}

export async function getDoctorProfileByUserId(userId: number) {
  const { data } = await http.get<DoctorSelfDTO>(`/doctor/profile/by-user/${userId}`);
  return data;
}

export async function updateDoctorProfile(profileId: number, payload: Partial<DoctorSelfDTO>) {
  const { data } = await http.put<DoctorSelfDTO>(`/doctor/profile/${profileId}`, payload);
  return data;
}

export async function changeDoctorPassword(profileId: number, newPassword: string) {
  await http.post(`/doctor/profile/${profileId}/change-password`, { newPassword });
}

export async function fetchDoctorWeekendDuties(profileId: number) {
  const { data } = await http.get<DoctorDutySimpleDTO[]>(`/doctor/profile/${profileId}/duties/weekend`);
  return data;
}
