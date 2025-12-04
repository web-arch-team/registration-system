import axios from 'axios';

// Axios 实例：统一前缀 /api，方便调用后端接口。
const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

export default http;
