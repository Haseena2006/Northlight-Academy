import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { BadgeCheck, XCircle } from 'lucide-react';
import api from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Spinner } from '../../components/ui.jsx';
import { fmtDate } from '../../utils/format.js';

export default function VerifyCertificate() {
  const { code } = useParams();
  const nav = useNavigate();
  const [input, setInput] = useState(code || '');
  const res = useFetch(() => (code ? api.get(`/certificates/${encodeURIComponent(code)}/verify`).then((r) => r.data) : Promise.resolve(null)), [code]);
  return (
    <div className="container section-sm" style={{ maxWidth: 640 }}>
      <h1 style={{ fontSize: '2rem' }}>Verify a certificate</h1>
      <p className="muted">Enter the certificate ID printed on the certificate.</p>
      <form className="row gap" onSubmit={(e) => { e.preventDefault(); if (input.trim()) nav(`/verify/${encodeURIComponent(input.trim())}`); }}>
        <input className="input" placeholder="NLA-XXXX-XXXX-XXXX" value={input} onChange={(e) => setInput(e.target.value)} aria-label="Certificate ID" />
        <button className="btn btn-primary" type="submit">Verify</button>
      </form>
      <div style={{ marginTop: 24 }}>
        {code && res.loading && <Spinner />}
        {res.data?.valid && (
          <div className="card card-pad stack"><div className="row gap"><BadgeCheck color="#16845B" /><strong style={{ color: '#16845B' }}>Valid certificate</strong></div>
            <div><span className="muted small">Awarded to</span><div><strong>{res.data.studentName}</strong></div></div>
            <div><span className="muted small">Course</span><div><strong>{res.data.courseTitle}</strong></div></div>
            <div><span className="muted small">Issued</span><div>{fmtDate(res.data.issuedAt)}</div></div>
            <div><span className="muted small">Certificate ID</span><div>{res.data.certificateCode}</div></div></div>
        )}
        {res.data && !res.data.valid && <div className="alert alert-error"><XCircle size={18} />No certificate was found with that ID.</div>}
      </div>
    </div>
  );
}
