export const fmtDate = (d) => (d ? new Date(d).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }) : '—');
export const fmtDuration = (m) => { if (!m) return '0 min'; const h = Math.floor(m / 60), r = m % 60; return h ? (r ? `${h}h ${r}m` : `${h}h`) : `${r} min`; };
export const titleCase = (s) => (s ? s.charAt(0) + s.slice(1).toLowerCase() : '');
export function roleHome(user) {
  const r = user?.roles || [];
  if (r.includes('ADMIN')) return '/admin';
  if (r.includes('INSTRUCTOR')) return '/instructor';
  return '/student';
}
export function passwordStrength(p) {
  let s = 0;
  if (p.length >= 8) s++; if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++; if (/\d/.test(p)) s++; if (/[^A-Za-z0-9]/.test(p) || p.length >= 12) s++;
  return s; // 0-4
}
export const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/;
export function videoKind(url) {
  if (!url) return { kind: 'none' };
  try {
    const u = new URL(url);
    if (u.hostname.includes('youtube.com') && u.searchParams.get('v')) return { kind: 'iframe', src: `https://www.youtube-nocookie.com/embed/${u.searchParams.get('v')}` };
    if (u.hostname === 'youtu.be') return { kind: 'iframe', src: `https://www.youtube-nocookie.com/embed/${u.pathname.slice(1)}` };
    if (u.hostname.includes('youtube.com') && u.pathname.startsWith('/embed/')) return { kind: 'iframe', src: url };
    if (u.hostname.includes('vimeo.com') && /^\/\d+$/.test(u.pathname)) return { kind: 'iframe', src: `https://player.vimeo.com/video${u.pathname}` };
    if (/\.(mp4|webm|ogg)$/i.test(u.pathname)) return { kind: 'video', src: url };
    return { kind: 'link', src: url };
  } catch { return { kind: 'none' }; }
}
