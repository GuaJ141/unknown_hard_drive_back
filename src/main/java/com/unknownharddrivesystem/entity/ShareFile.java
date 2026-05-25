package com.unknownharddrivesystem.entity;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.TimeUnit;

public class ShareFile {
    private int fileId;
    private String token;
    private long availTime;
    //1：秒(SECONDS)  2：分(MINUTES)  3：时(HOURS)  4：天(DAYS) null:永远
    private TimeUnit option;

    public ShareFile() {
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getAvailTime() {
        return availTime;
    }

    public void setAvailTime(long availTime) {
        this.availTime = availTime;
    }

    public TimeUnit getOption() {
        return option;
    }

    public void setOption(TimeUnit option) {
        this.option = option;
    }
}
