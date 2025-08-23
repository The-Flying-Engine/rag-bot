package dev.gov.rag_boot.web;

import dev.gov.rag_boot.core.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static dev.gov.rag_boot.web.ChatDtos.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
  private final RagService rag;

  @PostMapping("/ingest")
  public IngestRes ingest(@RequestBody IngestReq req) throws Exception {
    int n = rag.ingest(req.docId(), req.text());
    return new IngestRes(req.docId(), n);
  }

  @PostMapping("/ask")
  public AskRes ask(@RequestBody AskReq req) throws Exception {
    int k = req.topK() != null ? req.topK() : 5;
    String ans = rag.ask(req.question(), k);
    return new AskRes(ans);
  }


  @PostMapping(path = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream(@RequestBody AskReq req) {
    SseEmitter emitter = new SseEmitter(Duration.ofMinutes(5).toMillis());
    CompletableFuture.runAsync(() -> {
      try {
        String full = rag.ask(req.question(), req.topK() == null ? 5 : req.topK());
        int step = Math.max(1, full.length() / 40);
        for (int i = 0; i < full.length(); i += step) {
          String slice = full.substring(i, Math.min(full.length(), i + step));
          emitter.send(SseEmitter.event().data(new Chunk(slice, false)));
        }
        emitter.send(SseEmitter.event().data(new Chunk("", true)));
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
    });
    return emitter;
  }
}
