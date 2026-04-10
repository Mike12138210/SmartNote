package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.FriendApplyRequest;
import com.Mike12138210.SmartNote.service.impl.FriendService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.vo.UserSearchVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 发送好友申请
    @PostMapping("request")
    public Result<UserSearchVO> sendFriendApply(@Valid @RequestBody FriendApplyRequest request){
        Long currentUsrId = friendService.getCurrentUserId();
        UserSearchVO result = friendService.sendFriendApply(currentUsrId,request.getFriendId());
        if(result != null){
            return Result.success(result);
        }else{
            return Result.success("好友申请发送成功，请等待对方同意。",null);
        }
    }
}