package com.Mike12138210.SmartNote.vo;

public class FriendVO {
    private Long friendId;
    private String username;
    private String nickname;
    private String avatar;
    private String motto;
    private String groupName;

    public FriendVO() {
    }

    public FriendVO(Long friendId, String username, String nickname, String avatar, String motto, String groupName) {
        this.friendId = friendId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.motto = motto;
        this.groupName = groupName;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}