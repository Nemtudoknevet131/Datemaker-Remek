package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiChatServiceImpl implements AiChatService {
    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generateDateIdea(Long userId, String userMessage) {
        GroqChatRequest requestBody = new GroqChatRequest();

        requestBody.setModel(groqModel);
        requestBody.setMessages(List.of(
                new GroqMessage("system",
                        "You are an AI Date Planner for couples. "
                                + "You suggest specific, practical date ideas. "
                                + "Answer concisely."),
                new GroqMessage("user", userMessage)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<GroqChatRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GroqChatResponse> response = restTemplate.exchange(
                    GROQ_URL,
                    HttpMethod.POST,
                    entity,
                    GroqChatResponse.class);

            GroqChatResponse body = response.getBody();

            if (body != null && body.getChoices() != null && !body.getChoices().isEmpty()
                    && body.getChoices().get(0).getMessage() != null) {
                return body.getChoices().get(0).getMessage().getContent();
            }

            return "Sorry, I couldn't come up with a date idea right now. Please try again.";
        } catch (Exception e) {
            return "I had trouble reaching the AI service. Please try again later.";
        }
    }

    static class GroqChatRequest {
        private String model;
        private List<GroqMessage> messages;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<GroqMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<GroqMessage> messages) {
            this.messages = messages;
        }
    }

    static class GroqMessage {
        private String role;
        private String content;

        public GroqMessage() {
        }

        public GroqMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    static class GroqChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
    }

    static class Choice {
        private GroqMessage message;

        public GroqMessage getMessage() {
            return message;
        }

        public void setMessage(GroqMessage message) {
            this.message = message;
        }
    }
}