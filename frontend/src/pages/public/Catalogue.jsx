import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { BookOpen, Search, SlidersHorizontal, X } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import CourseCard from '../../components/CourseCard.jsx';
import { Empty, ErrorBox, Pagination, Spinner } from '../../components/ui.jsx';

export default function Catalogue() {
  const [sp, setSp] = useSearchParams();
  const q = sp.get('q') || '';
  const categoryId = sp.get('categoryId') || '';
  const difficulty = sp.get('difficulty') || '';
  const duration = sp.get('duration') || '';
  const sort = sp.get('sort') || 'newest';
  const page = Number(sp.get('page') || 0);
  const [text, setText] = useState(q);
  const cats = useFetch(() => api.get('/categories').then((r) => r.data));
  const stats = useFetch(() => api.get('/public/stats').then((r) => r.data));
  const durationRanges = { short: [undefined, 119], medium: [120, 299], long: [300, undefined] };
  const [minDuration, maxDuration] = durationRanges[duration] || [];
  const res = useFetch(() => api.get('/courses', { params: { q: q || undefined, categoryId: categoryId || undefined, difficulty: difficulty || undefined, minDuration, maxDuration, sort, page, size: 9 } }).then((r) => r.data), [q, categoryId, difficulty, duration, sort, page]);
  const set = (k, v) => { const n = new URLSearchParams(sp); v ? n.set(k, v) : n.delete(k); if (k !== 'page') n.delete('page'); setSp(n); };
  useEffect(() => { const t = setTimeout(() => { if (text !== q) set('q', text.trim()); }, 220); return () => clearTimeout(t); /* eslint-disable-next-line */ }, [text]);
  useEffect(() => setText(q), [q]);
  const clear = () => { setSp(new URLSearchParams()); setText(''); };
  const d = res.data;

  return (
    <div className="catalogue-page">
      <header className="catalogue-heading"><div className="container"><span className="eyebrow">Northlight learning</span><h1>Explore the course catalogue</h1><p>Build practical skills with structured, self-paced courses.</p><div className="catalogue-total"><BookOpen size={17} />{stats.data ? `${stats.data.courses} courses to explore` : 'Browse our course collection'}</div></div></header>
      <div className="container section-sm">
        <div className="catalogue-search">
          <div className="catalogue-searchbox"><Search size={18} aria-hidden="true" /><input placeholder="Search titles, skills, topics, instructors…" value={text} onChange={(e) => setText(e.target.value)} aria-label="Search courses" />{text && <button className="catalogue-search-clear" onClick={clear} aria-label="Clear search and filters"><X size={18} /></button>}</div>
          <label className="catalogue-sort"><span>Sort by</span><select className="input" value={sort} onChange={(e) => set('sort', e.target.value)} aria-label="Sort courses"><option value="newest">Newest</option><option value="title">Name A–Z</option><option value="title-desc">Name Z–A</option></select></label>
        </div>
        <nav className="catalogue-category-nav" aria-label="Course categories">
          <button className={`category-chip ${!categoryId ? 'selected' : ''}`} onClick={() => set('categoryId', '')}>All courses</button>
          {cats.data?.map((c) => <button key={c.id} className={`category-chip ${categoryId === String(c.id) ? 'selected' : ''}`} onClick={() => set('categoryId', String(c.id))}>{c.name}</button>)}
        </nav>
        <div className="catalogue-layout">
          <aside className="catalogue-filters" aria-label="Course filters">
            <h2><SlidersHorizontal size={17} />Refine results</h2>
            <label className="catalogue-filter-label" htmlFor="course-category">Category</label>
            <select id="course-category" className="input" value={categoryId} onChange={(e) => set('categoryId', e.target.value)}><option value="">All categories</option>{cats.data?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select>
            <label className="catalogue-filter-label" htmlFor="course-level">Difficulty</label>
            <select id="course-level" className="input" value={difficulty} onChange={(e) => set('difficulty', e.target.value)}><option value="">All levels</option><option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="ADVANCED">Advanced</option></select>
            <label className="catalogue-filter-label" htmlFor="course-duration">Course duration</label>
            <select id="course-duration" className="input" value={duration} onChange={(e) => set('duration', e.target.value)}><option value="">Any duration</option><option value="short">Under 2 hours</option><option value="medium">2 to 5 hours</option><option value="long">5 hours and above</option></select>
            {(categoryId || difficulty || duration || q) && <button className="btn btn-outline btn-sm catalogue-clear" onClick={clear}>Clear all filters</button>}
          </aside>
          <section className="catalogue-results" aria-label="Course results">
            {res.loading ? <Spinner /> : res.error ? <ErrorBox message={res.error} onRetry={res.reload} /> :
              d?.content?.length ? <><p className="catalogue-count"><strong>{d.totalElements}</strong> matching course{d.totalElements === 1 ? '' : 's'}</p><div className="grid catalogue-grid">{d.content.map((c) => <CourseCard key={c.id} course={c} />)}</div><Pagination page={d.page} totalPages={d.totalPages} onChange={(p) => set('page', p || '')} /></> :
                <Empty title="No courses match your filters" text="Try another topic or remove a filter to see more results." action={<button className="btn btn-outline btn-sm" onClick={clear}>Clear search and filters</button>} />}
          </section>
        </div>
      </div>
    </div>
  );
}
