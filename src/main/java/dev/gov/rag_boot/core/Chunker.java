package dev.gov.rag_boot.core;
import java.util.ArrayList;
import java.util.List;

public class Chunker {
  public List<String> chunk(String text, int size, int overlap) {
    String[] tokens = text.split("\\s+");
    List<String> chunks = new ArrayList<>();
    int i = 0;
    while (i < tokens.length) {
      int end = Math.min(tokens.length, i + size);
      StringBuilder sb = new StringBuilder();
      for (int j = i; j < end; j++) sb.append(tokens[j]).append(' ');
      chunks.add(sb.toString().trim());
      if (end == tokens.length) break;
      i = Math.max(0, end - overlap);
    }
    return chunks;
  }

  public static float cosine(float[] a, float[] b) {
    double dot = 0, na = 0, nb = 0;
    for (int i = 0; i < a.length; i++) {
      dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i];
    }
    return (float)(dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-9));
  }
}

