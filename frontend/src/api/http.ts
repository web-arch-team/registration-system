import axios from 'axios';

// Axios 实例：统一前缀 /api，方便调用后端接口。
const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true, // 发送请求时携带 cookie，以支持基于 session 的后端鉴权
});

export default http;
