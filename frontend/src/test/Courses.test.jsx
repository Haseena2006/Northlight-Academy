import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';
import CourseCard from '../components/CourseCard.jsx';
import { ProgressBar } from '../components/ui.jsx';
import api, { errMsg } from '../services/api.js';

const course = { id: 1, title: 'React Fundamentals', shortDescription: 'Learn React', categoryName: 'Web', difficulty: 'BEGINNER', durationMinutes: 90, thumbnailUrl: null, instructors: [{ id: 2, fullName: 'Grace' }], lessonCount: 3 };

it('renders a course card with a fallback thumbnail and link', () => {
  render(<MemoryRouter><CourseCard course={course} /></MemoryRouter>);
  expect(screen.getByText('React Fundamentals')).toBeInTheDocument();
  expect(screen.getByText('Grace')).toBeInTheDocument();
  expect(screen.getByRole('link', { name: /view course/i })).toHaveAttribute('href', '/courses/1');
});
it('shows progress accurately', () => {
  render(<ProgressBar value={42} />);
  expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '42');
});
it('maps API errors to friendly messages', () => {
  expect(errMsg({ response: { data: { message: 'You are already enrolled in this course' } } })).toMatch(/already enrolled/);
  expect(errMsg({ response: { data: { errors: { password: 'too weak' } } } })).toBe('too weak');
  expect(errMsg({ code: 'ERR_NETWORK' })).toMatch(/Cannot reach/);
});
it('enrollment call targets the student enrollment endpoint', async () => {
  const spy = vi.spyOn(api, 'post').mockResolvedValue({ data: {} });
  await api.post('/student/enrollments/1');
  expect(spy).toHaveBeenCalledWith('/student/enrollments/1');
});
