import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // string shorthand: '/foo' -> 'http://localhost:4567/foo'
      // '/api': 'http://localhost:8081',
      // with options
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // secure: false, // If your backend is HTTPS with self-signed cert
        // rewrite: (path) => path.replace(/^\/api/, '') // if you need to remove /api prefix for backend
      },
    }
  }
})
