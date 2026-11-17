import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import LessonManager from '../../components/LessonManager.jsx';
import { Badge, Empty, ErrorBox, ProgressBar, Spinner } from '../../components/ui.jsx';
import { fmtDate } from '../../utils/format.js';

export default function InstructorCourse() {
  const { courseId } = useParams();
  const [tab, setTab] = useState('lessons');
  const students = useFetch(() => api.get(`/instructor/courses/${courseId}/students`).then((r) => r.data), [courseId]);
  return (
    <>
      <div className="page-head"><div><Link to="/instructor/courses" className="small">← My courses</Link><h1>Course workspace</h1></div></div>
      <div className="tabs" role="tablist"><button role="tab" className={tab === 'lessons' ? 'on' : ''} onClick={() => setTab('lessons')}>Lessons</button><button role="tab" className={tab === 'students' ? 'on' : ''} onClick={() => setTab('students')}>Students</button></div>
      {tab === 'lessons' ? <LessonManager base="/instructor" courseId={courseId} /> : students.loading ? <Spinner /> : students.error ? <ErrorBox message={students.error} onRetry={students.reload} /> :
        students.data.length === 0 ? <Empty title="No students enrolled yet" /> :
        <div className="table-wrap"><table><thead><tr><th>Student</th><th>Enrolled</th><th style={{ width: 200 }}>Progress</th><th>Status</th></tr></thead><tbody>{students.data.map((e) => (
          <tr key={e.id}><td><strong>{e.studentName}</strong><br /><span className="muted small">{e.studentEmail}</span></td><td>{fmtDate(e.enrolledAt)}</td><td><div className="row gap"><div style={{ flex: 1 }}><ProgressBar value={e.progress} /></div><span className="small">{e.progress}%</span></div></td><td>{e.status === 'COMPLETED' ? <Badge tone="success">Completed</Badge> : <Badge>Active</Badge>}</td></tr>))}</tbody></table></div>}
    </>
  );
}
