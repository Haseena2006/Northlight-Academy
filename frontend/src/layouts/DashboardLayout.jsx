import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Award, BarChart3, BookOpen, ClipboardList, FolderTree, GraduationCap, LayoutDashboard, LogOut, Menu, User, Users, X } from 'lucide-react';
import { Brand } from '../components/Navbar.jsx';
import { useAuth } from '../context/AuthContext.jsx';

const LINKS = {
  student: [['/student', 'Overview', LayoutDashboard, true], ['/student/courses', 'My courses', BookOpen], ['/student/certificates', 'Certificates', Award], ['/student/profile', 'Profile', User]],
  admin: [['/admin', 'Overview', LayoutDashboard, true], ['/admin/courses', 'Courses', BookOpen], ['/admin/categories', 'Categories', FolderTree], ['/admin/students', 'Students', Users], ['/admin/instructors', 'Instructors', GraduationCap], ['/admin/enrollments', 'Enrollments', ClipboardList], ['/admin/profile', 'Profile', User]],
  instructor: [['/instructor', 'Overview', LayoutDashboard, true], ['/instructor/courses', 'My courses', BookOpen], ['/instructor/profile', 'Profile', User]],
};

export default function DashboardLayout({ area }) {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const nav = useNavigate();
  return (
    <div className="dash">
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <Brand />
        <nav aria-label={`${area} navigation`}>
          {LINKS[area].map(([to, label, Icon, end]) => (
            <NavLink key={to} to={to} end={end} className="side-link" onClick={() => setOpen(false)}><Icon size={18} />{label}</NavLink>
          ))}
          <NavLink to="/courses" className="side-link" onClick={() => setOpen(false)}><BarChart3 size={18} />Course catalogue</NavLink>
        </nav>
        <div className="side-foot">
          <strong>{user.fullName}</strong><span>{user.email}</span>
          <button className="btn btn-outline btn-sm btn-block" style={{ marginTop: 12 }} onClick={async () => { await logout(); nav('/'); }}><LogOut size={14} />Sign out</button>
        </div>
      </aside>
      <div className="dash-main">
        <div className="dash-top"><Brand /><button className="icon-btn" style={{ color: '#fff' }} onClick={() => setOpen(!open)} aria-label="Toggle navigation">{open ? <X /> : <Menu />}</button></div>
        <div className="dash-content"><Outlet /></div>
      </div>
    </div>
  );
}
