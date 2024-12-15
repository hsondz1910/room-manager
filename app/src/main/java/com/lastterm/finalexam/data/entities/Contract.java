package com.lastterm.finalexam.data.entities;

import java.util.Date;

public class Contract {
    private String contractId; // Contract ID
    private String tenantId; // Tenant ID
    private String ownerId; // Owner ID
    private String roomId; // Room ID rented by tenant
    private Date startDate; // Contract start date
    private Date endDate; // Contract end date
    private double rentAmount; // Rent amount
    private boolean isActive; // Contract status (active or expired)
    private String contractTerms; // Contract terms
    private boolean isSelected;

    public Contract() {}

    public Contract(String contractId, String tenantId, String ownerId, String roomId, Date startDate, Date endDate, double rentAmount, boolean isActive, String contractTerms) {
        this.contractId = contractId;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentAmount = rentAmount;
        this.isActive = isActive;
        this.contractTerms = contractTerms;
    }

    public Contract(String tenantId, String ownerId, String roomId, Date startDate, Date endDate, double rentAmount, boolean isActive, String contractTerms) {
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentAmount = rentAmount;
        this.isActive = isActive;
        this.contractTerms = contractTerms;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getContractTerms() {
        return contractTerms;
    }

    public void setContractTerms(String contractTerms) {
        this.contractTerms = contractTerms;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
