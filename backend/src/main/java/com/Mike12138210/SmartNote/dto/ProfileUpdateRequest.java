package com.Mike12138210.SmartNote.dto;

public class ProfileUpdateRequest {
    private String nickname;
    private String avatar;
    private String motto;

    public String getMotto() {return motto;}

    public void setMotto(String motto) {this.motto = motto;}

    public String getAvatar() {return avatar;}

    public void setAvatar(String avatar) {this.avatar = avatar;}

    public String getNickname() {return nickname;}

    public void setNickname(String nickname) {this.nickname = nickname;}
}