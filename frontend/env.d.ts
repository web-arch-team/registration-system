/// <reference types="vite/client" />

import 'vue-router';

declare module 'vue' {
  interface ComponentCustomProperties {
    $t: (key: string) => string;
  }
}

