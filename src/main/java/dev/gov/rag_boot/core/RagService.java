package dev.gov.rag_boot.core;


import dev.gov.rag_boot.model.Chunk;
import dev.gov.rag_boot.repo.ChunkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service @RequiredArgsConstructor
public class RagService {
  private final ChunkRepo repo;
  private final Embedder embedder;
  private final ChatClient chat;
  private final Chunker chunker = new Chunker();

  public int ingest(String docId, String text) throws Exception {
    var pieces = chunker.chunk(text, 220, 40);
    int idx = 0;
    for (String p : pieces) {
      float[] v = embedder.embedOne(p);
      repo.upsert(docId, idx++, p, v);
    }
    return pieces.size();
  }

  public String ask(String question, int k) throws Exception {
    float[] qv = embedder.embedOne(question);
    var all = repo.findAll();

    // rank by cosine
    all.sort((a,b) -> Float.compare(
        Chunker.cosine(b.getEmbedding(), qv),
        Chunker.cosine(a.getEmbedding(), qv)
    ));
    var top = all.subList(0, Math.min(k, all.size()));

    var sbCtx = new StringBuilder();
    for (int i = 0; i < top.size(); i++) {
      sbCtx.append("Source ").append(i+1).append(":\n").append(top.get(i).getText()).append("\n\n");
    }
    String prompt = """
        Use ONLY the context below to answer.
        If the answer is not in the context, say: I don't know.

        ===== CONTEXT =====
        %s
        ===== QUESTION =====
        %s

        Give the answer in 3-5 lines. Then list used sources like [S1, S2].
        """.formatted(sbCtx, question);

    return chat.complete(prompt);
  }
}

