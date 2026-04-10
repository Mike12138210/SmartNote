package com.Mike12138210.SmartNote.dto;

import jakarta.validation.constraints.NotNull;

public class FriendApplyRequest {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}