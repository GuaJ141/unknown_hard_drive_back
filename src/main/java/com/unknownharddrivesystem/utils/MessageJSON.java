package com.unknownharddrivesystem.utils;

import java.util.Set;

public class MessageJSON {
    private boolean systemInfo;

    private String message;

    private Set<Integer> userUId;


    public MessageJSON(boolean systemInfo, String message, Set<Integer> userUId) {
        this.systemInfo = systemInfo;
        this.message = message;
        this.userUId = userUId;
    }

    public boolean isSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(boolean systemInfo) {
        this.systemInfo = systemInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<Integer> getUserUId() {
        return userUId;
    }

    public void setUserUId(Set<Integer> userUId) {
        this.userUId = userUId;
    }
}
