import { resolve } from 'path';
import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    port: 3000,
    open: false
  },
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        facturaForm: resolve(__dirname, 'factura-form.html'),
        login: resolve(__dirname, 'login.html'),
        registro: resolve(__dirname, 'registro.html')
      }
    }
  }
});
