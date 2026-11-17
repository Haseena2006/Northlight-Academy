import { BookOpen, CheckCircle2, ClipboardList, Eye, GraduationCap, Users } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Empty, ErrorBox, ProgressBar, Spinner, StatCard } from '../../components/ui.jsx';

export default function AdminDashboard() {
  const { data: d, loading, error, reload } = useFetch(() => api.get('/admin/dashboard').then((r) => r.data));
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <div className="page-head"><h1>Administration overview</h1></div>
      <div className="grid grid-3" style={{ marginBottom: 24 }}>
        <StatCard icon={Users} label="Registered students" value={d.students} /><StatCard icon={GraduationCap} label="Instructors" value={d.instructors} /><StatCard icon={BookOpen} label="Courses" value={d.courses} />
        <StatCard icon={Eye} label="Published courses" value={d.publishedCourses} /><StatCard icon={ClipboardList} label="Enrollments" value={d.enrollments} /><StatCard icon={CheckCircle2} label="Completed enrollments" value={d.completedEnrollments} />
      </div>
      <section className="card card-pad"><h2>Course completion report</h2>
        {d.courseReports.length === 0 ? <Empty title="No courses yet" /> : <div className="table-wrap" style={{ boxShadow: 'none' }}><table><thead><tr><th>Course</th><th>Enrolled</th><th>Completed</th><th style={{ width: 200 }}>Completion rate</th></tr></thead>
          <tbody>{d.courseReports.map((r) => <tr key={r.courseId}><td>{r.title}</td><td>{r.enrolled}</td><td>{r.completed}</td><td><div className="row gap"><div style={{ flex: 1 }}><ProgressBar value={r.completionRate} /></div><span className="small">{r.completionRate}%</span></div></td></tr>)}</tbody></table></div>}
      </section>
    </>
  );
}
