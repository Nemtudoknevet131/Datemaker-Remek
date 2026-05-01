package com.example.demo.push;

import com.example.demo.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FireBaseManager {
    private static final String PROJECT_ID = "arboreal-totem-475109-p7";
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID
            + "/messages:send";;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendPartnerRequestNotif(String targetToken, String senderName) {
        String title = "New partner request";
        String body = senderName + " has sent you a partner request";
        sendMessage(targetToken, title, body, "PARTNER_REQUEST");
    }

    public void sendMessage(String targetToken, String title, String body, String type) {
        sendMessage(targetToken, title, body, type, null);
    }

    public void sendMessage(String targetToken, String title, String body, String type, MessageDto dto) {
        try {
            String accessToken = getAccessToken();

            Map<String, String> data = new HashMap<>();

            if (type != null) {
                data.put("type", type);
            }

            if (dto != null) {
                data.put("messageId", String.valueOf(dto.getId()));
                data.put("senderId", String.valueOf(dto.getSenderId()));
                data.put("receiverId", String.valueOf(dto.getReceiverId()));
                data.put("content", dto.getContent());
                if (dto.getCreatedAt() != null) {
                    data.put("createdAt", dto.getCreatedAt().toString());
                }
            }

            Map<String, Object> message = Map.of(
                    "message", Map.of(
                            "token", targetToken,
                            "notification", Map.of(
                                    "title", title,
                                    "body", body),
                            "data", data));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(message, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(FCM_ENDPOINT, entity, String.class);

            System.out.println("FCM V1 Response: " + response.getStatusCode() + " / " + response.getBody());
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            System.out.println("FCM Error: " + e.getStatusCode() + " - " + responseBody);

            if (e.getStatusCode() == HttpStatus.NOT_FOUND &&
                    responseBody != null && // CHANGED
                    responseBody.contains("\"errorCode\":\"UNREGISTERED\"")) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAccessToken() throws IOException {
        InputStream serviceAccountStream = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}