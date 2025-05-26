import React, { createContext, useContext, useState, useEffect } from 'react';
import AuthService from '../services/authService';
import api from '../services/api'; // For direct api instance access if needed for user fetching

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
      // Optionally, you could verify the token here and fetch user details
      // For now, we'll assume if a token exists, it's valid until an API call fails
      // Or, decode token to get user info if it's stored in the token (not best practice for sensitive info)
      // For simplicity, we'll not fetch user details on load here, but in a real app you might.
      // setUser({ username: 'loaded_from_token_placeholder' }); // Example if user info was in token
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
      // Set user with the username used for login, as backend doesn't return full user object on login
      // You might want to fetch full user details from another endpoint using the token
      // For now, just indicate login was successful or store email if needed
      // If your app primarily uses email as the identifier in the frontend user object:
      setUser({ email: email }); 
      return { email: email }; // Or return whatever user representation you prefer
    } catch (error) {
      console.error('Login failed:', error);
      logout(); // Clear any stale auth state
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
    loading, // To allow components to wait for initial auth check
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
