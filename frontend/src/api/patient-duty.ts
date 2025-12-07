import http from './http';

export interface PatientDutyItem {
  id: number;
  departmentName?: string;
  departmentId?: number;
  doctorId?: number;
  doctorName?: string;
  doctorTitle?: string;
  weekendType?: number;
  dutyTimeslot?: string;
  createdTime?: string;
  updatedTime?: string;
}

export async function listDutySchedules(filters?: { departmentId?: number; weekendType?: number; dutyTimeslot?: string }) {
  const params: string[] = [];
  if (filters) {
    if (filters.departmentId != null) params.push(`departmentId=${filters.departmentId}`);
    if (filters.weekendType != null) params.push(`weekendType=${filters.weekendType}`);
    if (filters.dutyTimeslot) params.push(`dutyTimeslot=${encodeURIComponent(filters.dutyTimeslot)}`);
  }
  const query = params.length ? '?' + params.join('&') : '';
  console.debug('listDutySchedules called with filters:', filters, ' -> query:', query);
  // use root mapping when query present, otherwise call /all as fallback
  if (query) {
    const { data } = await http.get<PatientDutyItem[]>(`/patient/duty-schedules${query}`);
    return data;
  }
  const { data } = await http.get<PatientDutyItem[]>(`/patient/duty-schedules/all`);
  return data;
}

export async function listDutySchedulesAll() {
  const { data } = await http.get<PatientDutyItem[]>(`/patient/duty-schedules/all`);
  return data;
}
