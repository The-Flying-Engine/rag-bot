import { useEffect, useRef, useState } from 'react'

import { ingest, ask, stream, dbInfo, dbTables, dbClear } from './api'

export default function App() {
  const [docId, setDocId] = useState('DOC 101')
  const [rawText, setRawText] = useState('')
  const [ingestMsg, setIngestMsg] = useState('')

  const [question, setQuestion] = useState('What is a VPN tunnel?')
  const [topK, setTopK] = useState(5)
  const [answer, setAnswer] = useState('')
  const abortRef = useRef(null)

  // DB state
  const [info, setInfo] = useState(null)
  const [tables, setTables] = useState([])

  

  const onIngest = async () => {
    if (!docId.trim() || !rawText.trim()) return
    const res = await ingest(docId.trim(), rawText)
    setIngestMsg(`Ingested ${res.chunks} chunks for docId=${res.docId}`)
  }

  const onAsk = async () => {
    const { answer } = await ask(question, Number(topK) || 5)
    setAnswer(answer)
  }

  // const onStream = () => {
  //   setAnswer('')
  //   abortRef.current = stream(
  //     question,
  //     Number(topK) || 5,
  //     (chunk) => setAnswer(prev => prev + chunk),
  //     () => { abortRef.current = null },
  //     (err) => { setAnswer(`Error: ${err.message}`) }
  //   )
  // }
  const refreshInfo = async () => setInfo(await dbInfo())
  const refreshTables = async () => setTables(await dbTables())

  const clearAll = async () => {
    if (!confirm('Truncate ALL H2 tables?')) return
    await dbClear()
    await refreshTables()
  }

  useEffect(() => {
    refreshInfo()
    refreshTables()
  }, [])

 return (
    <div className="page">
      <h2>Doc-Chat (RAG Implementation Using React + Spring Boot + H2)</h2>

      {/* 1) Ingest */}
      <section className="card">
        <h3>Insert Text Information</h3>
        <label>Document ID</label>
        <input value={docId} onChange={e=>setDocId(e.target.value)} placeholder="networks101" />
        <label>Raw Text</label>
        <textarea rows={8} value={rawText} onChange={e=>setRawText(e.target.value)}
                  placeholder="Paste your notes, docs, or content here..." />
        <div className="btn-row">
          <button onClick={onIngest}>Ingest</button>
        </div>
        <div className="ok" style={{marginTop: 8}}>{ingestMsg}</div>
      </section>

      {/* 2) Ask */}
      <section className="card">
        <h3>Ask Questions</h3>
        <label>Question</label>
        <input value={question} onChange={e=>setQuestion(e.target.value)} placeholder="What is a VPN tunnel?" />
        <label>TopK</label>
        <input type="number" min="1" max="10" value={topK} onChange={e=>setTopK(e.target.value)} />
        <div className="btn-row">
          <button onClick={onAsk}>Ask (non-stream)</button>
          {/* <button onClick={onStream}>Ask (stream)</button> */}
          {/* <button className="secondary" onClick={() => abortRef.current?.()}>Stop</button> */}
        </div>
        <h4 style={{marginTop: 12}}>Answer</h4>
        <div className="answer">{answer}</div>


      </section>

      {/* 3) DB */}
      <section className="card">
        <h3>Database Info</h3>
                {/* Fixed anchor target */}
        <p style={{marginTop: 12}}>
          <a href="http://localhost:8080/h2" target="_blank" rel="noopener noreferrer">Open H2 Console</a>
          {' '} (JDBC URL: <code>jdbc:h2:file:./ragdb</code>)
        </p>
        <div className="db-toolbar">
          <button onClick={refreshInfo}>Refresh Info</button>
          <button onClick={refreshTables}>Refresh Tables</button>
          <button className="secondary" onClick={clearAll}>Clear All Tables (H2)</button>
          {info && (
            <div className="db-meta">
              <strong>URL:</strong> {info.url} &nbsp;|&nbsp; <strong>User:</strong> {info.user} &nbsp;|&nbsp; <strong>Product:</strong> {info.product}
            </div>
          )}
        </div>

        <table className="db-grid">
          <thead>
            <tr><th>Table</th><th style={{width: 120}}>Rows</th></tr>
          </thead>
          <tbody>
            {tables.map(t => (
              <tr key={t.name}>
                <td>{t.name}</td>
                <td>{t.rows}</td>
              </tr>
            ))}
            {tables.length === 0 && (
              <tr><td colSpan={2} style={{textAlign:'center', padding:16}}>No tables found.</td></tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  )
}
