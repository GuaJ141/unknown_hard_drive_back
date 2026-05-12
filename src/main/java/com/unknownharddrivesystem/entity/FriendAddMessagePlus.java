package com.unknownharddrivesystem.entity;

public class FriendAddMessagePlus extends FriendAddMessage{
    private String username;

    private int uniqueId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }
}
