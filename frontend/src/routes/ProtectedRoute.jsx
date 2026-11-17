import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { Spinner } from '../components/ui.jsx';
import { roleHome } from '../utils/format.js';

/** Frontend guard for UX only; the backend independently enforces every permission. */
export default function ProtectedRoute({ roles }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <Spinner full />;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (roles && !roles.some((r) => user.roles.includes(r))) return <Navigate to={roleHome(user)} replace />;
  return <Outlet />;
}
