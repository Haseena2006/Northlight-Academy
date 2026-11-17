import { useState } from 'react';
import { ExternalLink, VideoOff } from 'lucide-react';
import { videoKind } from '../utils/format.js';

export default function VideoPlayer({ url, title }) {
  const v = videoKind(url);
  const [failed, setFailed] = useState(false);
  if (v.kind === 'iframe') return <div className="player"><iframe src={v.src} title={title} allow="accelerometer; encrypted-media; picture-in-picture" allowFullScreen referrerPolicy="strict-origin-when-cross-origin" /></div>;
  if (v.kind === 'video' && !failed) return <div className="player"><video src={v.src} controls onError={() => setFailed(true)} title={title} /></div>;
  return (
    <div className="player"><div className="fallback">
      <VideoOff size={36} />
      <p style={{ margin: '12px 0 8px' }}>{v.kind === 'none' ? 'No video has been added to this lesson yet.' : 'This video cannot be played inside the page.'}</p>
      {(v.kind === 'link' || failed) && <a className="btn btn-light btn-sm" href={v.src} target="_blank" rel="noopener noreferrer"><ExternalLink size={14} />Open video</a>}
    </div></div>
  );
}
