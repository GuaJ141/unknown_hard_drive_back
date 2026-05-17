package com.unknownharddrivesystem.ws;

public class WebSocketMessage {
    private Boolean systemInfo;

    private String message;

    private int toUser;

    private int fromUser;

    public String getMessage() {
        return message;
    }

    public WebSocketMessage() {
    }

    public WebSocketMessage(Boolean systemInfo, String message, int toUser, int fromUser) {
        this.systemInfo = systemInfo;
        this.message = message;
        this.toUser = toUser;
        this.fromUser = fromUser;
    }

//    public WebSocketMessage(String message, int toUser, int fromUser) {
//        this.systemInfo = false;
//        this.message = message;
//        this.toUser = toUser;
//        this.fromUser = fromUser;
//    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getToUser() {
        return toUser;
    }

    public void setToUser(int toUser) {
        this.toUser = toUser;
    }

    public int getFromUser() {
        return fromUser;
    }

    public void setFromUser(int fromUser) {
        this.fromUser = fromUser;
    }

    public Boolean getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(Boolean systemInfo) {
        this.systemInfo = systemInfo;
    }
}
