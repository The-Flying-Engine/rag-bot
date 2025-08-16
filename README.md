# RAG Chat (Spring Boot + H2)

A beginner-friendly **RAG** (Retrieve-Augment-Generate) demo using **Spring Boot**.  
You can paste text, store it as chunks with **vector embeddings**, and ask questions answered from your text only.

> Tech: Spring Boot (Web, JDBC), H2 DB, Jackson, HTTP client, optional Thymeleaf UI.  
> DB: H2 file DB by default. Upgrade path: PostgreSQL + pgvector.

---

## 1) Features

- Ingest plain text → chunk → embed → store.
- Ask a question → retrieve top-K chunks by **cosine similarity** → build prompt → get answer.
- Works with:
  - **Ollama** (local, no API key).
  - **OpenAI-compatible** APIs (set your key).
- Very simple web UI (server-rendered) **or** JSON API (if you prefer React/JS).

---

## 2) Project Structure

```
src/
 ├─ main/
 │   ├─ java/dev/gov/rag/
 │   │   ├─ RagBootApplication.java        # Spring Boot starter
 │   │   ├─ core/
 │   │   │   ├─ Chunker.java               # chunk + cosine
 │   │   │   ├─ Embedder.java              # embeddings API caller
 │   │   │   ├─ ChatClient.java            # chat completions API caller
 │   │   │   └─ RagService.java            # ingest + ask orchestration
 │   │   ├─ model/Chunk.java               # row model
 │   │   ├─ repo/ChunkRepo.java            # JDBC repo (H2)
 │   │   └─ web/RagController.java         # UI controller (Thymeleaf) or ApiController (JSON)
 │   └─ resources/
 │       ├─ application.properties         # config (DB + LLM)
 │       ├─ schema.sql                     # H2 schema (creates table)
 │       └─ templates/index.html           # simple UI (if using Thymeleaf)
 └─ test/...
```

---

## 3) Requirements

- Java **17+**
- Maven (wrapper provided: `./mvnw`)
- One LLM path:
  - **Ollama** (local): `ollama serve`, models pulled.
  - **OpenAI-compatible**: API key.

---

## 4) Configure LLM

Edit `src/main/resources/application.properties` and pick ONE option.

### Option A — Ollama (local)
```properties
# Web server
server.port=8080

# H2 file DB
spring.datasource.url=jdbc:h2:file:./ragdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.h2.console.enabled=true
spring.h2.console.path=/h2

# LLM / embedding (Ollama OpenAI-compatible API)
llm.base-url=http://localhost:11434/v1
llm.api-key=
llm.chat-model=llama3
llm.embed-model=nomic-embed-text
```

Start Ollama once:
```bash
ollama serve
ollama pull llama3
ollama pull nomic-embed-text
```

### Option B — OpenAI-compatible
```properties
llm.base-url=https://api.openai.com/v1
llm.api-key=YOUR_KEY
llm.chat-model=gpt-4o-mini
llm.embed-model=text-embedding-3-small
```

---

## 5) DB Schema (H2)

`src/main/resources/schema.sql`
```sql
DROP TABLE IF EXISTS chunks;

-- Composite primary key (docId + chunk_index)
CREATE TABLE chunks (
  doc_id      VARCHAR(255) NOT NULL,
  chunk_index INT          NOT NULL,
  text        CLOB         NOT NULL,
  embedding   CLOB         NOT NULL,
  PRIMARY KEY (doc_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_chunks_doc ON chunks(doc_id);
```

> We store the embedding as a JSON array string (simple for H2).  
> Retrieval does cosine similarity in Java.

---

## 6) Build & Run

```bash
# from project root
./mvnw clean spring-boot:run
```

Open: **http://localhost:8080**

- **Ingest Text**: enter `docId` + paste raw text → **Ingest**  
- **Ask**: type your question → **Ask**

H2 console: **http://localhost:8080/h2**  
JDBC URL: `jdbc:h2:file:./ragdb`

---

## 7) API (JSON) — if you prefer fetch/React

If you use a plain HTML/React front-end, expose JSON endpoints (sample):

```
POST /api/ingest
Body: { "docId": "networks101", "text": "..." }
Resp: { "message": "Ingested N chunks for docId=..." }

POST /api/ask
Body: { "question": "What is ...?", "topK": 5 }
Resp: { "answer": "..." }
```

Optional streaming (SSE) endpoint (advanced):
```
GET /api/ask/stream?question=...&topK=5
Events:
  event: delta  data: <partial text>
  event: done   data:
```

> If you use SSE from a different origin, enable CORS for `/api/**`.

---

## 8) How the flow works (high level)

1. **Ingest**
   - UI sends `docId` + raw text → `/ingest`.
   - Service splits text into chunks, gets an **embedding** for each chunk, stores rows in `chunks` (H2).

2. **Ask**
   - UI sends `question` → service embeds question.
   - Compute **cosine similarity** between question vector and each chunk vector.
   - Take **Top-K** chunks, build a prompt, call the **chat** API.
   - Return the answer to UI.

(That’s the RAG pipeline without going into the logic details.)

---

## 9) Troubleshooting

- **Cannot find Thymeleaf view `index`:**  
  Ensure file path is `src/main/resources/templates/index.html`  
  Controller must be `@Controller` and `return "index"`.

- **NPE in Embedder (json `data` missing):**  
  Your embeddings endpoint returned an error or different shape.  
  Check Ollama is running and model names are correct. Test:
  ```bash
  curl -s http://localhost:11434/v1/embeddings     -H "Content-Type: application/json"     -d '{"model":"nomic-embed-text","input":"hello"}'
  ```
  Should contain either `{"data":[{"embedding":[...]}]}` or `{"embedding":[...]}`.

- **H2 table not found / old schema still used:**  
  Make sure:
  ```properties
  spring.sql.init.mode=always
  spring.sql.init.schema-locations=classpath:schema.sql
  ```
  If needed, delete old files once:
  ```bash
  rm -f ragdb.mv.db ragdb.trace.db
  ```

- **HttpClient class error (TlsSocketStrategy):**  
  Remove explicit versions of `httpclient5/httpcore5` from `pom.xml` so Spring Boot manages them, then:
  ```bash
  ./mvnw clean spring-boot:run
  ```
  Or use JDK HTTP client and set `spring.http.client.factory=jdk`.

---

## 10) Upgrade Path (when ready)

- **PostgreSQL + pgvector**: store real vectors, query with `ORDER BY embedding <=> :qvec LIMIT k`.  
- **Hybrid retrieval**: add keyword search (trigram/BM25) + merge with vector hits.  
- **React UI**: move to a JSON API only, add **streaming answers** via SSE.  
- **File ingestion**: add PDF/Docx parsing (Apache Tika).

---

## 11) License

MIT (or add your own).

---

## 12) Quick Commands (cheat sheet)

```bash
# Run app
./mvnw spring-boot:run

# Rebuild from scratch
./mvnw clean package

# H2 console
open http://localhost:8080/h2

# Test Ollama embeddings
ollama serve
ollama pull llama3
ollama pull nomic-embed-text
curl -s http://localhost:11434/v1/embeddings   -H "Content-Type: application/json"   -d '{"model":"nomic-embed-text","input":"hello"}'
```
