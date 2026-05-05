import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Navbar from './components/Navbar';
import ChatWidget from './components/ChatWidget';
import Profile from './pages/Profile';
import Feed from './pages/Feed';
import MapPage from './pages/MapPage';
import Calendar from './pages/Calendar';
import LoginModal from './pages/Login';
import Register from './pages/Register';
import NotFound from './pages/NotFound';
import { AuthProvider, useAuth } from './context/AuthContext';
import './styles/App.css';

function AppContent() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const isRegisterRoute = location.pathname === '/register';
  return (
    <div className="app">
      {!isAuthenticated && !isRegisterRoute && <LoginModal />}
      {isRegisterRoute && <Register />}
      <Navbar />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Feed />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/map" element={<MapPage />} />
          <Route path="/calendar" element={<Calendar />} />
          <Route path="/register" element={<Feed />} />
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <ChatWidget />
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContent />
      </Router>
    </AuthProvider>
  );
}

export default App;
