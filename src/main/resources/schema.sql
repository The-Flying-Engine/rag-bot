DROP TABLE IF EXISTS chunks;

CREATE TABLE chunks (
  doc_id      VARCHAR(255) NOT NULL,
  chunk_index INT          NOT NULL,
  text        CLOB         NOT NULL,
  embedding   CLOB         NOT NULL,
  PRIMARY KEY (doc_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_chunks_doc ON chunks(doc_id);
