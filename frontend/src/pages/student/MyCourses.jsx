import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Award } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Badge, Empty, ErrorBox, ProgressBar, Spinner, Thumb } from '../../components/ui.jsx';

export default function MyCourses() {
  const { data, loading, error, reload } = useFetch(() => api.get('/student/enrollments').then((r) => r.data));
  const [q, setQ] = useState(''); const [status, setStatus] = useState('');
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  const list = data.filter((e) => e.course.title.toLowerCase().includes(q.toLowerCase()) && (!status || e.status === status));
  return (
    <>
      <div className="page-head"><h1>My courses</h1></div>
      <div className="filters">
        <input className="input grow" placeholder="Search my courses" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search my courses" />
        <select className="input" value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Status"><option value="">All</option><option value="ACTIVE">In progress</option><option value="COMPLETED">Completed</option></select>
      </div>
      {data.length === 0 ? <Empty title="You have not enrolled in any courses" text="Browse the catalogue to find your first course." action={<Link className="btn btn-primary" to="/courses">Browse courses</Link>} /> :
        list.length === 0 ? <Empty title="No matching courses" /> :
        <div className="grid grid-3">{list.map((e) => (
          <article key={e.id} className="card course-card">
            <Thumb src={e.course.thumbnailUrl} title={e.course.title} />
            <div className="body">
              <div className="row between"><span className="eyebrow">{e.course.categoryName}</span>{e.status === 'COMPLETED' ? <Badge tone="success">Completed</Badge> : <Badge>In progress</Badge>}</div>
              <h3>{e.course.title}</h3>
              <span className="muted small">{e.course.instructors.map((i) => i.fullName).join(', ') || 'Northlight Academy'}</span>
              <ProgressBar value={e.progress} /><span className="muted small">{e.progress}% · {e.completedLessons}/{e.totalLessons} lessons</span>
              <div className="row gap" style={{ marginTop: 8 }}>
                <Link className="btn btn-primary btn-sm" to={`/student/learn/${e.course.id}`}>{e.status === 'COMPLETED' ? 'Review' : 'Continue learning'}</Link>
                {e.certificateCode && <Link className="btn btn-outline btn-sm" to="/student/certificates"><Award size={14} />Certificate</Link>}
              </div>
            </div>
          </article>))}</div>}
    </>
  );
}
