import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext'; // Using register from AuthContext
import { useNavigate, Link } from 'react-router-dom';

const RegisterPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { register } = useAuth(); // Get register function from context
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await register({ username, email, password });
      alert('Registration successful! Please login.'); // Or a more sophisticated notification
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to register. Please try again.');
      console.error('Register page error:', err);
    }
  };

  return (
    <div className="auth-form">
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        {error && <p className="error-message">{error}</p>}
        <div className="form-group">
          <label htmlFor="username">Username</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit">Register</button>
        <p style={{ textAlign: 'center', marginTop: '15px' }}>
          Already have an account? <Link to="/login" className="nav-link" style={{display: 'inline', margin: '0'}}>Login here</Link>
        </p>
      </form>
    </div>
  );
};

export default RegisterPage;
