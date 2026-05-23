/* eslint-disable react-hooks/set-state-in-effect, no-unused-vars */
import React, { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import Header from './components/Header.jsx'
import Footer from './components/Footer.jsx'
import Home from './components/Home.jsx'
import TicketsPage from './components/TicketsPage.jsx'
import TicketDetailPage from './components/TicketDetailPage.jsx'
import CartPage from './components/CartPage.jsx'
import BookingSuccessPage from './components/BookingSuccessPage.jsx'
import ManagerPage from './components/ManagerPage.jsx'
import Login from './components/Login.jsx'
import Register from './components/Register.jsx'

// Route Guard for authenticated users
function PrivateRoute({ children }) {
  const location = useLocation();
  const token = localStorage.getItem('accessToken');
  
  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

// Route Guard for Admin users
function AdminRoute({ children }) {
  const location = useLocation();
  const token = localStorage.getItem('accessToken');
  const storedUser = localStorage.getItem('user');
  const userObj = storedUser ? JSON.parse(storedUser) : null;
  const isAdmin = userObj && userObj.roles && userObj.roles.includes('ADMIN');

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  if (!isAdmin) {
    // Standard users trying to access Admin pages get redirected to home
    return <Navigate to="/" replace />;
  }
  return children;
}

function App() {
  const [user, setUser] = useState(null);

  const updateAuth = () => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (err) {
        setUser(null);
      }
    } else {
      setUser(null);
    }
  };

  useEffect(() => {
    updateAuth();
    window.addEventListener('auth-status-change', updateAuth);
    window.addEventListener('auth-logout', updateAuth);
    
    // Listen for global session expiry to notify user
    const handleSessionExpired = (e) => {
      if (e.detail?.sessionExpired) {
        alert('Phiên đăng nhập của bạn đã hết hạn. Vui lòng đăng nhập lại.');
      }
    };
    window.addEventListener('auth-logout', handleSessionExpired);

    return () => {
      window.removeEventListener('auth-status-change', updateAuth);
      window.removeEventListener('auth-logout', updateAuth);
      window.removeEventListener('auth-logout', handleSessionExpired);
    };
  }, []);

  return (
    <Router>
      <Routes>
        {/* Manager Dashboard — No Header or Footer */}
        <Route 
          path="/system/manager" 
          element={
            <AdminRoute>
              <ManagerPage />
            </AdminRoute>
          } 
        />

        {/* Public & Customer Routes */}
        <Route path="*" element={
          <>
            <Header />
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/tickets" element={<TicketsPage />} />
              <Route path="/ticket/:id" element={<TicketDetailPage />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              
              {/* Customer Private Routes */}
              <Route 
                path="/cart" 
                element={
                  <PrivateRoute>
                    <CartPage />
                  </PrivateRoute>
                } 
              />
              <Route 
                path="/booking-success" 
                element={
                  <PrivateRoute>
                    <BookingSuccessPage />
                  </PrivateRoute>
                } 
              />
              <Route 
                path="/payment-result" 
                element={
                  <PrivateRoute>
                    <BookingSuccessPage />
                  </PrivateRoute>
                } 
              />
            </Routes>
            <Footer />
          </>
        } />
      </Routes>
    </Router>
  )
}

export default App
