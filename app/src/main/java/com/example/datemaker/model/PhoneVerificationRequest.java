package com.example.datemaker.model;

public class PhoneVerificationRequest {

    private String code;

    public PhoneVerificationRequest() {

    }

    public PhoneVerificationRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
