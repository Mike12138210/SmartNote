package com.Mike12138210.SmartNote.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateFriendGroupRequest {
    @NotBlank(message = "分组名不能为空")
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}