import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';
import { errMsg } from '../../services/api.js';
import { ErrorBox, Field, PasswordInput } from '../../components/ui.jsx';
import { PASSWORD_RE, passwordStrength } from '../../utils/format.js';
import { AuthSide } from './Login.jsx';

export default function Register() {
  const { register } = useAuth();
  const nav = useNavigate();
  const [f, setF] = useState({ fullName: '', email: '', password: '', confirm: '', terms: false });
  const [errs, setErrs] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const strength = passwordStrength(f.password);

  const submit = async (e) => {
    e.preventDefault();
    const v = {};
    if (!f.fullName.trim()) v.fullName = 'Full name is required';
    if (!/^\S+@\S+\.\S+$/.test(f.email)) v.email = 'Enter a valid email address';
    if (!PASSWORD_RE.test(f.password)) v.password = 'Use 8+ characters with upper-case, lower-case and a number';
    if (f.confirm !== f.password) v.confirm = 'Passwords do not match';
    if (!f.terms) v.terms = 'You must accept the terms';
    setErrs(v); setError('');
    if (Object.keys(v).length) return;
    setBusy(true);
    try {
      await register({ fullName: f.fullName.trim(), email: f.email.trim(), password: f.password });
      nav('/login', { replace: true, state: { registered: true } });
    } catch (err) { setError(errMsg(err)); setBusy(false); }
  };
  return (
    <div className="auth-wrap">
      <AuthSide title="Start learning today" text="Create a student account to enroll in courses and earn verifiable certificates." />
      <div className="auth-main"><form className="auth-card" onSubmit={submit} noValidate>
        <h1 style={{ fontSize: '1.7rem' }}>Create your account</h1>
        <ErrorBox message={error} />
        <Field label="Full name" error={errs.fullName}><input className="input" autoComplete="name" value={f.fullName} onChange={(e) => setF({ ...f, fullName: e.target.value })} /></Field>
        <Field label="Email" error={errs.email}><input className="input" type="email" autoComplete="email" value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></Field>
        <Field label="Password" error={errs.password}>
          <PasswordInput value={f.password} autoComplete="new-password" onChange={(e) => setF({ ...f, password: e.target.value })} />
          <div className="strength" aria-label={`Password strength ${strength} of 4`}>{[1, 2, 3, 4].map((i) => <span key={i} className={strength >= i ? `on${strength}` : ''} />)}</div>
        </Field>
        <Field label="Confirm password" error={errs.confirm}><PasswordInput value={f.confirm} autoComplete="new-password" onChange={(e) => setF({ ...f, confirm: e.target.value })} /></Field>
        <label className="check"><input type="checkbox" checked={f.terms} onChange={(e) => setF({ ...f, terms: e.target.checked })} /><span>I agree to the terms and conditions and privacy policy.</span></label>
        {errs.terms && <small className="field-error" style={{ marginTop: -8, marginBottom: 12 }}>{errs.terms}</small>}
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
        <p className="muted small" style={{ marginTop: 16 }}>Already registered? <Link to="/login">Sign in</Link></p>
      </form></div>
    </div>
  );
}
