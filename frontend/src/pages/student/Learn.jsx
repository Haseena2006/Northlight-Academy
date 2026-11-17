import { useEffect, useMemo, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { Award, BookOpen, CheckCircle2, ChevronLeft, ChevronRight, Circle, Clock3 } from 'lucide-react';
import api, { errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Badge, ErrorBox, ProgressBar, Spinner } from '../../components/ui.jsx';
import LessonReading from '../../components/LessonReading.jsx';

const readingMinutes = (content) => {
  const prose = (content || '').replace(/```[\s\S]*?```/g, ' ');
  const words = prose.trim() ? prose.trim().split(/\s+/).length : 0;
  return Math.max(1, Math.ceil(words / 220));
};

export default function Learn() {
  const { courseId } = useParams();
  const [sp, setSp] = useSearchParams();
  const { data, setData, loading, error, reload } = useFetch(() => api.get(`/student/courses/${courseId}/lessons`).then((r) => r.data), [courseId]);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState('');

  const lessons = data?.lessons || [];
  const wanted = Number(sp.get('lesson'));
  const current = lessons.find((lesson) => lesson.id === wanted)
    || lessons.find((lesson) => lesson.id === data?.lastLessonId)
    || lessons.find((lesson) => !lesson.completed)
    || lessons[0];
  const idx = current ? lessons.indexOf(current) : -1;
  const minutes = useMemo(() => readingMinutes(current?.description), [current?.description]);

  useEffect(() => {
    if (current && data && current.id !== data.lastLessonId) {
      api.post('/student/progress/last-accessed', { courseId: Number(courseId), lessonId: current.id }).catch(() => {});
    }
    // lastLessonId is intentionally not a trigger: changing the current lesson is what records access.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current?.id]);

  if (loading) return <Spinner />;
  if (error) return <><ErrorBox message={error} onRetry={reload} /><Link to="/student/courses">Back to my courses</Link></>;
  if (!current) return <p>This course has no lessons yet.</p>;

  const go = (lesson) => lesson && setSp({ lesson: lesson.id });
  const complete = async () => {
    setBusy(true); setErr('');
    try {
      const { data: progress } = await api.post('/student/progress', { courseId: Number(courseId), lessonId: current.id });
      setData({
        ...data,
        progress: progress.progress,
        status: progress.status,
        certificateCode: progress.certificateCode,
        lastLessonId: current.id,
        lessons: lessons.map((lesson) => lesson.id === current.id ? { ...lesson, completed: true } : lesson),
      });
      if (progress.status !== 'COMPLETED' && lessons[idx + 1]) go(lessons[idx + 1]);
    } catch (e) { setErr(errMsg(e)); } finally { setBusy(false); }
  };

  return (
    <>
      <div className="page-head learn-heading">
        <div><Link to="/student/courses" className="small">← My courses</Link><h1>{data.courseTitle}</h1></div>
        <div className="learn-progress"><div className="row between small muted"><span>Course progress</span><strong>{data.progress}%</strong></div><ProgressBar value={data.progress} /></div>
      </div>
      {data.status === 'COMPLETED' && <div className="alert alert-success"><Award size={18} /><span>Course completed. Your certificate is ready.</span><Link className="btn btn-sm btn-primary" to="/student/certificates">View certificate</Link></div>}
      <div className="learn">
        <main className="learn-main">
          <div className="lesson-meta row between wrap gap">
            <span className="eyebrow">Lesson {current.position} of {lessons.length}</span>
            <div className="row gap small muted"><span className="row gap"><Clock3 size={15} />{minutes} min read</span>{current.completed && <Badge tone="success">Completed</Badge>}</div>
          </div>
          <article className="card lesson-reading">
            <div className="lesson-reading-title"><BookOpen size={20} /><span>Reading lesson</span></div>
            <h2>{current.title}</h2>
            <LessonReading content={current.description || 'Reading material for this lesson is being prepared.'} />
          </article>
          <ErrorBox message={err} />
          <nav className="lesson-nav" aria-label="Lesson navigation">
            <button className="btn btn-outline" disabled={idx === 0} onClick={() => go(lessons[idx - 1])}><ChevronLeft size={16} />Previous lesson</button>
            <button className="btn btn-primary" disabled={busy || current.completed} onClick={complete}>
              <CheckCircle2 size={16} />{current.completed ? 'Completed' : busy ? 'Saving…' : 'Mark as complete'}
            </button>
            <button className="btn btn-outline" disabled={idx === lessons.length - 1} onClick={() => go(lessons[idx + 1])}>Next lesson<ChevronRight size={16} /></button>
          </nav>
        </main>
        <aside className="card learn-outline">
          <div className="card-pad learn-outline-head"><h3>Course content</h3><p className="muted small">{lessons.filter((lesson) => lesson.completed).length} of {lessons.length} lessons complete</p><ProgressBar value={data.progress} /></div>
          <ul className="lesson-list">{lessons.map((lesson) => (
            <li key={lesson.id}><button className={lesson.id === current.id ? 'cur' : ''} aria-current={lesson.id === current.id ? 'step' : undefined} onClick={() => go(lesson)}>
              {lesson.completed ? <CheckCircle2 size={18} className="done" /> : <Circle size={18} className="todo" />}
              <span className="learn-outline-title">{lesson.position}. {lesson.title}<br /><span className="muted small">{readingMinutes(lesson.description)} min read</span></span>
            </button></li>
          ))}</ul>
        </aside>
      </div>
    </>
  );
}
