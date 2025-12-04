import http from './http';

export interface DoctorDayScheduleItem {
  timeslot: string;
  patientId: number;
  patientName: string;
  patientIdCard: string;
  patientPhone: string;
  patientAge: number | null;
  patientGender: string;
}

export interface DoctorWeekScheduleItem {
  weekday: number;
  timeslot: string;
  department: string;
}

export async function fetchDoctorDaySchedule(doctorId: string, weekday: number) {
  const { data } = await http.get<DoctorDayScheduleItem[]>('/doctor/schedule', {
    params: { doctorId, weekday },
  });
  return data;
}

export async function fetchDoctorWeekSchedule(doctorId: string) {
  const { data } = await http.get<DoctorWeekScheduleItem[]>('/doctor/week-schedule', {
    params: { doctorId },
  });
  return data;
}
