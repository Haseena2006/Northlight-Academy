import { useState } from 'react';
import { ArrowDown, ArrowUp, Pencil, Plus, Trash2 } from 'lucide-react';
import api, { errMsg } from '../services/api.js';
import useFetch from '../hooks/useFetch.js';
import { Badge, Confirm, Empty, ErrorBox, Field, Modal, Spinner } from './ui.jsx';
import { fmtDuration } from '../utils/format.js';

const BLANK = { title: '', description: '', videoUrl: '', durationMinutes: 0, preview: false, required: true };

/** base is '/admin' or '/instructor'; the backend enforces which courses each role may touch. */
export default function LessonManager({ base, courseId }) {
  const { data, setData, loading, error, reload } = useFetch(() => api.get(`${base}/courses/${courseId}/lessons`).then((r) => r.data), [base, courseId]);
  const [edit, setEdit] = useState(null); const [del, setDel] = useState(null); const [err, setErr] = useState(''); const [busy, setBusy] = useState(false);

  const save = async (e) => {
    e.preventDefault(); setErr(''); setBusy(true);
    const body = { ...edit, durationMinutes: Number(edit.durationMinutes) || 0, videoUrl: edit.videoUrl?.trim() || null };
    try { edit.id ? await api.put(`${base}/lessons/${edit.id}`, body) : await api.post(`${base}/courses/${courseId}/lessons`, body); setEdit(null); reload(); }
    catch (x) { setErr(errMsg(x)); } finally { setBusy(false); }
  };
  const remove = async () => { setBusy(true); try { await api.delete(`${base}/lessons/${del.id}`); setDel(null); reload(); } catch (x) { setDel(null); setErr(errMsg(x)); } finally { setBusy(false); } };
  const move = async (i, d) => {
    const ids = data.map((l) => l.id); [ids[i], ids[i + d]] = [ids[i + d], ids[i]];
    try { const { data: n } = await api.put(`${base}/courses/${courseId}/lessons/reorder`, { lessonIds: ids }); setData(n); } catch (x) { setErr(errMsg(x)); }
  };
  if (loading) return <Spinner />;
  return (
    <div>
      <ErrorBox message={error || (!edit && err)} />
      <div className="row between" style={{ marginBottom: 12 }}><strong>{data?.length || 0} lessons</strong><button className="btn btn-primary btn-sm" onClick={() => { setErr(''); setEdit({ ...BLANK }); }}><Plus size={14} />Add lesson</button></div>
      {!data?.length ? <Empty title="No lessons yet" text="Add the first lesson to build the curriculum." /> :
        <div className="table-wrap"><table><thead><tr><th>#</th><th>Title</th><th>Duration</th><th>Reading</th><th></th></tr></thead><tbody>{data.map((l, i) => (
          <tr key={l.id}><td>{l.position}</td><td><strong>{l.title}</strong> {l.preview && <Badge tone="success">Preview</Badge>}</td><td>{fmtDuration(l.durationMinutes)}</td><td>{l.description?.trim() ? <Badge>Added</Badge> : <Badge tone="muted">Missing</Badge>}</td>
            <td><div className="actions">
              <button className="icon-btn" disabled={i === 0} onClick={() => move(i, -1)} aria-label="Move up"><ArrowUp size={16} /></button>
              <button className="icon-btn" disabled={i === data.length - 1} onClick={() => move(i, 1)} aria-label="Move down"><ArrowDown size={16} /></button>
              <button className="icon-btn" onClick={() => { setErr(''); setEdit({ ...l, videoUrl: l.videoUrl || '', description: l.description || '' }); }} aria-label="Edit lesson"><Pencil size={16} /></button>
              <button className="icon-btn" onClick={() => setDel(l)} aria-label="Delete lesson"><Trash2 size={16} /></button></div></td></tr>))}</tbody></table></div>}
      {edit && <Modal title={edit.id ? 'Edit lesson' : 'Add lesson'} onClose={() => setEdit(null)}><form onSubmit={save}><ErrorBox message={err} />
        <Field label="Title"><input className="input" required maxLength={200} value={edit.title} onChange={(e) => setEdit({ ...edit, title: e.target.value })} /></Field>
        <Field label="Reading material (Markdown)" hint="Use headings, lists, and fenced code blocks to structure the lesson."><textarea className="input lesson-editor" rows={14} required value={edit.description} onChange={(e) => setEdit({ ...edit, description: e.target.value })} /></Field>
        <Field label="Legacy video URL (optional)" hint="Kept for API compatibility; students use the reading material."><input className="input" value={edit.videoUrl} onChange={(e) => setEdit({ ...edit, videoUrl: e.target.value })} /></Field>
        <Field label="Duration (minutes)"><input className="input" type="number" min="0" value={edit.durationMinutes} onChange={(e) => setEdit({ ...edit, durationMinutes: e.target.value })} /></Field>
        <label className="check"><input type="checkbox" checked={edit.preview} onChange={(e) => setEdit({ ...edit, preview: e.target.checked })} />Free preview lesson</label>
        <label className="check"><input type="checkbox" checked={edit.required} onChange={(e) => setEdit({ ...edit, required: e.target.checked })} />Required for course completion</label>
        <div className="row end gap"><button type="button" className="btn btn-outline" onClick={() => setEdit(null)}>Cancel</button><button className="btn btn-primary" disabled={busy}>Save lesson</button></div></form></Modal>}
      {del && <Confirm title="Delete lesson" message={`Delete “${del.title}”? Student progress for this lesson will be removed.`} onConfirm={remove} onCancel={() => setDel(null)} busy={busy} />}
    </div>
  );
}
