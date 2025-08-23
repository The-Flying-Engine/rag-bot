import axios from 'axios'

export async function ingest(docId, text) {
  const { data } = await axios.post('/api/ingest', { docId, text })
  return data  // { docId, chunks }
}

export async function ask(question, topK = 5) {
  const { data } = await axios.post('/api/ask', { question, topK })
  return data  // { answer }
}

export async function dbInfo() {
  const { data } = await axios.get('/api/db/info')
  return data // { url, user, product, tableCount }
}

export async function dbTables() {
  const { data } = await axios.get('/api/db/tables')
  return data.items // [ { name, rows }, ... ]
}

export async function dbClear() {
  const { data } = await axios.post('/api/db/clear')
  return data // { truncatedTables }
}

// optional streaming
export function stream(question, topK = 5, onChunk, onDone, onError) {
  const ctrl = new AbortController()
  fetch('/api/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, topK }),
    signal: ctrl.signal,
  }).then(async (res) => {
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buf = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buf += decoder.decode(value, { stream: true })
      const frames = buf.split('\n\n')
      buf = frames.pop()
      for (const f of frames) {
        if (f.startsWith('data:')) {
          const payload = f.slice(5).trim()
          try {
            const evt = JSON.parse(payload)
            if (evt.done) onDone?.()
            else onChunk?.(evt.text)
          } catch { /* ignore */ }
        }
      }
    }
  }).catch(err => onError?.(err))
  return () => ctrl.abort()
}
