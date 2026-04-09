package com.Mike12138210.SmartNote.vo;

import com.Mike12138210.SmartNote.entity.User;

public class UserSearchVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String motto;

    public UserSearchVO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.motto = user.getMotto();
    }

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getMotto() { return motto; }
    public void setMotto(String motto) { this.motto = motto; }
}