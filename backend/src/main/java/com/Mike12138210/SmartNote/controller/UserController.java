package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.ProfileUpdateRequest;
import com.Mike12138210.SmartNote.dto.UserInfoResponse;
import com.Mike12138210.SmartNote.service.impl.UserService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 修改个人资料
    @PutMapping("/me/profile")
    private Result<?> updateProfile(@RequestBody ProfileUpdateRequest request){
        Long userId = getCurrentUserId();
        userService.updateProfile(userId,request);
        return Result.success(null);
    }

    // 获取当前登录用户的ID
    private Long getCurrentUserId(){
        Map<String, Object> claims = ThreadLocalUtil.get();
        if(claims == null){
            return null;
        }
        Object userId = claims.get("userId");
        if(userId == null){
            return null;
        }
        return ((Number)userId).longValue();
    }
}