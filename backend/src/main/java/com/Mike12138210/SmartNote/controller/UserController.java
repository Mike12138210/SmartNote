package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.UserInfoResponse;
import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.service.impl.UserService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {
    @Autowired
    private UserService userService;

    // 获取当前登录用户信息
    @GetMapping("/me")
    private Result<UserInfoResponse> getCurrentUser(){
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number)claims.get("userId")).longValue();

        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }
}