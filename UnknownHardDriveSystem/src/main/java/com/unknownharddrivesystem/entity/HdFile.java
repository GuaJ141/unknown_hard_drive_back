package com.unknownharddrivesystem.entity;

import java.sql.Timestamp;

public class HdFile {
    private int id;
    private String filename;
    private String path;
    private String username;
    private String type;
    private int status = 1;
    private Timestamp uploadTime;
    private int favor;
    private String fileformat;
    private long size;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "hdFile{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", path='" + path + '\'' +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", status=" + status +
                ", uploadTime=" + uploadTime +
                ", favor=" + favor +
                ", fileformat='" + fileformat + '\'' +
                ", size=" + size +
                '}';
    }

    public String getFileformat() {
        return fileformat;
    }

    public void setFileformat(String fileformat) {
        this.fileformat = fileformat;
    }

    public int getFavor() {
        return favor;
    }

    public void setFavor(int favor) {
        this.favor = favor;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
