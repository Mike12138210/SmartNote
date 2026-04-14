package com.Mike12138210.SmartNote.vo;

import com.Mike12138210.SmartNote.entity.User;

import java.time.LocalDateTime;

public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String motto;
    private LocalDateTime createTime;

    public UserInfoVO(User user) {
        this.id = user.getUid();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.avatar = user.getAvatar();
        this.motto = user.getMotto();
        this.createTime = user.getCreateTime();
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

    public LocalDateTime getCreateTime() {return createTime;}

    public void setCreateTime(LocalDateTime createTime) {this.createTime = createTime;}
}