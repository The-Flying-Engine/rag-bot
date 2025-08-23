package dev.gov.rag_boot.web;

public class ChatDtos {
  public record IngestReq(String docId, String text) {}
  public record IngestRes(String docId, int chunks) {}

  public record AskReq(String question, Integer topK) {}
  public record AskRes(String answer) {}

  // for optional streaming
  public record Chunk(String text, boolean done) {}
}
