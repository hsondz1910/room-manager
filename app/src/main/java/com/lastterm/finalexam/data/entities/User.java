package com.lastterm.finalexam.data.entities;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String urlAvatar;

    public User() {
    }

    public User(String fullName, String username, String email, String phone, String role, String urlAvatar) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.urlAvatar = urlAvatar;
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
        map.put("fullName", fullName);
        map.put("username", username);
        map.put("email", email);
        map.put("phone", phone);
        map.put("role", role);
        map.put("urlAvatar", urlAvatar);
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
        return user;
    }
}
