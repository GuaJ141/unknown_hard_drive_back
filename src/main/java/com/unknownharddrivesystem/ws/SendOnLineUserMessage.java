package com.unknownharddrivesystem.ws;

import java.util.Set;

public class SendOnLineUserMessage {
    private Boolean systemInfo;

    private Set<Integer> userUId;

    public SendOnLineUserMessage() {
    }

    public SendOnLineUserMessage(boolean systemInfo, Set<Integer> userUId) {
        this.systemInfo = systemInfo;
        this.userUId = userUId;

    }

    public boolean isSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(boolean systemInfo) {
        this.systemInfo = systemInfo;
    }

    public Set<Integer> getUserUId() {
        return userUId;
    }

    public void setUserUId(Set<Integer> userUId) {
        this.userUId = userUId;
    }
}
