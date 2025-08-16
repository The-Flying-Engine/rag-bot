package dev.gov.rag_boot.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gov.rag_boot.model.Chunk;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository @RequiredArgsConstructor
public class ChunkRepo {
  private final JdbcTemplate jdbc;
  private final ObjectMapper om = new ObjectMapper();

public void upsert(String docId, int idx, String text, float[] emb) throws Exception {
  String embJson = om.writeValueAsString(emb);
  jdbc.update("""
      MERGE INTO chunks (doc_id, chunk_index, text, embedding)
      KEY (doc_id, chunk_index)
      VALUES (?, ?, ?, ?)
    """, docId, idx, text, embJson);
}


  public List<Chunk> findAll() {
    return jdbc.query("SELECT doc_id,chunk_index,text,embedding FROM chunks",
      (ResultSet rs, int row) -> {
        try {
          float[] emb = om.readValue(rs.getString("embedding"), float[].class);
          return new Chunk(
              rs.getString("doc_id"),
              rs.getInt("chunk_index"),
              rs.getString("text"),
              emb
          );
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
  }
}
