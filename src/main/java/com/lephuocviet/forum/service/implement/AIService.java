package com.lephuocviet.forum.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lephuocviet.forum.service.IAIService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AIService implements IAIService {

    @Value("${AI_API.URL}")
    String AI_URL;

    @Value("${AI_API.KEY}")
    String API_KEY;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean checkPostIsLanguage(String language, String title, String content) {
        // Tạo header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tạo nội dung đầu vào
        String inputText = String.format(
                "This is a forum for sharing knowledge about multilingualism. "
                        + "Given the title \"%s\" and content \"%s\", is this article appropriate for a forum about languages "
                        + "and is it written in %s language? Please answer true or false no explain anything, only true or false.",
                title, content, language
        );

        // Body request dưới dạng JSON
        String requestBody = String.format(
                "{ \"inputs\": \"%s\" }",
                inputText.replace("\"", "\\\"") // Escape ký tự nháy kép nếu cần
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Gửi yêu cầu tới API
            ResponseEntity<String> response = restTemplate.exchange(
                    AI_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                String aiResponse = responseBody.get(0).get("generated_text").asText().split("\n")[0].trim();
                String[] words = aiResponse.split("\\s+");
                int length = words.length;
                String lastSixWords = "";
                for (int i = Math.max(0, length - 6); i < length; i++) {
                    lastSixWords += words[i] + " ";
                }
                lastSixWords = lastSixWords.trim();
                System.out.println(lastSixWords);
                if (lastSixWords.contains("True")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
