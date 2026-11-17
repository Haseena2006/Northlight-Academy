import { useState } from 'react';
import { Pencil, Plus, Trash2 } from 'lucide-react';
import api, { errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Confirm, Empty, ErrorBox, Field, Modal, Notice, Spinner } from '../../components/ui.jsx';

export default function AdminCategories() {
  const { data, loading, error, reload } = useFetch(() => api.get('/categories').then((r) => r.data));
  const [edit, setEdit] = useState(null); const [del, setDel] = useState(null); const [err, setErr] = useState(''); const [ok, setOk] = useState(''); const [busy, setBusy] = useState(false);
  const save = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true);
    try { edit.id ? await api.put(`/admin/categories/${edit.id}`, { name: edit.name, description: edit.description }) : await api.post('/admin/categories', { name: edit.name, description: edit.description }); setEdit(null); setOk('Category saved.'); reload(); }
    catch (x) { setErr(errMsg(x)); } finally { setBusy(false); }
  };
  const remove = async () => { setBusy(true); try { await api.delete(`/admin/categories/${del.id}`); setDel(null); setOk('Category deleted.'); reload(); } catch (x) { setDel(null); setOk(''); setErr(errMsg(x)); } finally { setBusy(false); } };
  if (loading) return <Spinner />;
  return (
    <>
      <div className="page-head"><h1>Categories</h1><button className="btn btn-primary" onClick={() => { setErr(''); setEdit({ name: '', description: '' }); }}><Plus size={16} />Add category</button></div>
      <ErrorBox message={error || (!edit && err)} /><Notice>{ok}</Notice>
      {data.length === 0 ? <Empty title="No categories" /> :
        <div className="table-wrap"><table><thead><tr><th>Name</th><th>Description</th><th>Courses</th><th></th></tr></thead><tbody>{data.map((c) => (
          <tr key={c.id}><td><strong>{c.name}</strong></td><td className="muted">{c.description}</td><td>{c.courseCount}</td>
            <td><div className="actions"><button className="btn btn-outline btn-sm" onClick={() => { setErr(''); setEdit(c); }}><Pencil size={14} />Edit</button><button className="btn btn-outline btn-sm" onClick={() => setDel(c)}><Trash2 size={14} />Delete</button></div></td></tr>))}</tbody></table></div>}
      {edit && <Modal title={edit.id ? 'Edit category' : 'Add category'} onClose={() => setEdit(null)}><form onSubmit={save}><ErrorBox message={err} />
        <Field label="Name"><input className="input" required maxLength={80} value={edit.name} onChange={(e) => setEdit({ ...edit, name: e.target.value })} /></Field>
        <Field label="Description"><input className="input" maxLength={300} value={edit.description || ''} onChange={(e) => setEdit({ ...edit, description: e.target.value })} /></Field>
        <div className="row end gap"><button type="button" className="btn btn-outline" onClick={() => setEdit(null)}>Cancel</button><button className="btn btn-primary" disabled={busy}>Save</button></div></form></Modal>}
      {del && <Confirm title="Delete category" message={`Delete “${del.name}”? This cannot be undone.`} onConfirm={remove} onCancel={() => setDel(null)} busy={busy} />}
    </>
  );
}
