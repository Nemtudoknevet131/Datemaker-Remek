package com.example.datemaker.model;

public class PartnerSearchResponse {
    UserDto user;
    private boolean alreadyPartnered;
    private boolean pendingRequestFromMe;

    public UserDto getUser() {
        return user;
    }

    public boolean isAlreadyPartnered() {
        return alreadyPartnered;
    }

    public boolean isPendingRequestFromMe() {
        return pendingRequestFromMe;
    }
}
