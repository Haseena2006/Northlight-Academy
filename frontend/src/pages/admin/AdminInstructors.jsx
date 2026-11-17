import { useState } from 'react';
import { Plus } from 'lucide-react';
import api, { errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Badge, Empty, ErrorBox, Field, Modal, Notice, PasswordInput, Spinner } from '../../components/ui.jsx';
import { PASSWORD_RE, fmtDate } from '../../utils/format.js';

export default function AdminInstructors() {
  const { data, loading, error, reload } = useFetch(() => api.get('/admin/instructors', { params: { size: 50 } }).then((r) => r.data));
  const allCourses = useFetch(() => api.get('/admin/courses', { params: { size: 50, sort: 'title' } }).then((r) => r.data.content));
  const [create, setCreate] = useState(null); const [assign, setAssign] = useState(null); const [err, setErr] = useState(''); const [ok, setOk] = useState(''); const [busy, setBusy] = useState(false);

  const doCreate = async (e) => {
    e.preventDefault(); setErr('');
    if (!PASSWORD_RE.test(create.password)) return setErr('Initial password needs 8+ characters with upper-case, lower-case and a number');
    setBusy(true);
    try { await api.post('/admin/instructors', create); setCreate(null); setOk('Instructor account created. Share the initial password securely; they can change it from their profile.'); reload(); }
    catch (x) { setErr(errMsg(x)); } finally { setBusy(false); }
  };
  const doAssign = async () => {
    setBusy(true); setErr('');
    try { await api.put(`/admin/instructors/${assign.id}/courses`, { courseIds: assign.ids }); setAssign(null); setOk('Assignments updated.'); reload(); }
    catch (x) { setErr(errMsg(x)); } finally { setBusy(false); }
  };
  const toggle = async (u) => { setErr(''); try { await api.patch(`/admin/users/${u.id}/status`, { active: !u.active }); reload(); } catch (x) { setErr(errMsg(x)); } };
  if (loading) return <Spinner />;
  return (
    <>
      <div className="page-head"><h1>Instructors</h1><button className="btn btn-primary" onClick={() => { setErr(''); setCreate({ fullName: '', email: '', password: '' }); }}><Plus size={16} />Add instructor</button></div>
      <ErrorBox message={error || (!create && !assign && err)} /><Notice>{ok}</Notice>
      {data.content.length === 0 ? <Empty title="No instructors yet" text="Create an instructor account, then assign courses." /> :
        <div className="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Joined</th><th>Assigned courses</th><th>Status</th><th></th></tr></thead><tbody>{data.content.map((u) => (
          <tr key={u.id}><td><strong>{u.fullName}</strong></td><td>{u.email}</td><td>{fmtDate(u.createdAt)}</td><td>{u.assignedCourses.length ? u.assignedCourses.map((c) => c.title).join(', ') : <span className="muted">None</span>}</td>
            <td>{u.active ? <Badge tone="success">Active</Badge> : <Badge tone="error">Inactive</Badge>}</td>
            <td><div className="actions"><button className="btn btn-outline btn-sm" onClick={() => { setErr(''); setAssign({ id: u.id, name: u.fullName, ids: u.assignedCourses.map((c) => c.id) }); }}>Assign courses</button><button className="btn btn-outline btn-sm" onClick={() => toggle(u)}>{u.active ? 'Deactivate' : 'Activate'}</button></div></td></tr>))}</tbody></table></div>}
      {create && <Modal title="Add instructor" onClose={() => setCreate(null)}><form onSubmit={doCreate}><ErrorBox message={err} />
        <Field label="Full name"><input className="input" required value={create.fullName} onChange={(e) => setCreate({ ...create, fullName: e.target.value })} /></Field>
        <Field label="Email"><input className="input" type="email" required value={create.email} onChange={(e) => setCreate({ ...create, email: e.target.value })} /></Field>
        <Field label="Initial password" hint="No email invitation is sent; share this password securely."><PasswordInput value={create.password} autoComplete="new-password" onChange={(e) => setCreate({ ...create, password: e.target.value })} /></Field>
        <div className="row end gap"><button type="button" className="btn btn-outline" onClick={() => setCreate(null)}>Cancel</button><button className="btn btn-primary" disabled={busy}>Create instructor</button></div></form></Modal>}
      {assign && <Modal title={`Assign courses — ${assign.name}`} onClose={() => setAssign(null)}><ErrorBox message={err} />
        <div className="checklist" style={{ maxHeight: 280 }}>{allCourses.data?.map((c) => <label key={c.id}><input type="checkbox" checked={assign.ids.includes(c.id)} onChange={(e) => setAssign({ ...assign, ids: e.target.checked ? [...assign.ids, c.id] : assign.ids.filter((x) => x !== c.id) })} />{c.title}</label>)}</div>
        <div className="row end gap" style={{ marginTop: 16 }}><button className="btn btn-outline" onClick={() => setAssign(null)}>Cancel</button><button className="btn btn-primary" disabled={busy} onClick={doAssign}>Save assignments</button></div></Modal>}
    </>
  );
}
