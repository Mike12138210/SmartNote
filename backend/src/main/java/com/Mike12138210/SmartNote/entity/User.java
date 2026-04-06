package com.Mike12138210.SmartNote.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@TableName("user") // 指定该实体类对应的表名
public class User {
    // id,username,email,phone,nickname,avatar,motto
    @TableId(type = IdType.AUTO) // 标记主键字段，并指定主键生成策略为数据库自增
    private Long id;
    private String username;
    @Email
    private String email;
    @NotEmpty
    private String phone;
    private String password; // 存储 BCrypt 加密后的密文
    private String nickname; // 昵称
    private String avatar; // 头像地址
    private String motto; // 座右铭

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充（配合自动填充处理器）
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime;

    @TableLogic // 逻辑删除注解，MyBatis-Plus 会自动在查询条件中加入 `deleted=0`
    private Integer deleted; // 0-未删除，1-已删除

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}