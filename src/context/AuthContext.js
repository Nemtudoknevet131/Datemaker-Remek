import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return (
      localStorage.getItem('isAuthenticated') === 'true' ||
      sessionStorage.getItem('isAuthenticated') === 'true'
    );
  });

  const login = (email, password, rememberMe = false) => {
    console.log('Login:', { email, password });
    setIsAuthenticated(true);
    if (rememberMe) {
      localStorage.setItem('isAuthenticated', 'true');
      sessionStorage.removeItem('isAuthenticated');
    } else {
      sessionStorage.setItem('isAuthenticated', 'true');
      localStorage.removeItem('isAuthenticated');
    }
  };

  const bypass = () => {
    setIsAuthenticated(true);
    sessionStorage.setItem('isAuthenticated', 'true');
  };

  const logout = () => {
    setIsAuthenticated(false);
    localStorage.removeItem('isAuthenticated');
    sessionStorage.removeItem('isAuthenticated');
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, bypass, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
