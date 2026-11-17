import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { BarChart3, BookOpen, CheckCircle2, Clock, Lock, User } from 'lucide-react';
import api, { errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { Badge, ErrorBox, Notice, Spinner, Thumb } from '../../components/ui.jsx';
import LessonReading from '../../components/LessonReading.jsx';
import { fmtDuration, titleCase } from '../../utils/format.js';

export default function CourseDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const nav = useNavigate();
  const isStudent = user?.roles.includes('STUDENT');
  const course = useFetch(() => api.get(`/courses/${id}`).then((r) => r.data), [id]);
  const mine = useFetch(() => (isStudent ? api.get('/student/enrollments').then((r) => r.data) : Promise.resolve([])), [isStudent, id]);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');
  const [preview, setPreview] = useState(null);

  if (course.loading) return <Spinner full />;
  if (course.error) return <div className="container section"><ErrorBox message={course.error} onRetry={course.reload} /><Link to="/courses">Back to catalogue</Link></div>;
  const { summary: s, description, learningOutcomes, prerequisites = [], lessons } = course.data;
  const enrolled = mine.data?.some((e) => e.course.id === s.id);

  const enroll = async () => {
    setBusy(true); setErr('');
    try { await api.post(`/student/enrollments/${s.id}`); nav(`/student/learn/${s.id}`); }
    catch (e) { setErr(errMsg(e)); setBusy(false); }
  };

  let cta;
  if (!user) cta = <Link className="btn btn-primary btn-block" to="/login" state={{ from: `/courses/${s.id}` }}>Sign in to enroll</Link>;
  else if (!isStudent) cta = <Notice type="info">Staff accounts cannot enroll in courses.</Notice>;
  else if (enrolled) cta = <Link className="btn btn-primary btn-block" to={`/student/learn/${s.id}`}>Continue learning</Link>;
  else cta = <button className="btn btn-primary btn-block" disabled={busy || mine.loading} onClick={enroll}>{busy ? 'Enrolling…' : 'Enroll in this course'}</button>;

  return (
    <>
      <section className="detail-hero"><div className="container detail-hero-grid">
        <div><span className="eyebrow" style={{ color: '#D5B878' }}>{s.categoryName}</span>
        <h1>{s.title}</h1>
        <p style={{ color: '#C9D4E4', maxWidth: 720 }}>{s.shortDescription}</p>
        <div className="meta">
          <span><User size={15} />{s.instructors.length > 0 ? s.instructors.map((i) => i.fullName).join(', ') : 'Instructor to be announced'}</span>
          <span><Clock size={15} />{fmtDuration(s.durationMinutes)}</span><span><BarChart3 size={15} />{titleCase(s.difficulty)}</span><span>{s.lessonCount} lessons</span>
        </div></div><Thumb src={s.thumbnailUrl} title={`${s.title} course cover`} />
      </div></section>
      <div className="container section-sm">
        <div className="detail-grid">
          <div className="stack">
            <div className="card card-pad"><h2>About this course</h2><p style={{ whiteSpace: 'pre-line' }}>{description}</p></div>
            {learningOutcomes.length > 0 && <div className="card card-pad"><h2>What you will learn</h2><ul className="outcomes">{learningOutcomes.map((o) => <li key={o}><CheckCircle2 size={18} />{o}</li>)}</ul></div>}
            {prerequisites.length > 0 && <div className="card card-pad"><h2>Before you begin</h2><ul className="outcomes">{prerequisites.map((p) => <li key={p}><CheckCircle2 size={18} />{p}</li>)}</ul></div>}
            <div className="card card-pad">
              <h2>Curriculum</h2>
              {lessons.length === 0 ? <p className="muted">Lessons will be added soon.</p> : (
                <ul className="curriculum">
                  {lessons.map((l) => (
                    <li key={l.id}>
                      {l.preview ? <BookOpen size={18} color="#2857A4" /> : <Lock size={18} color="#66758A" />}
                      <span className="t">{l.position}. {l.title}</span>
                      {l.preview && <Badge tone="success">Preview</Badge>}
                      {l.preview && <button className="btn btn-sm btn-outline" onClick={() => setPreview(preview?.id === l.id ? null : l)}>{preview?.id === l.id ? 'Hide reading' : 'Read sample'}</button>}
                      <span className="muted small">{fmtDuration(l.durationMinutes)}</span>
                    </li>
                  ))}
                </ul>
              )}
              {preview && <article className="card card-pad preview-reading" style={{ marginTop: 16 }}><h3>{preview.title}</h3><LessonReading content={preview.description} /></article>}
            </div>
          </div>
          <aside className="card course-enroll-panel" style={{ position: 'sticky', top: 90 }}>
            <div className="card-pad">
              <ErrorBox message={err} />
              {cta}
              <p className="muted small" style={{ marginTop: 12, marginBottom: 0 }}>Progress is saved to your account and available on any device.</p>
            </div>
          </aside>
        </div>
      </div>
    </>
  );
}
