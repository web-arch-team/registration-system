import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建Axios实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // 接口基础地址（可在.env文件配置）
    timeout: 5000 // 请求超时时间
});

// 请求拦截器：添加请求头（如Token）
request.interceptors.request.use(
    (config) => {
        // 示例：从本地存储获取Token并添加到请求头
        const token = localStorage.getItem('admin_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器：统一处理错误
request.interceptors.response.use(
    (response) => {
        // 假设后端返回格式为 { code: number, data: any, message: string }
        const res = response.data;
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败');
            return Promise.reject(new Error(res.message || 'Error'));
        }
        return res;
    },
    (error) => {
        ElMessage.error(error.message || '网络异常，请稍后再试');
        return Promise.reject(error);
    }
);

export default request;