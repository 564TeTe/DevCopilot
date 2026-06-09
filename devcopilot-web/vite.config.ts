import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    host: '127.0.0.1',
    port: 15173,
    proxy: {
      '/api': {
        target: 'http://localhost:18080',
        changeOrigin: true
      },
      '/actuator': {
        target: 'http://localhost:18080',
        changeOrigin: true
      },
      '/v3': {
        target: 'http://localhost:18080',
        changeOrigin: true
      },
      '/swagger-ui': {
        target: 'http://localhost:18080',
        changeOrigin: true
      }
    }
  }
});
