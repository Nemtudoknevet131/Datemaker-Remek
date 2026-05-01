package com.example.demo.service;

import com.example.demo.dto.RecaptchaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verify(String token) {
        if (token == null || token.isBlank()) {
            log.warn("reCAPTCHA token is null/blank");
            return false;
        }

        String url = "https://www.google.com/recaptcha/api/siteverify";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", recaptchaSecret);
        body.add("response", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<RecaptchaResponse> response = restTemplate.postForEntity(url, request, RecaptchaResponse.class);

        RecaptchaResponse recaptchaResponse = response.getBody();
        if (recaptchaResponse == null) {
            log.warn("reCAPTCHA response body was null");
            return false;
        }

        if (!recaptchaResponse.isSuccess()) {
            log.warn("reCAPTCHA failed. Hostname: {}, errors: {}",
                    recaptchaResponse.getHostname(),
                    recaptchaResponse.getErrorCodes());
        }

        return recaptchaResponse.isSuccess();
    }
}