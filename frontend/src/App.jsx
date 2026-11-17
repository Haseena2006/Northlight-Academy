import { Link, Route, Routes } from 'react-router-dom';
import PublicLayout from './layouts/PublicLayout.jsx';
import DashboardLayout from './layouts/DashboardLayout.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';
import Home from './pages/public/Home.jsx';
import Catalogue from './pages/public/Catalogue.jsx';
import CourseDetail from './pages/public/CourseDetail.jsx';
import VerifyCertificate from './pages/public/VerifyCertificate.jsx';
import Login from './pages/auth/Login.jsx';
import Register from './pages/auth/Register.jsx';
import { ForgotPassword, ResetPassword } from './pages/auth/PasswordPages.jsx';
import Profile from './pages/Profile.jsx';
import StudentDashboard from './pages/student/StudentDashboard.jsx';
import MyCourses from './pages/student/MyCourses.jsx';
import Learn from './pages/student/Learn.jsx';
import Certificates from './pages/student/Certificates.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AdminCourses from './pages/admin/AdminCourses.jsx';
import AdminCategories from './pages/admin/AdminCategories.jsx';
import AdminStudents from './pages/admin/AdminStudents.jsx';
import AdminInstructors from './pages/admin/AdminInstructors.jsx';
import AdminEnrollments from './pages/admin/AdminEnrollments.jsx';
import InstructorDashboard from './pages/instructor/InstructorDashboard.jsx';
import InstructorCourses from './pages/instructor/InstructorCourses.jsx';
import InstructorCourse from './pages/instructor/InstructorCourse.jsx';
import { Empty } from './components/ui.jsx';

const NotFound = () => <div className="container section"><Empty title="Page not found" text="The page you are looking for does not exist." action={<Link className="btn btn-primary" to="/">Back to home</Link>} /></div>;

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/courses" element={<Catalogue />} />
        <Route path="/courses/:id" element={<CourseDetail />} />
        <Route path="/verify" element={<VerifyCertificate />} />
        <Route path="/verify/:code" element={<VerifyCertificate />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="*" element={<NotFound />} />
      </Route>

      <Route element={<ProtectedRoute roles={['STUDENT']} />}>
        <Route path="/student" element={<DashboardLayout area="student" />}>
          <Route index element={<StudentDashboard />} />
          <Route path="courses" element={<MyCourses />} />
          <Route path="learn/:courseId" element={<Learn />} />
          <Route path="certificates" element={<Certificates />} />
          <Route path="profile" element={<Profile />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={['ADMIN']} />}>
        <Route path="/admin" element={<DashboardLayout area="admin" />}>
          <Route index element={<AdminDashboard />} />
          <Route path="courses" element={<AdminCourses />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="students" element={<AdminStudents />} />
          <Route path="instructors" element={<AdminInstructors />} />
          <Route path="enrollments" element={<AdminEnrollments />} />
          <Route path="profile" element={<Profile />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={['INSTRUCTOR']} />}>
        <Route path="/instructor" element={<DashboardLayout area="instructor" />}>
          <Route index element={<InstructorDashboard />} />
          <Route path="courses" element={<InstructorCourses />} />
          <Route path="courses/:courseId" element={<InstructorCourse />} />
          <Route path="profile" element={<Profile />} />
        </Route>
      </Route>
    </Routes>
  );
}
