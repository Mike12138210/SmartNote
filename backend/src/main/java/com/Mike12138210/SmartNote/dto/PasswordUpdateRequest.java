package com.Mike12138210.SmartNote.dto;

public class PasswordUpdateRequest {
    private String oldPassword;
    private String newPassword;

    public String getOldPassword() {return oldPassword;}

    public void setOldPassword(String oldPassword) {this.oldPassword = oldPassword;}

    public String getNewPassword() {return newPassword;}

    public void setNewPassword(String newPassword) {this.newPassword = newPassword;}
}
