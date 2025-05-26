import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Navbar = () => {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login'); // Redirect to login page after logout
  };

  return (
    <header>
      <div className="container">
        <Link to={isAuthenticated ? "/dashboard" : "/login"} style={{ textDecoration: 'none' }}>
          <h1>PDFCollab</h1>
        </Link>
        <nav>
          {isAuthenticated ? (
            <>
              <Link to="/dashboard">Dashboard</Link>
              <a href="#" onClick={handleLogout} style={{ marginLeft: '20px', textDecoration: 'none', color: '#333', fontWeight: '500', cursor: 'pointer' }}>Logout</a>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Navbar;
