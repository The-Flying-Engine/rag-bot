package dev.gov.rag_boot.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class Chunk {
  private String docId;
  private int chunkIndex;
  private String text;
  private float[] embedding; 
}
