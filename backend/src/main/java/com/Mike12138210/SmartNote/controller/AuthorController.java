package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.LoginRequest;
import com.Mike12138210.SmartNote.dto.RegisterRequest;
import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.service.impl.UserService;
import com.Mike12138210.SmartNote.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthorController {
    @Autowired
    private UserService userService;

    // 注册
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request){
        if(!request.getPassword().equals(request.getConfirmPassword())){
            return Result.error("两次密码输入不一致，请重新输入。");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());

        try {
            userService.register(user);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 登录
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request){
        String token = userService.login(request);
        Map<String, String> data = new HashMap<>();
        data.put("token",token);
        return Result.success(data);
    }
}