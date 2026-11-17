import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { errMsg } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';
import { ErrorBox, Field, Notice, PasswordInput } from '../components/ui.jsx';
import { PASSWORD_RE, fmtDate } from '../utils/format.js';

export default function Profile() {
  const { user, setUser, logout } = useAuth();
  const nav = useNavigate();
  const [name, setName] = useState(user.fullName);
  const [pm, setPm] = useState({ ok: '', err: '' });
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' });
  const [pwm, setPwm] = useState({ ok: '', err: '' });

  const saveProfile = async (e) => {
    e.preventDefault(); setPm({ ok: '', err: '' });
    if (!name.trim()) return setPm({ ok: '', err: 'Name is required' });
    try { const { data } = await api.put('/auth/me', { fullName: name.trim() }); setUser(data); setPm({ ok: 'Profile updated.', err: '' }); }
    catch (err) { setPm({ ok: '', err: errMsg(err) }); }
  };
  const changePw = async (e) => {
    e.preventDefault(); setPwm({ ok: '', err: '' });
    if (!PASSWORD_RE.test(pw.newPassword)) return setPwm({ ok: '', err: 'Use 8+ characters with upper-case, lower-case and a number' });
    if (pw.newPassword !== pw.confirm) return setPwm({ ok: '', err: 'Passwords do not match' });
    try { await api.post('/auth/change-password', { currentPassword: pw.currentPassword, newPassword: pw.newPassword }); await logout(); nav('/login'); }
    catch (err) { setPwm({ ok: '', err: errMsg(err) }); }
  };
  return (
    <>
      <div className="page-head"><h1>Profile</h1></div>
      <div className="grid grid-2">
        <form className="card card-pad" onSubmit={saveProfile}>
          <h2>Account details</h2><ErrorBox message={pm.err} /><Notice>{pm.ok}</Notice>
          <Field label="Full name"><input className="input" value={name} onChange={(e) => setName(e.target.value)} /></Field>
          <Field label="Email"><input className="input" value={user.email} disabled /></Field>
          <p className="muted small">Role: {user.roles.join(', ')} · Member since {fmtDate(user.createdAt)}</p>
          <button className="btn btn-primary">Save changes</button>
        </form>
        <form className="card card-pad" onSubmit={changePw}>
          <h2>Change password</h2><ErrorBox message={pwm.err} />
          <Field label="Current password"><PasswordInput value={pw.currentPassword} autoComplete="current-password" onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} /></Field>
          <Field label="New password"><PasswordInput value={pw.newPassword} autoComplete="new-password" onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} /></Field>
          <Field label="Confirm new password"><PasswordInput value={pw.confirm} autoComplete="new-password" onChange={(e) => setPw({ ...pw, confirm: e.target.value })} /></Field>
          <p className="muted small">You will be signed out on all devices after changing your password.</p>
          <button className="btn btn-dark">Update password</button>
        </form>
      </div>
      <div className="card card-pad" style={{ marginTop: 24 }}><h2>Session</h2><button className="btn btn-outline" onClick={async () => { await logout(); nav('/'); }}>Sign out</button></div>
    </>
  );
}
