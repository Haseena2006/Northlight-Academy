import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Link } from 'react-router-dom';
import { Badge, Empty, ErrorBox, Spinner, Thumb } from '../../components/ui.jsx';
import { fmtDuration } from '../../utils/format.js';

export default function InstructorCourses() {
  const { data, loading, error, reload } = useFetch(() => api.get('/instructor/courses').then((r) => r.data));
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <div className="page-head"><h1>My courses</h1></div>
      {data.length === 0 ? <Empty title="No courses assigned" text="An administrator can assign courses to you." /> :
        <div className="grid grid-3">{data.map((c) => (
          <article key={c.id} className="card course-card"><Thumb src={c.thumbnailUrl} title={c.title} />
            <div className="body"><div className="row between"><span className="eyebrow">{c.categoryName}</span>{c.published ? <Badge tone="success">Published</Badge> : <Badge tone="muted">Draft</Badge>}</div>
              <h3>{c.title}</h3><div className="meta"><span>{c.lessonCount} lessons</span><span>{fmtDuration(c.durationMinutes)}</span></div>
              <Link className="btn btn-primary btn-sm" style={{ marginTop: 8 }} to={`/instructor/courses/${c.id}`}>Manage course</Link></div></article>))}</div>}
    </>
  );
}
