import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { LoginResult } from '@/api/auth';

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResult | null>(null);

  function setUser(u: LoginResult | null) {
    user.value = u;
    if (u) {
      sessionStorage.setItem('auth_user', JSON.stringify(u));
    } else {
      sessionStorage.removeItem('auth_user');
    }
  }

  // 初始化时尝试恢复登录态（前期仅存储基础信息，无 token）
  function restore() {
    const raw = sessionStorage.getItem('auth_user');
    if (raw) {
      try {
        user.value = JSON.parse(raw) as LoginResult;
      } catch {
        sessionStorage.removeItem('auth_user');
      }
    }
  }

  return {
    user,
    setUser,
    restore,
  };
});
