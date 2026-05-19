package com.unknownharddrivesystem.ws;

import java.util.Objects;
import java.util.Set;

public class SendOnLineUserMessage {
    private Boolean systemInfo;

    //0用户下线 1用户上线 2上线首次获取在线用户
    private int optionCode;

    private Object userUId;

    public SendOnLineUserMessage() {
    }

    public SendOnLineUserMessage(Boolean systemInfo, int optionCode, Object userUId) {
        this.systemInfo = systemInfo;
        this.optionCode = optionCode;
        this.userUId = userUId;
    }

    public Boolean getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(Boolean systemInfo) {
        this.systemInfo = systemInfo;
    }

    public int getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(int optionCode) {
        this.optionCode = optionCode;
    }

    public Object getUserUId() {
        return userUId;
    }

    public void setUserUId(Object userUId) {
        this.userUId = userUId;
    }
}
