import api from './api';

const AuthService = {
  register: (userData) => {
    return api.post('/auth/register', userData);
  },

  login: (credentials) => {
    return api.post('/auth/login', credentials);
  },

  // logout: () => { /* Handled by removing token and updating context */ },
};

export default AuthService;
