import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Learn from '../pages/student/Learn.jsx';

const { api } = vi.hoisted(() => ({ api: { get: vi.fn(), post: vi.fn() } }));
vi.mock('../services/api.js', () => ({ default: api, errMsg: (e) => e?.response?.data?.message || 'Request failed' }));

const sample = (lessons) => ({
  courseId: 8,
  courseTitle: 'Java Programming Masterclass',
  progress: 0,
  status: 'ACTIVE',
  lastLessonId: null,
  lessons,
});

const lesson = (id, position, title, completed = false) => ({
  id,
  title,
  description: `## Introduction\n\nRead about ${title}.\n\n## Example\n\n\`\`\`java\nSystem.out.println("ready");\n\`\`\``,
  durationMinutes: 60,
  position,
  completed,
});

function renderLearn(path = '/student/learn/8') {
  return render(<MemoryRouter initialEntries={[path]}><Routes><Route path="/student/learn/:courseId" element={<Learn />} /></Routes></MemoryRouter>);
}

describe('student reading lessons', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.get.mockResolvedValue({ data: sample([lesson(21, 1, 'Java syntax and types'), lesson(22, 2, 'Object-oriented design')]) });
    api.post.mockImplementation((path) => path.endsWith('/progress/last-accessed')
      ? Promise.resolve({})
      : Promise.resolve({ data: { progress: 50, status: 'ACTIVE', certificateCode: null } }));
  });

  it('renders structured reading, estimates time, and navigates between lessons', async () => {
    const user = userEvent.setup();
    const { container } = renderLearn('/student/learn/8?lesson=21');
    expect(await screen.findByRole('heading', { name: 'Java syntax and types' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Introduction' })).toBeInTheDocument();
    expect(screen.getAllByText('1 min read').length).toBeGreaterThanOrEqual(2);
    expect(container.querySelector('pre code').textContent).toContain('System.out.println');
    expect(screen.getByRole('button', { name: /previous lesson/i })).toBeDisabled();
    await user.click(screen.getByRole('button', { name: /next lesson/i }));
    expect(await screen.findByRole('heading', { name: 'Object-oriented design' })).toBeInTheDocument();
  });

  it('marks completion through the existing progress endpoint and updates course progress', async () => {
    api.get.mockResolvedValue({ data: sample([lesson(21, 1, 'Java syntax and types')]) });
    api.post.mockImplementation((path) => path.endsWith('/progress/last-accessed')
      ? Promise.resolve({})
      : Promise.resolve({ data: { progress: 100, status: 'COMPLETED', certificateCode: 'NL-TEST' } }));
    const user = userEvent.setup();
    renderLearn('/student/learn/8?lesson=21');
    await user.click(await screen.findByRole('button', { name: /mark as complete/i }));
    await waitFor(() => expect(api.post).toHaveBeenCalledWith('/student/progress', { courseId: 8, lessonId: 21 }));
    expect(await screen.findByText(/course completed/i)).toBeInTheDocument();
    expect(screen.getByText('100%')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Completed' })).toBeDisabled();
  });
});
