package com.Mike12138210.SmartNote.dto;

import com.Mike12138210.SmartNote.entity.User;

public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String motto;

    public UserInfoResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.avatar = user.getAvatar();
        this.motto = user.getMotto();
    }

    public Long getId(){return id;}

    public String getUsername() {return username;}

    public String getNickname() {return nickname;}

    public String getEmail() {return email;}

    public String getPhone() {return phone;}

    public String getAvatar() {return avatar;}

    public String getMotto() {return motto;}

    public void setId(Long id) {this.id = id;}

    public void setUsername(String username) {this.username = username;}

    public void setNickname(String nickname) {this.nickname = nickname;}

    public void setEmail(String email) {this.email = email;}

    public void setPhone(String phone) {this.phone = phone;}

    public void setAvatar(String avatar) {this.avatar = avatar;}

    public void setMotto(String motto) {this.motto = motto;}
}