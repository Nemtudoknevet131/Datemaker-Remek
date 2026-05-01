package com.example.datemaker.model;

import com.google.gson.annotations.SerializedName;

public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    @SerializedName("userName")
    private String username;
    private String email;
    private boolean emailVerified;
    private boolean adult;
    private String dateOfBirth;
    private Long partnerId;
    private String avatarUrl;
    private boolean premium;

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isAdult() {
        return adult;
    }

    public Long getPartnerId() {
        return partnerId;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }
}

