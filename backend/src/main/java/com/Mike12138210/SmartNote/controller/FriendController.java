package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.service.impl.FriendService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.vo.UserSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/friends")
public class FriendController {
    @Autowired
    private FriendService friendService;

    // 搜索用户
    @GetMapping("/search")
    public Result<List<UserSearchVO>> searchUsers(@RequestParam String keyword){
        return Result.success(friendService.searchUsers(keyword, friendService.getCurrentUserId()));
    }
}