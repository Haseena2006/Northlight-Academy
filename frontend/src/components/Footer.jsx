import { Link } from 'react-router-dom';
import { Brand } from './Navbar.jsx';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="grid grid-3">
          <div><Brand /><p style={{ marginTop: 12 }}>Professional technology education for engineers and teams.</p></div>
          <div><h4>Platform</h4><Link to="/courses">Course catalogue</Link><Link to="/verify">Verify a certificate</Link><Link to="/register">Create an account</Link></div>
          <div><h4>Account</h4><Link to="/login">Sign in</Link><Link to="/forgot-password">Reset password</Link></div>
        </div>
        <div className="footer-bottom">© {new Date().getFullYear()} Northlight Academy. All rights reserved.</div>
      </div>
    </footer>
  );
}
