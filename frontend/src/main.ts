import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

import App from './App.vue';
import router from './router';
import { useAuthStore } from '@/stores/auth';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount('#app');

// 恢复基础登录态（无 token，仅示例用）
const authStore = useAuthStore();
authStore.restore();
