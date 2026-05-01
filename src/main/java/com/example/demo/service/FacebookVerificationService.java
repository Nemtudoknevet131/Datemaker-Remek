package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
public class FacebookVerificationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${facebook.app-id}")
    private String appId;

    @Value("${facebook.app-secret}")
    private String appSecret;

    public FacebookVerificationService(RestTemplateBuilder builder) {
        // Configure timeouts via the request factory (non-deprecated)
        this.restTemplate = builder
                .requestFactory(() -> {
                    var f = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                    // timeouts in milliseconds
                    f.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    f.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
                    return f;
                })
                .build();
    }

    public FbInfo verify(String accessToken) {
        try {
            log.info("[FB] verify() called");

            // 1) Optional but helpful: debug/validate token for *your* app
            String appAccessToken = appId + "|" + appSecret;

            URI debugUri = UriComponentsBuilder
                    .fromUriString("https://graph.facebook.com/v18.0/debug_token")
                    .queryParam("input_token", accessToken)
                    .queryParam("access_token", appAccessToken)
                    .encode()
                    .build()
                    .toUri();

            ResponseEntity<String> debugResp = restTemplate.getForEntity(debugUri, String.class);
            log.debug("FB debug_token status={} body={}", debugResp.getStatusCode(), debugResp.getBody());

            if (!debugResp.getStatusCode().is2xxSuccessful() || debugResp.getBody() == null) {
                return null;
            }
            JsonNode debugData = objectMapper.readTree(debugResp.getBody()).path("data");
            if (!debugData.path("is_valid").asBoolean(false))
                return null;
            if (!appId.equals(debugData.path("app_id").asText()))
                return null;

            log.info("[FB] debug_token status={} body={}", debugResp.getStatusCode(), debugResp.getBody());

            // 2) Recommended: appsecret_proof
            String proof = hmacSha256(accessToken, appSecret);

            URI meUri = UriComponentsBuilder
                    .fromUriString("https://graph.facebook.com/v18.0/me")
                    .queryParam("fields", "id,first_name,last_name,name,email,birthday")
                    .queryParam("access_token", accessToken)
                    .queryParam("appsecret_proof", proof)
                    .encode()
                    .build()
                    .toUri();

            ResponseEntity<String> meResp = restTemplate.getForEntity(meUri, String.class);
            log.debug("FB /me status={} body={}", meResp.getStatusCode(), meResp.getBody());

            if (!meResp.getStatusCode().is2xxSuccessful() || meResp.getBody() == null)
                return null;

            JsonNode node = objectMapper.readTree(meResp.getBody());
            String id = node.path("id").asText(null);
            String first = node.path("first_name").asText(null);
            String last = node.path("last_name").asText(null);
            String name = node.path("name").asText(null);
            String email = node.path("email").asText(null);
            String birthday = node.path("birthday").asText(null);

            log.info("[FB] parsed: id={} first={} last={} name={} email={}", id, first, last, name, email);

            if (id == null)
                return null;
            return new FbInfo(id, name, email, birthday);

        } catch (Exception e) {
            log.error("FB verify error", e);
            return null;
        }
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record FbInfo(String id, String name, String email, String birthday) {
    }

    public String getProfilePictureUrl(String accesstoken) {
        record FbPicData(String url, boolean is_silhouette) {
        }
        record FbPic(FbPicData data) {
        }

        String uri = "https://graph.facebook.com/v19.0/me/picture?type=large&redirect=false&access_token="
                + accesstoken;
        FbPic resp = new org.springframework.web.client.RestTemplate().getForObject(uri, FbPic.class);

        return (resp != null && resp.data() != null && !resp.data().is_silhouette()) ? resp.data().url : null;
    }
}