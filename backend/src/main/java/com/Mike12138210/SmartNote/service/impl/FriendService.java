package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.entity.Friend;
import com.Mike12138210.SmartNote.mapper.FriendMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendService {
    @Autowired
    private FriendMapper friendMapper;

    // 判断两人是否为好友
    public boolean isFriend(Long userId,Long otherId){
        // 检查userId是否将otherId加为好友
        LambdaQueryWrapper<Friend> friendWrapper1 = new LambdaQueryWrapper<>();
        friendWrapper1.eq(Friend::getUserId,userId)
                .eq(Friend::getFriendId,otherId);

        // 检查otherId是否将uerId加为好友
        LambdaQueryWrapper<Friend> friendWrapper2 = new LambdaQueryWrapper<>();
        friendWrapper2.eq(Friend::getUserId,otherId)
                .eq(Friend::getFriendId,userId);
        return friendMapper.selectCount(friendWrapper1) > 0 && friendMapper.selectCount(friendWrapper2) > 0;
    }
}