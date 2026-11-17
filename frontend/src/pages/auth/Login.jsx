import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';
import { errMsg } from '../../services/api.js';
import { ErrorBox, Field, Notice, PasswordInput } from '../../components/ui.jsx';
import { roleHome } from '../../utils/format.js';

export function AuthSide({ title, text }) {
  return <div className="auth-side"><span className="eyebrow" style={{ color: '#9DB8E3' }}>Northlight Academy</span><h2>{title}</h2><p>{text}</p></div>;
}

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const loc = useLocation();
  const [f, setF] = useState({ email: '', password: '', rememberMe: false });
  const [errs, setErrs] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    const v = {};
    if (!/^\S+@\S+\.\S+$/.test(f.email)) v.email = 'Enter a valid email address';
    if (!f.password) v.password = 'Password is required';
    setErrs(v); setError('');
    if (Object.keys(v).length) return;
    setBusy(true);
    try {
      const u = await login(f.email.trim(), f.password, f.rememberMe);
      nav(loc.state?.from && loc.state.from !== '/login' ? loc.state.from : roleHome(u), { replace: true });
    } catch (err) { setError(errMsg(err)); setBusy(false); }
  };
  return (
    <div className="auth-wrap">
      <AuthSide title="Welcome back" text="Sign in to continue your courses, track progress and download certificates." />
      <div className="auth-main"><form className="auth-card" onSubmit={submit} noValidate>
        <h1 style={{ fontSize: '1.7rem' }}>Sign in</h1>
        <p className="muted">Use the email address you registered with.</p>
        {loc.state?.registered && <Notice>Account created. You can sign in now.</Notice>}
        <ErrorBox message={error} />
        <Field label="Email" error={errs.email}><input className="input" type="email" autoComplete="email" value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></Field>
        <Field label="Password" error={errs.password}><PasswordInput value={f.password} autoComplete="current-password" onChange={(e) => setF({ ...f, password: e.target.value })} /></Field>
        <div className="row between" style={{ marginBottom: 16 }}>
          <label className="check" style={{ margin: 0 }}><input type="checkbox" checked={f.rememberMe} onChange={(e) => setF({ ...f, rememberMe: e.target.checked })} />Keep me signed in</label>
          <Link to="/forgot-password" className="small">Forgot password?</Link>
        </div>
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
        <p className="muted small" style={{ marginTop: 16 }}>New here? <Link to="/register">Create an account</Link></p>
      </form></div>
    </div>
  );
}
