import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/NotFound.css';

function NotFound() {
  return (
    <div className="not-found">
      <div className="not-found-content">
        <span className="not-found-emoji">💔</span>
        <h1>404</h1>
        <h2>Page Not Found</h2>
        <p>Oops! Looks like this page doesn't exist.</p>
        <Link to="/" className="not-found-button">
          Back to Home
        </Link>
      </div>
    </div>
  );
}

export default NotFound;
