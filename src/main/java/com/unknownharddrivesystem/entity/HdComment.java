package com.unknownharddrivesystem.entity;

import java.sql.Timestamp;

public class HdComment {
    private int id;
    private Timestamp commentTime;
    private String comment;
    private String username;
    private int postId;
    private int status;

    @Override
    public String toString() {
        return "hdComment{" +
                "id=" + id +
                ", commentTime=" + commentTime +
                ", comment='" + comment + '\'' +
                ", username='" + username + '\'' +
                ", postId=" + postId +
                ", status=" + status +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(Timestamp commentTime) {
        this.commentTime = commentTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
