import { BookOpen, CheckCircle2, Users } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Empty, ErrorBox, ProgressBar, Spinner, StatCard } from '../../components/ui.jsx';
import { Link } from 'react-router-dom';

export default function InstructorDashboard() {
  const { data: d, loading, error, reload } = useFetch(() => api.get('/instructor/dashboard').then((r) => r.data));
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <div className="page-head"><h1>Instructor overview</h1></div>
      <div className="grid grid-3" style={{ marginBottom: 24 }}>
        <StatCard icon={BookOpen} label="Assigned courses" value={d.assignedCourses} /><StatCard icon={Users} label="Enrolled students" value={d.totalStudents} /><StatCard icon={CheckCircle2} label="Completed enrollments" value={d.completedEnrollments} />
      </div>
      <section className="card card-pad"><h2>Course performance</h2>
        {d.courses.length === 0 ? <Empty title="No courses assigned" text="An administrator can assign courses to you." /> :
          <div className="table-wrap" style={{ boxShadow: 'none' }}><table><thead><tr><th>Course</th><th>Enrolled</th><th>Completed</th><th style={{ width: 200 }}>Completion</th><th></th></tr></thead><tbody>{d.courses.map((c) => (
            <tr key={c.courseId}><td>{c.title}</td><td>{c.enrolled}</td><td>{c.completed}</td><td><div className="row gap"><div style={{ flex: 1 }}><ProgressBar value={c.completionRate} /></div><span className="small">{c.completionRate}%</span></div></td><td><Link className="btn btn-outline btn-sm" to={`/instructor/courses/${c.courseId}`}>Manage</Link></td></tr>))}</tbody></table></div>}
      </section>
    </>
  );
}
