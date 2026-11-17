import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { vi } from 'vitest';
import ProtectedRoute from '../routes/ProtectedRoute.jsx';
import Login from '../pages/auth/Login.jsx';
import Register from '../pages/auth/Register.jsx';
import { passwordStrength, roleHome } from '../utils/format.js';

let mockAuth;
vi.mock('../context/AuthContext.jsx', () => ({ useAuth: () => mockAuth }));

const wrap = (ui, path = '/') => render(<MemoryRouter initialEntries={[path]}>{ui}</MemoryRouter>);

describe('login form', () => {
  beforeEach(() => { mockAuth = { login: vi.fn().mockResolvedValue({ roles: ['STUDENT'] }), user: null, loading: false }; });
  it('validates required fields before calling the API', async () => {
    wrap(<Login />);
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));
    expect(screen.getByText(/valid email/i)).toBeInTheDocument();
    expect(mockAuth.login).not.toHaveBeenCalled();
  });
  it('submits credentials', async () => {
    wrap(<Login />);
    await userEvent.type(screen.getByLabelText(/^email/i), 'a@b.com');
    await userEvent.type(screen.getByLabelText(/^password/i), 'Secret123');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));
    expect(mockAuth.login).toHaveBeenCalledWith('a@b.com', 'Secret123', false);
  });
  it('shows a friendly error when authentication fails', async () => {
    mockAuth.login = vi.fn().mockRejectedValue({ response: { data: { message: 'Invalid email or password' } } });
    wrap(<Login />);
    await userEvent.type(screen.getByLabelText(/^email/i), 'a@b.com');
    await userEvent.type(screen.getByLabelText(/^password/i), 'Wrong1234');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password');
  });
});

describe('registration form', () => {
  beforeEach(() => { mockAuth = { register: vi.fn().mockResolvedValue({}), user: null, loading: false }; });
  it('rejects weak passwords and mismatched confirmation', async () => {
    wrap(<Register />);
    await userEvent.type(screen.getByLabelText(/full name/i), 'Ada');
    await userEvent.type(screen.getByLabelText(/^email/i), 'ada@x.com');
    await userEvent.type(screen.getByLabelText('Password', { exact: true }), 'weak');
    await userEvent.click(screen.getByRole('button', { name: /create account/i }));
    expect(screen.getByText(/8\+ characters/i)).toBeInTheDocument();
    expect(screen.getByText(/do not match/i)).toBeInTheDocument();
    expect(screen.getByText(/accept the terms/i)).toBeInTheDocument();
    expect(mockAuth.register).not.toHaveBeenCalled();
  });
});

describe('protected routes and role navigation', () => {
  const tree = (
    <Routes>
      <Route path="/login" element={<div>login page</div>} />
      <Route path="/student" element={<div>student home</div>} />
      <Route path="/admin" element={<div>admin home</div>} />
      <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/secret" element={<div>admin secret</div>} /></Route>
    </Routes>
  );
  it('redirects anonymous users to login', () => { mockAuth = { user: null, loading: false }; wrap(tree, '/admin/secret'); expect(screen.getByText('login page')).toBeInTheDocument(); });
  it('redirects users with the wrong role to their own area', () => { mockAuth = { user: { roles: ['STUDENT'] }, loading: false }; wrap(tree, '/admin/secret'); expect(screen.getByText('student home')).toBeInTheDocument(); });
  it('allows the right role', () => { mockAuth = { user: { roles: ['ADMIN'] }, loading: false }; wrap(tree, '/admin/secret'); expect(screen.getByText('admin secret')).toBeInTheDocument(); });
});

describe('helpers', () => {
  it('maps roles to home routes', () => { expect(roleHome({ roles: ['ADMIN'] })).toBe('/admin'); expect(roleHome({ roles: ['INSTRUCTOR'] })).toBe('/instructor'); expect(roleHome({ roles: ['STUDENT'] })).toBe('/student'); });
  it('scores password strength', () => { expect(passwordStrength('abc')).toBe(0); expect(passwordStrength('Abcdefg1')).toBe(3); });
});
