package com.Mike12138210.SmartNote.dto;

import jakarta.validation.constraints.NotBlank;

public class NotePermissionRequest {
    @NotBlank(message = "笔记权限不能为空")
    private String permission;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
