package com.Mike12138210.SmartNote.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1,max = 20,message = "用户名长度必须在1-20之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 20,message = "密码长度必须在6-20之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @AssertTrue(message = "邮箱和手机号至少填写一个")
    public boolean isAtLeastOneContact(){
        return (email != null && !email.isEmpty()) || (phone != null && !phone.isEmpty());
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}