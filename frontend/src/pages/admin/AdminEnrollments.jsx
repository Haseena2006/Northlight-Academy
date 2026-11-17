import { useState } from 'react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Badge, Empty, ErrorBox, Pagination, ProgressBar, Spinner } from '../../components/ui.jsx';
import { fmtDate } from '../../utils/format.js';

export default function AdminEnrollments() {
  const [courseId, setCourseId] = useState(''); const [page, setPage] = useState(0);
  const courses = useFetch(() => api.get('/admin/courses', { params: { size: 50, sort: 'title' } }).then((r) => r.data.content));
  const { data, loading, error, reload } = useFetch(() => api.get('/admin/enrollments', { params: { courseId: courseId || undefined, page, size: 10 } }).then((r) => r.data), [courseId, page]);
  return (
    <>
      <div className="page-head"><h1>Enrollments and progress</h1></div>
      <ErrorBox message={error} onRetry={reload} />
      <div className="filters"><select className="input" value={courseId} onChange={(e) => { setCourseId(e.target.value); setPage(0); }} aria-label="Course"><option value="">All courses</option>{courses.data?.map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}</select></div>
      {loading ? <Spinner /> : !data || data.content.length === 0 ? <Empty title="No enrollments" /> : <>
        <div className="table-wrap"><table><thead><tr><th>Student</th><th>Course</th><th>Enrolled</th><th style={{ width: 180 }}>Progress</th><th>Status</th></tr></thead><tbody>{data.content.map((e) => (
          <tr key={e.id}><td><strong>{e.studentName}</strong><br /><span className="muted small">{e.studentEmail}</span></td><td>{e.courseTitle}</td><td>{fmtDate(e.enrolledAt)}</td>
            <td><div className="row gap"><div style={{ flex: 1 }}><ProgressBar value={e.progress} /></div><span className="small">{e.progress}%</span></div></td>
            <td>{e.status === 'COMPLETED' ? <Badge tone="success">Completed</Badge> : <Badge>Active</Badge>}</td></tr>))}</tbody></table></div>
        <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} /></>}
    </>
  );
}
