import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../styles/Register.css';

const Register = () => {
  const [formValues, setFormValues] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    dob: '',
  });
  const [formError, setFormError] = useState('');

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

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormValues((prev) => ({ ...prev, [name]: value }));
  };

  const isEmailValid = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  const getDobBounds = () => {
    const today = new Date();
    const maxDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate())
      .toISOString()
      .slice(0, 10);
    const minDate = new Date(today.getFullYear() - 100, today.getMonth(), today.getDate())
      .toISOString()
      .slice(0, 10);
    return { minDate, maxDate };
  };

  const { minDate, maxDate } = getDobBounds();
  const isDobValid = (value) => !value || value <= maxDate;

  const handleSubmit = (event) => {
    event.preventDefault();
    setFormError('');

    if (!formValues.firstName || !formValues.lastName || !formValues.email || !formValues.password || !formValues.dob) {
      setFormError('Please fill in all required fields.');
      return;
    }

    if (!isEmailValid(formValues.email)) {
      setFormError('Please enter a valid email address.');
      return;
    }

    if (!isDobValid(formValues.dob)) {
      setFormError('You must be at least 18 years old.');
      return;
    }

    const allRulesMet = passwordRules.every((rule) => rule.test(formValues.password));
    if (!allRulesMet) {
      setFormError('Password does not meet all requirements.');
    }
  };

  return (
    <div className="register-page">
      <div className="register-card">
        <div className="register-header">
          <h1>Datemaker</h1>
          <p>Create your account</p>
        </div>

        <form className="register-form" onSubmit={handleSubmit}>
          {formError && <div className="register-error">{formError}</div>}
          <div className="register-row">
            <div className="form-group">
              <label htmlFor="firstName">First name</label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                placeholder="Alex"
                value={formValues.firstName}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lastName">Last name</label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                placeholder="Johnson"
                value={formValues.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="your.email@gmail.com"
              value={formValues.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
            {formValues.email && !isEmailValid(formValues.email) && (
              <span className="email-error">Please enter a valid email address.</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="Create a password"
              value={formValues.password}
              onChange={handleChange}
              autoComplete="new-password"
              required
            />
            <ul className="password-rules">
              {passwordRules.map((rule) => {
                const isMet = rule.test(formValues.password);
                return (
                  <li key={rule.id} className={isMet ? 'met' : ''}>
                    <span className="rule-icon" aria-hidden="true">
                      {isMet ? '❤️' : '💔'}
                    </span>
                    <span>{rule.label}</span>
                  </li>
                );
              })}
            </ul>
          </div>

          <div className="form-group">
            <label htmlFor="dob">Date of birth</label>
            <input
              id="dob"
              name="dob"
              type="date"
              value={formValues.dob}
              onChange={handleChange}
              min={minDate}
              max={maxDate}
              required
            />
            {formValues.dob && !isDobValid(formValues.dob) && (
              <span className="dob-error">You must be at least 18 years old.</span>
            )}
          </div>

          <button type="submit" className="register-btn">
            Create account
          </button>
        </form>

        <div className="register-divider">
          <span>or</span>
        </div>

        <button type="button" className="google-btn">
          <span className="google-icon">G</span>
          Login with Google
        </button>

        <div className="register-footer">
          <p>
            Already have an account? <Link to="/">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
