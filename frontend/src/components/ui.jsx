import { useState } from 'react';
import { AlertCircle, BookOpen, ChevronLeft, ChevronRight, Eye, EyeOff, Inbox, Loader2, X } from 'lucide-react';

export const Spinner = ({ full }) => (
  <div className={full ? 'center-screen' : 'center-block'} role="status" aria-label="Loading"><Loader2 className="spin" size={28} /></div>
);
export const ErrorBox = ({ message, onRetry }) => message ? (
  <div className="alert alert-error" role="alert"><AlertCircle size={18} /><span>{message}</span>{onRetry && <button className="btn btn-sm btn-outline" onClick={onRetry}>Retry</button>}</div>
) : null;
export const Notice = ({ type = 'success', children }) => children ? <div className={`alert alert-${type}`} role="status">{children}</div> : null;
export const Empty = ({ title, text, action }) => (
  <div className="empty"><Inbox size={32} /><h3>{title}</h3>{text && <p>{text}</p>}{action}</div>
);
export const ProgressBar = ({ value }) => (
  <div className="progress" role="progressbar" aria-valuenow={value} aria-valuemin={0} aria-valuemax={100}><span style={{ width: `${value}%` }} /></div>
);
export function Thumb({ src, title }) {
  const [bad, setBad] = useState(false);
  if (!src || bad) return <div className="thumb thumb-fallback" aria-label={title}><BookOpen size={34} /></div>;
  return <img className="thumb" src={src} alt={title} loading="lazy" onError={() => setBad(true)} />;
}
export function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Pagination">
      <button className="btn btn-sm btn-outline" disabled={page === 0} onClick={() => onChange(page - 1)}><ChevronLeft size={16} />Previous</button>
      <span>Page {page + 1} of {totalPages}</span>
      <button className="btn btn-sm btn-outline" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>Next<ChevronRight size={16} /></button>
    </nav>
  );
}
export function Modal({ title, onClose, children, wide }) {
  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`modal ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head"><h2>{title}</h2><button className="icon-btn" onClick={onClose} aria-label="Close"><X size={20} /></button></div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}
export function Confirm({ title, message, confirmText = 'Delete', onConfirm, onCancel, busy }) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p>{message}</p>
      <div className="row end gap"><button className="btn btn-outline" onClick={onCancel}>Cancel</button><button className="btn btn-danger" disabled={busy} onClick={onConfirm}>{busy ? 'Working…' : confirmText}</button></div>
    </Modal>
  );
}
export function Field({ label, error, children, hint }) {
  return <label className="field"><span className="label">{label}</span>{children}{hint && <small className="hint">{hint}</small>}{error && <small className="field-error">{error}</small>}</label>;
}
export function PasswordInput({ value, onChange, id, autoComplete, placeholder }) {
  const [show, setShow] = useState(false);
  return (
    <div className="pw-wrap">
      <input id={id} className="input" type={show ? 'text' : 'password'} value={value} onChange={onChange} autoComplete={autoComplete} placeholder={placeholder} />
      <button type="button" className="icon-btn pw-toggle" onClick={() => setShow(!show)} aria-label={show ? 'Hide password' : 'Show password'}>{show ? <EyeOff size={18} /> : <Eye size={18} />}</button>
    </div>
  );
}
export const StatCard = ({ icon: Icon, label, value }) => (
  <div className="card stat"><div className="stat-icon"><Icon size={20} /></div><div><div className="stat-value">{value ?? '—'}</div><div className="stat-label">{label}</div></div></div>
);
export const Badge = ({ children, tone }) => <span className={`badge ${tone ? 'badge-' + tone : ''}`}>{children}</span>;
