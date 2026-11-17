import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Download } from 'lucide-react';
import api, { downloadFile, errMsg } from '../../services/api.js';
import useFetch from '../../hooks/useFetch.js';
import { Empty, ErrorBox, Spinner } from '../../components/ui.jsx';
import { fmtDate } from '../../utils/format.js';

export default function Certificates() {
  const { data, loading, error, reload } = useFetch(() => api.get('/student/certificates').then((r) => r.data));
  const [err, setErr] = useState('');
  const dl = async (c) => { setErr(''); try { await downloadFile(`/student/certificates/${c.certificateCode}/download`, `northlight-certificate-${c.certificateCode}.pdf`); } catch (e) { setErr(errMsg(e)); } };
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  return (
    <>
      <div className="page-head"><h1>Certificates</h1></div>
      <ErrorBox message={err} />
      {data.length === 0 ? <Empty title="No certificates yet" text="Complete every lesson of a course to earn a certificate." action={<Link className="btn btn-outline btn-sm" to="/student/courses">Go to my courses</Link>} /> :
        <div className="grid grid-2">{data.map((c) => (
          <div key={c.id} className="stack">
            <div className="cert"><span className="eyebrow">Northlight Academy</span><h2>Certificate of Completion</h2><p className="muted" style={{ margin: 0 }}>awarded to</p><h3>{c.studentName}</h3><p className="muted" style={{ margin: 0 }}>for completing</p><h3 style={{ color: '#2857A4' }}>{c.courseTitle}</h3><p className="muted small">{fmtDate(c.issuedAt)} · ID {c.certificateCode}</p></div>
            <div className="row gap"><button className="btn btn-primary btn-sm" onClick={() => dl(c)}><Download size={14} />Download PDF</button><Link className="btn btn-outline btn-sm" to={`/verify/${c.certificateCode}`}>Public verification page</Link></div>
          </div>))}</div>}
    </>
  );
}
