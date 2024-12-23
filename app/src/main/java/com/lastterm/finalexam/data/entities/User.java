package com.lastterm.finalexam.data.entities;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String urlAvatar;
    private boolean isActive;
    private boolean isSelected;

    public User() {
    }

    public User(String userId, String fullName, String username, String email, String phone, String role, String urlAvatar, boolean isActive) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.urlAvatar = urlAvatar;
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("fullName", fullName);
        map.put("username", username);
        map.put("email", email);
        map.put("phone", phone);
        map.put("role", role);
        map.put("urlAvatar", urlAvatar);
        map.put("isActive", isActive);
        return map;
    }

    public static User fromMap(Map<String, Object> map) {
        User user = new User();
        user.setFullName((String) map.get("fullName"));
        user.setUsername((String) map.get("username"));
        user.setEmail((String) map.get("email"));
        user.setPhone((String) map.get("phone"));
        user.setRole((String) map.get("role"));
        user.setUrlAvatar((String) map.get("urlAvatar"));
        user.setActive(map.get("isActive") != null ? (Boolean) map.get("isActive") : false);
        return user;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
