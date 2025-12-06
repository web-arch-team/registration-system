import http from './http';

import type { ResultDTO, DoctorProfile, Department } from './admin-schedule';

// 周末类型枚举
export enum WeekendType {
    SATURDAY = 6,
    SUNDAY = 7,
}

// 值班时段枚举
export enum DutyTimeslot {
    MORNING = 'MORNING',
    AFTERNOON = 'AFTERNOON',
    NIGHT = 'NIGHT',
}

// 值班DTO
export interface DutyScheduleDTO {
    id?: number;
    departmentId: number;
    doctorProfileId: number;
    weekendType: WeekendType;
    dutyTimeslot: DutyTimeslot;
}

// 值班记录VO
export interface DoctorDutyScheduleVO {
    id: number;
    department: Department;
    doctorProfile: DoctorProfile;
    weekendType: number;
    dutyTimeslot: string;
    createdTime: string;
    updatedTime: string;
}



/**
 * 根据科室ID查询值班记录
 */
export async function getDutyScheduleByDeptId(deptId: number) {
    const { data } = await http.get<ResultDTO<DoctorDutyScheduleVO[]>>(
        `/admin/duty-schedule/department/${deptId}`
    );
    return data;
}

/**
 * 新增值班记录
 */
export async function addDutySchedule(payload: DutyScheduleDTO) {
    const { data } = await http.post<ResultDTO<DoctorDutyScheduleVO>>(
        '/admin/duty-schedule',
        payload
    );
    return data;
}

/**
 * 编辑值班记录
 */
export async function updateDutySchedule(payload: DutyScheduleDTO) {
    const { data } = await http.put<ResultDTO<DoctorDutyScheduleVO>>(
        '/admin/duty-schedule',
        payload
    );
    return data;
}

/**
 * 删除单条值班记录
 */
export async function deleteDutySchedule(id: number) {
    const { data } = await http.delete<ResultDTO<void>>(
        `/admin/duty-schedule/${id}`
    );
    return data;
}

/**
 * 批量删除科室所有值班记录
 */
export async function batchDeleteDutyByDeptId(deptId: number) {
    const { data } = await http.delete<ResultDTO<void>>(
        `/admin/duty-schedule/batch/department/${deptId}`
    );
    return data;
}

/**
 * 查询所有科室
 */
export async function fetchDepartments() {
    try {
        // 后端 AdminDepartmentController 暴露路径为 /api/admin/departments，axios 的 baseURL 已设置为 /api
        // 因此这里请求 '/admin/departments'，返回可能是 ResultDTO 包装或直接数组
        const resp = await http.get('/admin/departments');
        const body = resp.data;
        if (Array.isArray(body)) return body as Department[];
        if (body && Array.isArray(body.data)) return body.data as Department[];
        return [] as Department[];
    } catch (error) {
        console.error('加载科室列表失败：', error);
        throw error; // 抛出错误让前端捕获提示
    }
}

/**
 * 根据科室ID查询医生
 */
export async function fetchDoctorsByDeptId(deptId: number) {
    try {
        const { data } = await http.get<ResultDTO<DoctorProfile[]>>(
            `/schedule/department/${deptId}/doctors`
        );
        return data;
    } catch (error) {
        console.error('加载医生列表失败：', error);
        throw error;
    }
}