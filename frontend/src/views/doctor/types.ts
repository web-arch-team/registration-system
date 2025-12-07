export interface DoctorSelfDTO {
  id: number;
  username: string;
  doctorId?: string;
  name?: string;
  age?: number;
  gender?: 'male' | 'female';
  title?: string;
  departmentId?: number;
  departmentName?: string;
  diseases?: { id: number; name: string }[];
}

export interface DoctorDutySimpleDTO {
  id: number;
  weekendType: number; // 6=Saturday,7=Sunday
  dutyTimeslot: 'MORNING' | 'AFTERNOON' | 'NIGHT';
  departmentId?: number;
  departmentName?: string;
}
