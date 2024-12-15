package com.lastterm.finalexam.data.entities;

import java.util.List;

public class DepositRequest {
    private String requestId; // ID of the deposit request
    private String userId; // ID of the user
    private String roomId; // ID of the room
    private double depositAmount; // Deposit amount
    private String status; // Status of the request (pending, approved, rejected)
    private long timestamp; // Time the request was created
    private String ownerId; // ID of the room owner
    private List<String> roomImageUrls;

    public DepositRequest() {}

    public DepositRequest(String requestId, String userId, String roomId, double depositAmount, String status, long timestamp, String ownerId) {
        this.requestId = requestId;
        this.userId = userId;
        this.roomId = roomId;
        this.depositAmount = depositAmount;
        this.status = status;
        this.timestamp = timestamp;
        this.ownerId = ownerId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getRoomImageUrls() {
        return roomImageUrls;
    }

    public void setRoomImageUrls(List<String> roomImageUrls) {
        this.roomImageUrls = roomImageUrls;
    }
}