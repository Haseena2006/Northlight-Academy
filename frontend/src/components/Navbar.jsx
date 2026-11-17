import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { GraduationCap, Menu, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { roleHome } from '../utils/format.js';

export const Brand = ({ to = '/' }) => (
  <Link to={to} className="brand"><span className="brand-mark"><GraduationCap size={18} /></span>Northlight Academy</Link>
);

export default function Navbar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const nav = useNavigate();
  const close = () => setOpen(false);
  return (
    <header className={`navbar ${open ? 'open' : ''}`}>
      <div className="container">
        <Brand />
        <nav className="nav-links" aria-label="Main">
          <NavLink to="/" end onClick={close}>Home</NavLink>
          <NavLink to="/courses" onClick={close}>Courses</NavLink>
          <NavLink to="/verify" onClick={close}>Verify certificate</NavLink>
        </nav>
        <div className="nav-actions">
          {user ? (
            <>
              <Link className="btn btn-outline btn-sm" to={roleHome(user)} onClick={close}>My dashboard</Link>
              <button className="btn btn-dark btn-sm" onClick={async () => { close(); await logout(); nav('/'); }}>Sign out</button>
            </>
          ) : (
            <>
              <Link className="btn btn-outline btn-sm" to="/login" onClick={close}>Sign in</Link>
              <Link className="btn btn-primary btn-sm" to="/register" onClick={close}>Create account</Link>
            </>
          )}
        </div>
        <button className="icon-btn nav-toggle" onClick={() => setOpen(!open)} aria-label="Toggle menu" aria-expanded={open}>{open ? <X /> : <Menu />}</button>
      </div>
    </header>
  );
}
