import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Login.css';

function LoginModal() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const { login, bypass } = useAuth();

  const passwordRules = [
    {
      id: 'length',
      label: '8 characters long',
      test: (value) => value.length >= 8,
    },
    {
      id: 'uppercase',
      label: 'One capital letter',
      test: (value) => /[A-Z]/.test(value),
    },
    {
      id: 'lowercase',
      label: 'One lowercase letter',
      test: (value) => /[a-z]/.test(value),
    },
    {
      id: 'number',
      label: 'One number',
      test: (value) => /\d/.test(value),
    },
    {
      id: 'special',
      label: 'One special character',
      test: (value) => /[^A-Za-z0-9]/.test(value),
    },
  ];
  const isEmailValid = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Please fill in all fields');
      return;
    }

    if (!isEmailValid(email)) {
      setError('Please enter a valid email address.');
      return;
    }

    const isPasswordValid = passwordRules.every((rule) => rule.test(password));
    if (!isPasswordValid) {
      setError('Password does not meet requirements.');
      return;
    }

    login(email, password, rememberMe);
  };

  const handleBypass = () => {
    bypass();
  };

  return (
    <div className="login-overlay">
      <div className="login-modal">
        <div className="login-header">
          <h1>Datemaker</h1>
          <p>Sign in to continue</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          {error && <div className="login-error">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="email">Gmail</label>
            <input
              type="email"
              id="email"
              placeholder="your.email@gmail.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <div className="remember-me">
            <input
              type="checkbox"
              id="rememberMe"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
            />
            <label htmlFor="rememberMe">Remember me</label>
          </div>

          <button type="submit" className="login-btn">
            Sign In
          </button>
        </form>

        <div className="login-divider">
          <span>or</span>
        </div>

        <button onClick={handleBypass} className="bypass-btn">
          Bypass Login (Dev Mode)
        </button>

        <div className="login-footer">
          <p>Don't have an account? <Link to="/register">Sign up</Link></p>
        </div>
      </div>
    </div>
  );
}

export default LoginModal;
