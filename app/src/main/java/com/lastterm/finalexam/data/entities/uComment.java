package com.lastterm.finalexam.data.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class uComment {
    private String iD;
    private String userId;
    private String name;
    private String rate;
    private Date date;
    private String comment;

    // Default constructor (required for Firestore deserialization)
    public uComment() {
    }

    // Constructor with parameters
    public uComment(String iD, String userId, String name, String rate, Date date, String comment) {
        this.iD = iD;
        this.userId = userId;
        this.name = name;
        this.rate = rate;
        this.date = date;
        this.comment = comment;
    }

    // Getters and Setters
    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> city = new HashMap<>();
        city.put("userId", userId);
        city.put("name", name);
        city.put("rate", rate);
        city.put("date", date);
        city.put("comment", comment);
        return city;
    }


}
