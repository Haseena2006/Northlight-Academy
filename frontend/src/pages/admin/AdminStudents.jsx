import { useEffect, useState } from 'react';
import api, { errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Badge, Empty, ErrorBox, Pagination, Spinner } from '../../components/ui.jsx';
import { fmtDate } from '../../utils/format.js';

export default function AdminStudents() {
  const [q, setQ] = useState(''); const [dq, setDq] = useState(''); const [active, setActive] = useState(''); const [page, setPage] = useState(0); const [err, setErr] = useState('');
  useEffect(() => { const t = setTimeout(() => { setDq(q); setPage(0); }, 350); return () => clearTimeout(t); }, [q]);
  const { data, loading, error, reload } = useFetch(() => api.get('/admin/students', { params: { q: dq || undefined, active: active || undefined, page, size: 10 } }).then((r) => r.data), [dq, active, page]);
  const toggle = async (u) => { setErr(''); try { await api.patch(`/admin/users/${u.id}/status`, { active: !u.active }); reload(); } catch (e) { setErr(errMsg(e)); } };
  return (
    <>
      <div className="page-head"><h1>Students</h1></div>
      <ErrorBox message={error || err} />
      <div className="filters"><input className="input grow" placeholder="Search by name or email" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search students" />
        <select className="input" value={active} onChange={(e) => { setActive(e.target.value); setPage(0); }} aria-label="Status"><option value="">All statuses</option><option value="true">Active</option><option value="false">Inactive</option></select></div>
      {loading ? <Spinner /> : !data || data.content.length === 0 ? <Empty title="No students found" /> : <>
        <div className="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Registered</th><th>Enrolled</th><th>Completed</th><th>Status</th><th></th></tr></thead><tbody>{data.content.map((u) => (
          <tr key={u.id}><td><strong>{u.fullName}</strong></td><td>{u.email}</td><td>{fmtDate(u.createdAt)}</td><td>{u.enrolledCourses}</td><td>{u.completedCourses}</td>
            <td>{u.active ? <Badge tone="success">Active</Badge> : <Badge tone="error">Inactive</Badge>}</td>
            <td><button className="btn btn-outline btn-sm" onClick={() => toggle(u)}>{u.active ? 'Deactivate' : 'Activate'}</button></td></tr>))}</tbody></table></div>
        <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} /></>}
    </>
  );
}
