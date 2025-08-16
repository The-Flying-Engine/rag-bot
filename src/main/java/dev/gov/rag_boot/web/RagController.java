package dev.gov.rag_boot.web;

import dev.gov.rag_boot.core.RagService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller @RequiredArgsConstructor
public class RagController {
  private final RagService rag;

  @GetMapping("/")
  public String home() { return "index"; }

  @PostMapping("/ingest")
  public String ingest(@RequestParam String docId, @RequestParam String text, Model model) throws Exception {
    int n = rag.ingest(docId, text);
    model.addAttribute("ingestMsg", "Ingested " + n + " chunks for docId=" + docId);
    return "index";
  }

  @PostMapping("/ask")
  public String ask(@RequestParam String question, @RequestParam(defaultValue = "5") int topK, Model model) throws Exception {
    String ans = rag.ask(question, topK);
    model.addAttribute("answer", ans);
    return "index";
  }
}
