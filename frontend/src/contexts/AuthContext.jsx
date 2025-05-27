import React, { createContext, useContext, useState, useEffect } from 'react';
import AuthService from '../services/authService';
import api from '../services/api'; 

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true); // To check initial auth status

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    if (storedToken) {
      setToken(storedToken);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await AuthService.login({ email, password });
      // Backend returns { jwtToken: "..." }
      const { jwtToken } = response.data;
      if (!jwtToken) {
        throw new Error('Login failed: No token received from server.');
      }
      localStorage.setItem('token', jwtToken);
      setToken(jwtToken);
      setUser({ email: email }); 
      return { email: email }; 
    } catch (error) {
      console.error('Login failed:', error);
      logout(); 
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const response = await AuthService.register(userData);
      return response.data;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    // Optionally, redirect to login page via useNavigate if called from a component
    // Or let consuming components handle redirection
  };

  const isAuthenticated = !!token;

  const value = {
    user,
    token,
    isAuthenticated,
    login,
    register,
    logout,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
