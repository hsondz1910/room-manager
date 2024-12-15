package com.lastterm.finalexam.data.entities;

import java.util.List;

public class MessageClass {
    String id;
    String message;
    String senderID;
    String date;
    String img;
    boolean isRead;

    public MessageClass() {
    }

    public MessageClass(String message, String senderID, String date, String img, boolean isRead) {
        this.message = message;
        this.senderID = senderID;
        this.date = date;
        this.img = img;
        this.isRead = isRead;
    }

    public MessageClass(String id, String message, String senderID, String date, String img, boolean isRead) {
        this.id = id;
        this.message = message;
        this.senderID = senderID;
        this.date = date;
        this.img = img;
        this.isRead = isRead;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getsenderID() {
        return senderID;
    }

    public void setsenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
