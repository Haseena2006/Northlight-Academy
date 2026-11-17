import { useEffect, useState } from 'react';
import { Eye, EyeOff, ListChecks, Pencil, Plus, Trash2 } from 'lucide-react';
import api, { errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Badge, Confirm, Empty, ErrorBox, Field, Modal, Notice, Pagination, Spinner, Thumb } from '../../components/ui.jsx';
import LessonManager from '../../components/LessonManager.jsx';
import { fmtDuration, titleCase } from '../../utils/format.js';

const BLANK = { title: '', shortDescription: '', description: '', categoryId: '', difficulty: 'BEGINNER', durationMinutes: 60, outcomes: '', prerequisites: '', instructorIds: [] };

export default function AdminCourses() {
  const [q, setQ] = useState(''); const [dq, setDq] = useState(''); const [page, setPage] = useState(0);
  useEffect(() => { const t = setTimeout(() => { setDq(q); setPage(0); }, 350); return () => clearTimeout(t); }, [q]);
  const list = useFetch(() => api.get('/admin/courses', { params: { q: dq || undefined, page, size: 10 } }).then((r) => r.data), [dq, page]);
  const cats = useFetch(() => api.get('/categories').then((r) => r.data));
  const instr = useFetch(() => api.get('/admin/instructors', { params: { size: 50 } }).then((r) => r.data.content));
  const [form, setForm] = useState(null); const [file, setFile] = useState(null); const [del, setDel] = useState(null); const [lessons, setLessons] = useState(null);
  const [err, setErr] = useState(''); const [ok, setOk] = useState(''); const [busy, setBusy] = useState(false);

  const openEdit = async (c) => {
    setErr(''); setFile(null);
    if (!c) return setForm({ ...BLANK, categoryId: cats.data?.[0]?.id || '' });
    try { const { data } = await api.get(`/admin/courses/${c.id}`); setForm({ id: c.id, title: data.summary.title, shortDescription: data.summary.shortDescription, description: data.description, categoryId: data.summary.categoryId, difficulty: data.summary.difficulty, durationMinutes: data.summary.durationMinutes, outcomes: data.learningOutcomes.join('\n'), prerequisites: (data.prerequisites || []).join('\n'), instructorIds: data.summary.instructors.map((i) => i.id) }); }
    catch (e) { setErr(errMsg(e)); }
  };
  const save = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true);
    const body = { title: form.title, shortDescription: form.shortDescription, description: form.description, categoryId: Number(form.categoryId), difficulty: form.difficulty, durationMinutes: Number(form.durationMinutes), learningOutcomes: form.outcomes.split('\n').map((s) => s.trim()).filter(Boolean), prerequisites: form.prerequisites.split('\n').map((s) => s.trim()).filter(Boolean), instructorIds: form.instructorIds };
    try {
      const { data } = form.id ? await api.put(`/admin/courses/${form.id}`, body) : await api.post('/admin/courses', body);
      if (file) { const fd = new FormData(); fd.append('file', file); await api.post(`/admin/courses/${data.summary.id}/thumbnail`, fd); }
      setForm(null); setOk('Course saved.'); list.reload();
    } catch (x) { setErr(errMsg(x)); } finally { setBusy(false); }
  };
  const toggle = async (c) => { setErr(''); setOk(''); try { await api.patch(`/admin/courses/${c.id}/status`, { published: !c.published }); list.reload(); } catch (x) { setErr(errMsg(x)); } };
  const remove = async () => { setBusy(true); try { await api.delete(`/admin/courses/${del.id}`); setDel(null); setOk('Course deleted.'); list.reload(); } catch (x) { setDel(null); setErr(errMsg(x)); } finally { setBusy(false); } };
  const d = list.data;
  return (
    <>
      <div className="page-head"><h1>Courses</h1><button className="btn btn-primary" onClick={() => openEdit(null)}><Plus size={16} />Add course</button></div>
      <ErrorBox message={list.error || (!form && err)} /><Notice>{ok}</Notice>
      <div className="filters"><input className="input grow" placeholder="Search courses" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search courses" /></div>
      {list.loading ? <Spinner /> : !d || d.content.length === 0 ? <Empty title="No courses found" /> : <>
        <div className="table-wrap"><table><thead><tr><th>Course</th><th>Category</th><th>Level</th><th>Duration</th><th>Lessons</th><th>Status</th><th></th></tr></thead><tbody>{d.content.map((c) => (
          <tr key={c.id}><td><div className="row gap"><div style={{ width: 72, flex: 'none' }}><Thumb src={c.thumbnailUrl} title={c.title} /></div><div><strong>{c.title}</strong><br /><span className="muted small">{c.instructors.map((i) => i.fullName).join(', ') || 'No instructor assigned'}</span></div></div></td>
            <td>{c.categoryName}</td><td>{titleCase(c.difficulty)}</td><td>{fmtDuration(c.durationMinutes)}</td><td>{c.lessonCount}</td><td>{c.published ? <Badge tone="success">Published</Badge> : <Badge tone="muted">Draft</Badge>}</td>
            <td><div className="actions">
              <button className="btn btn-outline btn-sm" onClick={() => setLessons(c)}><ListChecks size={14} />Lessons</button>
              <button className="btn btn-outline btn-sm" onClick={() => openEdit(c)}><Pencil size={14} />Edit</button>
              <button className="btn btn-outline btn-sm" onClick={() => toggle(c)}>{c.published ? <><EyeOff size={14} />Unpublish</> : <><Eye size={14} />Publish</>}</button>
              <button className="btn btn-outline btn-sm" onClick={() => setDel(c)} aria-label={`Delete ${c.title}`}><Trash2 size={14} /></button></div></td></tr>))}</tbody></table></div>
        <Pagination page={d.page} totalPages={d.totalPages} onChange={setPage} /></>}

      {form && <Modal title={form.id ? 'Edit course' : 'Add course'} onClose={() => setForm(null)} wide><form onSubmit={save}><ErrorBox message={err} />
        <Field label="Title"><input className="input" required maxLength={200} value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></Field>
        <Field label="Short description"><input className="input" required maxLength={300} value={form.shortDescription} onChange={(e) => setForm({ ...form, shortDescription: e.target.value })} /></Field>
        <Field label="Description"><textarea className="input" required value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
        <div className="grid grid-3" style={{ gap: 16 }}>
          <Field label="Category"><select className="input" required value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}><option value="" disabled>Select…</option>{cats.data?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select></Field>
          <Field label="Difficulty"><select className="input" value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: e.target.value })}><option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="ADVANCED">Advanced</option></select></Field>
          <Field label="Duration (minutes)"><input className="input" type="number" min="1" required value={form.durationMinutes} onChange={(e) => setForm({ ...form, durationMinutes: e.target.value })} /></Field>
        </div>
        <Field label="Learning outcomes" hint="One per line"><textarea className="input" value={form.outcomes} onChange={(e) => setForm({ ...form, outcomes: e.target.value })} /></Field>
        <Field label="Prerequisites" hint="One per line; leave blank if none"><textarea className="input" value={form.prerequisites} onChange={(e) => setForm({ ...form, prerequisites: e.target.value })} /></Field>
        <Field label="Instructors"><div className="checklist">{instr.data?.length ? instr.data.map((i) => <label key={i.id}><input type="checkbox" checked={form.instructorIds.includes(i.id)} onChange={(e) => setForm({ ...form, instructorIds: e.target.checked ? [...form.instructorIds, i.id] : form.instructorIds.filter((x) => x !== i.id) })} />{i.fullName}</label>) : <span className="muted small">No instructors yet</span>}</div></Field>
        <Field label="Thumbnail" hint="JPEG, PNG or WebP, up to 2 MB"><input className="input" type="file" accept="image/jpeg,image/png,image/webp" onChange={(e) => setFile(e.target.files[0] || null)} /></Field>
        <div className="row end gap"><button type="button" className="btn btn-outline" onClick={() => setForm(null)}>Cancel</button><button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save course'}</button></div></form></Modal>}
      {lessons && <Modal title={`Lessons — ${lessons.title}`} onClose={() => { setLessons(null); list.reload(); }} wide><LessonManager base="/admin" courseId={lessons.id} /></Modal>}
      {del && <Confirm title="Delete course" message={`Delete “${del.title}” and its lessons? This cannot be undone.`} onConfirm={remove} onCancel={() => setDel(null)} busy={busy} />}
    </>
  );
}
