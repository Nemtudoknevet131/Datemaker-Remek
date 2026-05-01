package com.example.datemaker.model;

public class PartnerResponse {
    private boolean hasPartner;
    private User partner;
    private String relationshipStartDate;

    public boolean isHasPartner() {
        return hasPartner;
    }

    public void setHasPartner(boolean hasPartner) {
        this.hasPartner = hasPartner;
    }

    public User getPartner() {
        return partner;
    }

    public void setPartner(User partner) {
        this.partner = partner;
    }

    public String getRelationshipStartDate() {
        return relationshipStartDate;
    }

    public void setRelationshipStartDate(String relationshipStartDate) {
        this.relationshipStartDate = relationshipStartDate;
    }
}
