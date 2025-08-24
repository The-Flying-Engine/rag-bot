import { useEffect, useRef, useState } from "react";
import { ingest, ask, stream, dbInfo, dbTables, dbClear } from "./api";
import "./App.css";

export default function App() {

  const [docId, setDocId] = useState("DOC 101");
  const [rawText, setRawText] = useState("");
  const [ingestMsg, setIngestMsg] = useState("");


  const [question, setQuestion] = useState("What is a VPN tunnel?");
  const [topK, setTopK] = useState(5);
  const [answer, setAnswer] = useState("");
  const [messages, setMessages] = useState([
    { role: "assistant", text: "Hi! Paste some content on the left, then ask me anything." }
  ]);
  const abortRef = useRef(null);


  const [info, setInfo] = useState(null);
  const [tables, setTables] = useState([]);

  const onIngest = async () => {
    if (!docId.trim() || !rawText.trim()) return;
    const res = await ingest(docId.trim(), rawText);
    setIngestMsg(`Ingested ${res.chunks} chunks for docId=${res.docId}`);
    setRawText("");
  };

  const onAsk = async () => {
    const q = question.trim();
    if (!q) return;
    setMessages((m) => [...m, { role: "user", text: q }]);
    setAnswer("");
    setQuestion("");

    // non-stream call
    const { answer: a } = await ask(q, Number(topK) || 5);
    setMessages((m) => [...m, { role: "assistant", text: a }]);
  };

  // Optional streaming (kept for later)
  // const onStream = () => {
  //   const q = question.trim();
  //   if (!q) return;
  //   setMessages(m => [...m, { role: "user", text: q }]);
  //   setQuestion("");
  //   setMessages(m => [...m, { role: "assistant", text: "" }]); // placeholder
  //   const idx = messages.length; // position of assistant message
  //   abortRef.current = stream(
  //     q,
  //     Number(topK) || 5,
  //     (chunk) => setMessages(m => {
  //       const copy = [...m];
  //       copy[idx] = { role: "assistant", text: (copy[idx]?.text || "") + chunk };
  //       return copy;
  //     }),
  //     () => { abortRef.current = null; },
  //     (err) => setMessages(m => [...m, { role: "assistant", text: `Error: ${err.message}` }])
  //   );
  // };

  const refreshInfo = async () => setInfo(await dbInfo());
  const refreshTables = async () => setTables(await dbTables());

  const clearAll = async () => {
    if (!confirm("Truncate ALL H2 tables?")) return;
    await dbClear();
    await refreshTables();
  };

  useEffect(() => {
    refreshInfo();
    refreshTables();
  }, []);

  return (
    <div className="layout">
    
      <aside className="sidebar">
        <div className="brand">Doc-Chat</div>

        <div className="section">
          <h3>Insert Text</h3>
          <label>Document ID</label>
          <input
            value={docId}
            onChange={(e) => setDocId(e.target.value)}
            placeholder="networks101"
          />

          <label>Raw Text</label>
          <textarea
            rows={10}
            value={rawText}
            onChange={(e) => setRawText(e.target.value)}
            placeholder="Paste your notes, docs, or content here..."
          />
          <button className="primary" onClick={onIngest}>Ingest</button>

          {ingestMsg && <div className="notice ok">{ingestMsg}</div>}
        </div>

        <div className="section">
          <h3>Database</h3>
          <p className="h2link">
            <a href="http://localhost:8080/h2" target="_blank" className="h2link" rel="noreferrer">
              Open H2 Console
            </a>{" "}
            (JDBC: <code>jdbc:h2:file:./ragdb</code>)
          </p>

          <div className="db-tools">
            <button onClick={refreshInfo}>Refresh Info</button>
            <button onClick={refreshTables}>Refresh Tables</button>
            <button className="danger" onClick={clearAll}>Clear All</button>
          </div>

          {info && (
            <div className="db-meta">
              <div><b>URL:</b> {info.url}</div>
              <div><b>User:</b> {info.user}</div>
              <div><b>Product:</b> {info.product}</div>
            </div>
          )}

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Table</th>
                  <th style={{ width: 90, textAlign: "right" }}>Rows</th>
                </tr>
              </thead>
              <tbody>
                {tables.map((t) => (
                  <tr key={t.name}>
                    <td>{t.name}</td>
                    <td style={{ textAlign: "right" }}>{t.rows}</td>
                  </tr>
                ))}
                {tables.length === 0 && (
                  <tr>
                    <td colSpan={2} style={{ textAlign: "center", padding: 12 }}>
                      No tables found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </aside>

      <main className="chat">
        <header className="chat-header">
          <div>
            <h2 className="sub">React + Spring Boot + H2 + Ollama</h2>
          </div>
          <div className="topk">
            <label>Top-K</label>
            <input
              type="number"
              min="1"
              max="10"
              value={topK}
              onChange={(e) => setTopK(e.target.value)}
            />
          </div>
        </header>


        <div className="messages">
          {messages.map((m, i) => (
            <div key={i} className={`bubble ${m.role}`}>
              {m.text}
            </div>
          ))}
        </div>

    
        <div className="composer">
          <input
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask me anything about your ingested docs…"
            onKeyDown={(e) => e.key === "Enter" && onAsk()}
          />
          <button className="primary" onClick={onAsk}>Send</button>
        </div>
      </main>
    </div>
  );
}
