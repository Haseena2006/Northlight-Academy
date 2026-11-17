import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import api, { errMsg } from '../../services/api.js';
import { ErrorBox, Field, Notice, PasswordInput } from '../../components/ui.jsx';
import { PASSWORD_RE } from '../../utils/format.js';
import { AuthSide } from './Login.jsx';

export function ForgotPassword() {
  const [email, setEmail] = useState(''); const [msg, setMsg] = useState(''); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  const submit = async (e) => {
    e.preventDefault(); setError(''); setMsg('');
    if (!/^\S+@\S+\.\S+$/.test(email)) return setError('Enter a valid email address');
    setBusy(true);
    try { const { data } = await api.post('/auth/forgot-password', { email }); setMsg(data.message); } catch (err) { setError(errMsg(err)); } finally { setBusy(false); }
  };
  return (
    <div className="auth-wrap"><AuthSide title="Reset your password" text="Request a single-use reset link that expires after 30 minutes." />
      <div className="auth-main"><form className="auth-card" onSubmit={submit} noValidate>
        <h1 style={{ fontSize: '1.7rem' }}>Forgot password</h1>
        <ErrorBox message={error} /><Notice>{msg}</Notice>
        <Field label="Email"><input className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} /></Field>
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Sending…' : 'Request reset link'}</button>
        <p className="muted small" style={{ marginTop: 16 }}>Email delivery is only available if the operator has configured a mail provider. <Link to="/login">Back to sign in</Link></p>
      </form></div></div>
  );
}

export function ResetPassword() {
  const [sp] = useSearchParams(); const nav = useNavigate();
  const token = sp.get('token') || '';
  const [pw, setPw] = useState(''); const [c, setC] = useState(''); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  const submit = async (e) => {
    e.preventDefault(); setError('');
    if (!PASSWORD_RE.test(pw)) return setError('Use 8+ characters with upper-case, lower-case and a number');
    if (pw !== c) return setError('Passwords do not match');
    setBusy(true);
    try { await api.post('/auth/reset-password', { token, newPassword: pw }); nav('/login', { replace: true }); } catch (err) { setError(errMsg(err)); setBusy(false); }
  };
  return (
    <div className="auth-wrap"><AuthSide title="Choose a new password" text="Reset links are single-use. All existing sessions are signed out after a reset." />
      <div className="auth-main"><form className="auth-card" onSubmit={submit} noValidate>
        <h1 style={{ fontSize: '1.7rem' }}>Reset password</h1>
        {!token && <ErrorBox message="This reset link is missing its token." />}
        <ErrorBox message={error} />
        <Field label="New password"><PasswordInput value={pw} autoComplete="new-password" onChange={(e) => setPw(e.target.value)} /></Field>
        <Field label="Confirm new password"><PasswordInput value={c} autoComplete="new-password" onChange={(e) => setC(e.target.value)} /></Field>
        <button className="btn btn-primary btn-block" disabled={busy || !token}>{busy ? 'Saving…' : 'Update password'}</button>
      </form></div></div>
  );
}
