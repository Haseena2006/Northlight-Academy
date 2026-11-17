const INLINE = /(\*\*[^*]+\*\*|`[^`]+`)/g;
const TOKEN = /(\/\/[^\n]*|#[^\n]*|--[^\n]*|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\b(?:abstract|assert|boolean|break|case|catch|class|const|continue|def|do|else|enum|extends|false|final|finally|float|for|from|function|if|implements|import|in|instanceof|int|interface|let|new|null|package|private|protected|public|return|static|super|switch|this|throw|throws|true|try|var|void|while|WITH|SELECT|FROM|WHERE|GROUP|BY|ORDER|COUNT|AS|VALUES|JOIN|ON|AND|OR|NOT|CREATE|INSERT|UPDATE|DELETE|TABLE|PRIMARY|KEY|INT|TEXT|String|List|System|println|print)\b|\b\d+(?:\.\d+)?\b)/g;

function inline(text) {
  return text.split(INLINE).map((part, i) => {
    if (part.startsWith('**') && part.endsWith('**')) return <strong key={i}>{part.slice(2, -2)}</strong>;
    if (part.startsWith('`') && part.endsWith('`')) return <code key={i}>{part.slice(1, -1)}</code>;
    return part;
  });
}

function CodeBlock({ language, code }) {
  const className = language ? `language-${language.toLowerCase()}` : '';
  const pieces = code.split(TOKEN);
  return <pre className="reading-code"><code className={className}>{pieces.map((piece, i) => {
    if (/^(\/\/|#|--)/.test(piece)) return <span className="syntax-comment" key={i}>{piece}</span>;
    if (/^["']/.test(piece)) return <span className="syntax-string" key={i}>{piece}</span>;
    if (/^\d/.test(piece)) return <span className="syntax-number" key={i}>{piece}</span>;
    if (/^(abstract|assert|boolean|break|case|catch|class|const|continue|def|do|else|enum|extends|false|final|finally|float|for|from|function|if|implements|import|in|instanceof|int|interface|let|new|null|package|private|protected|public|return|static|super|switch|this|throw|throws|true|try|var|void|while|WITH|SELECT|FROM|WHERE|GROUP|BY|ORDER|COUNT|AS|VALUES|JOIN|ON|AND|OR|NOT|CREATE|INSERT|UPDATE|DELETE|TABLE|PRIMARY|KEY|INT|TEXT)$/.test(piece)) return <span className="syntax-keyword" key={i}>{piece}</span>;
    return piece;
  })}</code></pre>;
}

/** Render the safe Markdown subset used by lesson reading notes. Raw HTML is treated as text. */
export default function LessonReading({ content }) {
  const lines = (content || '').replace(/\r/g, '').split('\n');
  const blocks = [];
  let paragraph = [];
  let list = [];
  let code = null;

  const flushParagraph = () => {
    if (paragraph.length) blocks.push({ type: 'p', text: paragraph.join(' ') });
    paragraph = [];
  };
  const flushList = () => {
    if (list.length) blocks.push({ type: 'list', items: list });
    list = [];
  };

  for (const line of lines) {
    const fence = line.match(/^```\s*([\w+-]*)\s*$/);
    if (fence) {
      flushParagraph(); flushList();
      if (code) { blocks.push({ type: 'code', language: code.language, text: code.lines.join('\n') }); code = null; }
      else code = { language: fence[1], lines: [] };
      continue;
    }
    if (code) { code.lines.push(line); continue; }
    if (!line.trim()) { flushParagraph(); flushList(); continue; }
    const heading = line.match(/^(#{1,4})\s+(.+)$/);
    if (heading) { flushParagraph(); flushList(); blocks.push({ type: 'heading', level: heading[1].length, text: heading[2] }); continue; }
    const item = line.match(/^\s*(?:[-*]|\d+\.)\s+(.+)$/);
    if (item) { flushParagraph(); list.push(item[1]); continue; }
    flushList(); paragraph.push(line.trim());
  }
  if (code) blocks.push({ type: 'code', language: code.language, text: code.lines.join('\n') });
  flushParagraph(); flushList();

  return <div className="lesson-markdown">{blocks.map((block, i) => {
    if (block.type === 'p') return <p key={i}>{inline(block.text)}</p>;
    if (block.type === 'list') return <ul key={i}>{block.items.map((item, j) => <li key={j}>{inline(item)}</li>)}</ul>;
    if (block.type === 'code') return <CodeBlock key={i} language={block.language} code={block.text} />;
    const Tag = `h${Math.min(block.level + 1, 5)}`;
    return <Tag key={i}>{inline(block.text)}</Tag>;
  })}</div>;
}
