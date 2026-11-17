import { Link } from 'react-router-dom';
import { Award, BookOpenCheck, LineChart, ShieldCheck } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import CourseCard from '../../components/CourseCard.jsx';
import { ErrorBox, Spinner, Empty } from '../../components/ui.jsx';

const WHY = [
  [BookOpenCheck, 'Structured curricula', 'Courses are organised into ordered lessons so learners always know what comes next.'],
  [LineChart, 'Progress you can trust', 'Completion is recorded on the server, so your progress follows you across devices.'],
  [Award, 'Verifiable certificates', 'Every certificate carries a unique ID that anyone can verify publicly.'],
  [ShieldCheck, 'Secure by design', 'Role-based access, hashed credentials and rotating session tokens protect every account.'],
];

export default function Home() {
  const stats = useFetch(() => api.get('/public/stats').then((r) => r.data));
  const featured = useFetch(() => api.get('/courses', { params: { size: 6, sort: 'newest' } }).then((r) => r.data.content));
  const cats = useFetch(() => api.get('/categories').then((r) => r.data));
  const s = stats.data;
  return (
    <>
      <section className="hero">
        <div className="container hero-grid">
          <div>
            <span className="eyebrow" style={{ color: '#9DB8E3' }}>Professional technology education</span>
            <h1>Build job-ready engineering skills, one lesson at a time.</h1>
            <p className="lead">Northlight Academy offers focused courses in software development, databases and cloud delivery, with tracked progress and verifiable certificates.</p>
            <div className="row gap wrap"><Link to="/courses" className="btn btn-light">Explore courses</Link><Link to="/register" className="btn btn-outline" style={{ background: 'transparent', color: '#fff', borderColor: 'rgba(255,255,255,.4)' }}>Create free account</Link></div>
          </div>
          <div className="hero-panel">
            <div className="num">{s ? s.courses : '—'}</div><div className="lbl">Published courses</div>
            <hr style={{ border: 0, borderTop: '1px solid rgba(255,255,255,.14)', margin: '16px 0' }} />
            <div className="num">{s ? s.students : '—'}</div><div className="lbl">Registered learners</div>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head"><div><span className="eyebrow">Featured</span><h2 style={{ margin: 0 }}>Latest courses</h2></div><Link to="/courses" className="btn btn-outline btn-sm">View all courses</Link></div>
          {featured.loading ? <Spinner /> : featured.error ? <ErrorBox message={featured.error} onRetry={featured.reload} /> :
            featured.data?.length ? <div className="grid grid-3">{featured.data.map((c) => <CourseCard key={c.id} course={c} />)}</div> : <Empty title="No courses published yet" text="Check back soon." />}
        </div>
      </section>

      {cats.data?.length > 0 && (
        <section className="section-sm">
          <div className="container">
            <div className="section-head"><div><span className="eyebrow">Browse</span><h2 style={{ margin: 0 }}>Learning categories</h2></div></div>
            <div className="grid grid-4">
              {cats.data.map((c) => (
                <Link key={c.id} to={`/courses?categoryId=${c.id}`} className="card cat-tile"><h3>{c.name}</h3><p className="muted small" style={{ margin: 0 }}>{c.description}</p></Link>
              ))}
            </div>
          </div>
        </section>
      )}

      <section className="section">
        <div className="container">
          <div className="section-head"><div><span className="eyebrow">Why Northlight</span><h2 style={{ margin: 0 }}>Designed for serious learners</h2></div></div>
          <div className="grid grid-4">
            {WHY.map(([Icon, t, d]) => <div key={t} className="card feature"><div className="stat-icon"><Icon size={20} /></div><h3>{t}</h3><p className="muted small" style={{ margin: 0 }}>{d}</p></div>)}
          </div>
        </div>
      </section>

      <section className="stats-band">
        <div className="container grid grid-4" style={{ textAlign: 'center' }}>
          {[['Published courses', s?.courses], ['Learners', s?.students], ['Enrollments', s?.enrollments], ['Courses completed', s?.completions]].map(([l, v]) => (
            <div key={l}><div className="num">{v ?? '—'}</div><div className="muted small">{l}</div></div>
          ))}
        </div>
      </section>
    </>
  );
}
