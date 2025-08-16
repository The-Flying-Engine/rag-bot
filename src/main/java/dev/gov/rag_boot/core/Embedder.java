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
public class Embedder {
  @Value("${llm.base-url}") private String baseUrl;
  @Value("${llm.api-key:}") private String apiKey;
  @Value("${llm.embed-model}") private String embedModel;

  private final ObjectMapper om = new ObjectMapper();

  public float[] embedOne(String text) throws Exception {
    try (CloseableHttpClient http = HttpClients.createDefault()) {
      HttpPost req = new HttpPost(baseUrl + "/embeddings");
      if (apiKey != null && !apiKey.isBlank()) req.addHeader("Authorization","Bearer " + apiKey);
      String body = om.createObjectNode().put("model", embedModel).put("input", text).toString();
      req.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
      var res = http.execute(req);
      JsonNode json = om.readTree(res.getEntity().getContent());
      JsonNode arr = json.get("data").get(0).get("embedding");
      float[] v = new float[arr.size()];
      for (int i = 0; i < arr.size(); i++) v[i] = (float) arr.get(i).asDouble();
      return v;
    }
  }
}
