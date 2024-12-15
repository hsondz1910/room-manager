package com.lastterm.finalexam.data.entities;

import java.util.List;

public class ChatRoom {
    String id;
    String roomId;
    List<String> users;
    List<MessageClass> messages;


    public ChatRoom() {
    }

    public ChatRoom(String id, String roomId,List<String> users, List<MessageClass> messages) {
        this.id = id;
        this.roomId = roomId;
        this.users = users;
        this.messages = messages;
    }

    public ChatRoom(String roomId, String userIDSent, String userIDReceiver) {
        this.roomId = roomId;
        this.users = List.of(userIDSent, userIDReceiver);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<MessageClass> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageClass> messages) {
        this.messages = messages;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
