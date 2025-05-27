import axios from 'axios';

const API_URL = 'http://localhost:8082/api'; // Your Spring Boot backend URL

const api = axios.create({
  baseURL: API_URL,
});

// Interceptor to add JWT token to requests if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
