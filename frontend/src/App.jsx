import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import PdfViewPage from './pages/PdfViewPage'; 
import PdfDetailPage from './pages/PdfDetailPage'; // Import the new detail page
import SharedPdfViewerPage from './pages/SharedPdfViewerPage'; // Import the shared PDF viewer page

import { useAuth } from './contexts/AuthContext';

function App() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="container" style={{ textAlign: 'center', marginTop: '50px' }}><h2>Loading...</h2></div>; // Or a proper spinner component
  }

  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route 
          path="/dashboard" 
          element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" />}
        />
        <Route 
          path="/pdf/:id" 
          element={isAuthenticated ? <PdfDetailPage /> : <Navigate to="/login" />}
        />
        <Route 
          path="/"
          element={isAuthenticated ? <Navigate to="/dashboard" /> : <Navigate to="/login" />}
        />
        <Route path="/share/:shareToken" element={<SharedPdfViewerPage />} /> {/* Public route for shared PDFs */}
      </Routes>
    </>
  );
}

export default App;
