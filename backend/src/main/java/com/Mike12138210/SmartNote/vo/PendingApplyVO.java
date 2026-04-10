package com.Mike12138210.SmartNote.vo;

import java.time.LocalDateTime;

public class PendingApplyVO {
    private Long applyId;
    private Long fromUserId;
    private String username;
    private String avatar;
    private String motto;
    private LocalDateTime applyTime;

    public PendingApplyVO(){}

    public PendingApplyVO(Long applyId, Long fromUserId, String username, String avatar, String motto, LocalDateTime applyTime) {
        this.applyId = applyId;
        this.fromUserId = fromUserId;
        this.username = username;
        this.avatar = avatar;
        this.motto = motto;
        this.applyTime = applyTime;
    }

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }
}