package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.FriendApplyRequest;
import com.Mike12138210.SmartNote.dto.UpdateFriendGroupRequest;
import com.Mike12138210.SmartNote.service.impl.FriendService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.vo.FriendVO;
import com.Mike12138210.SmartNote.vo.PendingApplyVO;
import com.Mike12138210.SmartNote.vo.UserSearchVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    @Autowired
    private FriendService friendService;

    // 搜索用户
    @GetMapping("/search")
    public Result<List<UserSearchVO>> searchUsers(@RequestParam String keyword){
        return Result.success(friendService.searchUsers(keyword, friendService.getCurrentUserId()));
    }

    // 发送好友申请
    @PostMapping("/requests")
    public Result<UserSearchVO> sendFriendApply(@Valid @RequestBody FriendApplyRequest request){
        Long currentUsrId = friendService.getCurrentUserId();
        UserSearchVO result = friendService.sendFriendApply(currentUsrId,request.getFriendId());
        if(result != null){
            return Result.success(result);
        }else{
            return Result.success("好友申请发送成功，请等待对方同意",null);
        }
    }

    // 查看好友申请
    @GetMapping("/requests/pending")
    public Result<List<PendingApplyVO>> getPendingApplications(){
        Long userId = friendService.getCurrentUserId();
        List<PendingApplyVO> list = friendService.getPendingApplications(userId);
        return Result.success(list);
    }

    // 同意好友申请
    @PutMapping("/requests/{applyId}/approve")
    public Result<?> approveApply(@PathVariable Long applyId){
        Long userId = friendService.getCurrentUserId();
        friendService.approveApply(applyId,userId);
        return Result.success("已成功添加该好友",null);
    }

    // 拒绝好友申请
    @PutMapping("/requests/{applyId}/reject")
    public Result<?> rejectApply(@PathVariable Long applyId){
        Long userId = friendService.getCurrentUserId();
        friendService.rejectApply(applyId,userId);
        return Result.success("已拒绝该好友申请",null);
    }

    // 查看好友列表
    @GetMapping
    public Result<Page<FriendVO>> getFriendList(@RequestParam(defaultValue = "1")int pageNum,
                                                @RequestParam(defaultValue = "10")int pageSize,
                                                @RequestParam(required = false)String groupName){
        Long userId = friendService.getCurrentUserId();
        Page<FriendVO> page = friendService.getFriendList(pageNum,pageSize,groupName,userId);
        return Result.success(page);
    }

    // 修改好友分组
    @PutMapping("/{friendId}/group")
    public Result<?> updateFriendGroup(@PathVariable Long friendId,
                                       @Valid @RequestBody UpdateFriendGroupRequest request){
        Long currentUserId = friendService.getCurrentUserId();
        friendService.updateFriendGroup(currentUserId,friendId,request.getGroupName());
        return Result.success("分组修改成功",null);
    }
}