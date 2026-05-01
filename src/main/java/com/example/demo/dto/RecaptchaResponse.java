// src/main/java/com/example/demo/dto/RecaptchaResponse.java
package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RecaptchaResponse {

    private boolean success;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    private String hostname;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    public boolean isSuccess() {
        return success;
    }

    public String getChallengeTs() {
        return challengeTs;
    }

    public String getHostname() {
        return hostname;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }
}
