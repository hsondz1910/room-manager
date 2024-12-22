package com.lastterm.finalexam.data.entities;

import java.util.HashMap;
import java.util.Map;

public class Appointment {
    private String id;
    private String chatID;
    private String title;
    private String description;
    private String Date;
    private String time;
    private String caledarID;
    private int status;

    public Appointment(){}

    public Appointment(String chatID, String title, String description, String date, String time, String caledarID, int status) {
        this.chatID = chatID;
        this.title = title;
        this.description = description;
        Date = date;
        this.time = time;
        this.caledarID = caledarID;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getCaledarID() {
        return caledarID;
    }

    public void setCaledarID(String caledarID) {
        this.caledarID = caledarID;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("chatID", chatID);
        result.put("title", title);
        result.put("description", description);
        result.put("date", Date);
        result.put("time", time);
        result.put("status", status);
        return result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
