package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.PasswordUpdateRequest;
import com.Mike12138210.SmartNote.dto.ProfileUpdateRequest;
import com.Mike12138210.SmartNote.service.impl.UserService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.Mike12138210.SmartNote.vo.UserInfoVO;
import jakarta.validation.Valid;
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
    private Result<UserInfoVO> getCurrentUser(){
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number)claims.get("userId")).longValue();

        UserInfoVO userInfo = userService.getUserInfo(userId);
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

    // 修改密码
    @PutMapping("/me/password")
    public Result<?> updatePassword(@Valid @RequestBody PasswordUpdateRequest request){
        Long userId = getCurrentUserId();
        userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success(null);
    }
}