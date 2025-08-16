package dev.gov.rag_boot.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatClient {
  @Value("${llm.base-url}") private String baseUrl;
  @Value("${llm.api-key:}") private String apiKey;
  @Value("${llm.chat-model}") private String chatModel;

  private final ObjectMapper om = new ObjectMapper();

  public String complete(String prompt) throws Exception {
    try (CloseableHttpClient http = HttpClients.createDefault()) {
      HttpPost req = new HttpPost(baseUrl + "/chat/completions");
      if (apiKey != null && !apiKey.isBlank()) req.addHeader("Authorization","Bearer " + apiKey);
      var body = om.createObjectNode()
        .put("model", chatModel)
        .set("messages", om.createArrayNode()
          .add(om.createObjectNode().put("role","system").put("content","Answer ONLY from given context. If unsure, say \"I don't know\". Keep answers short."))
          .add(om.createObjectNode().put("role","user").put("content", prompt))
        );
      req.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));
      var res = http.execute(req);
      JsonNode json = om.readTree(res.getEntity().getContent());
      return json.get("choices").get(0).get("message").get("content").asText();
    }
  }
}
