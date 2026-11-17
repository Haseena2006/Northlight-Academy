import { Link } from 'react-router-dom';
import { Award, BookOpen, CheckCircle2, TrendingUp } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { Empty, ErrorBox, ProgressBar, Spinner, StatCard, Thumb } from '../../components/ui.jsx';
import { fmtDate } from '../../utils/format.js';

export default function StudentDashboard() {
  const { user } = useAuth();
  const { data: d, loading, error, reload } = useFetch(() => api.get('/student/dashboard').then((r) => r.data));
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <div className="page-head"><div><h1>Welcome back, {user.fullName.split(' ')[0]}</h1><p className="muted" style={{ margin: 0 }}>Here is where your learning stands.</p></div><Link className="btn btn-primary" to="/courses">Browse courses</Link></div>
      <div className="grid grid-4" style={{ marginBottom: 24 }}>
        <StatCard icon={BookOpen} label="Enrolled courses" value={d.enrolled} />
        <StatCard icon={TrendingUp} label="In progress" value={d.inProgress} />
        <StatCard icon={CheckCircle2} label="Completed" value={d.completed} />
        <StatCard icon={Award} label="Overall progress" value={`${d.overallProgress}%`} />
      </div>
      <div className="grid grid-2">
        <section className="card card-pad">
          <h2>Continue learning</h2>
          {d.continueLearning.length === 0 ? <Empty title="Nothing in progress" text="Enroll in a course to get started." action={<Link className="btn btn-outline btn-sm" to="/courses">Find a course</Link>} /> :
            <div className="stack">{d.continueLearning.map((e) => (
              <div key={e.id} className="row gap" style={{ alignItems: 'flex-start' }}>
                <div style={{ width: 96, flex: 'none' }}><Thumb src={e.course.thumbnailUrl} title={e.course.title} /></div>
                <div style={{ flex: 1, minWidth: 0 }}><strong>{e.course.title}</strong><ProgressBar value={e.progress} /><span className="muted small">{e.progress}% · {e.completedLessons}/{e.totalLessons} lessons</span></div>
                <Link className="btn btn-sm btn-primary" to={`/student/learn/${e.course.id}`}>Resume</Link>
              </div>))}</div>}
        </section>
        <section className="card card-pad">
          <h2>Recent activity</h2>
          {d.recent.length === 0 ? <p className="muted">Completed lessons will appear here.</p> :
            <ul className="curriculum">{d.recent.map((a, i) => <li key={i}><CheckCircle2 size={18} color="#16845B" /><span className="t"><strong>{a.lessonTitle}</strong><br /><span className="muted small">{a.courseTitle}</span></span><span className="muted small">{fmtDate(a.completedAt)}</span></li>)}</ul>}
        </section>
      </div>
    </>
  );
}
