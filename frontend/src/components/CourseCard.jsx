import { Link } from 'react-router-dom';
import { BarChart3, Clock, User } from 'lucide-react';
import { Badge, Thumb } from './ui.jsx';
import { fmtDuration, titleCase } from '../utils/format.js';

export default function CourseCard({ course }) {
  const instructor = course.instructors?.map((i) => i.fullName).join(', ') || 'Instructor to be announced';
  return (
    <Link className="card course-card" to={`/courses/${course.id}`} aria-label={`View course: ${course.title}`}>
      <Thumb src={course.thumbnailUrl} title={course.title} />
      <div className="body">
        <span className="eyebrow">{course.categoryName}</span>
        <h3>{course.title}</h3>
        <p className="desc">{course.shortDescription}</p>
        <div className="meta">
          <span title={instructor}><User size={14} />{instructor}</span>
          <span><Clock size={14} />{fmtDuration(course.durationMinutes)}</span>
          <span><BarChart3 size={14} />{titleCase(course.difficulty)}</span>
        </div>
        <div className="row between course-card-bottom">
          <Badge tone="muted">{course.lessonCount} lessons</Badge>
          <span className="btn btn-outline btn-sm">View course</span>
        </div>
      </div>
    </Link>
  );
}
