package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.service.IChatGPTService;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;




@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatGPTService implements IChatGPTService {
    @Value("${CHAT_GPT.URL}")
    private String OPENAI_API_URL ;
    @Value("${CHAT_GPT.KEY}")
    private String API_KEY ; // Thay bằng API key của bạn

    @Override
    public boolean checkPostIsLanguage(String language , String title, String content) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("Content-Type", "application/json");

        String question = "Please answer the result is true or false no explanation please answer anything more just answer true or false " +
                "This is a forum for sharing knowledge about languages " +
                "I have Title " + title + " and Content " + content + " " +
                "Please tell me if this article is suitable for a forum for sharing knowledge about multilingualism " +
                "And is it really of this " + language + " language.";

        String requestBody = "{"
                + "\"model\": \"gpt-3.5-turbo\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + question + "\"}],"
                + "\"max_tokens\": 10"
                + "}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String result = response.getBody();
            return result != null && result.toLowerCase().contains("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
