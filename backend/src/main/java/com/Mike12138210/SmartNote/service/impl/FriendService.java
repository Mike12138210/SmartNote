package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.entity.Friend;
import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.mapper.FriendApplyMapper;
import com.Mike12138210.SmartNote.mapper.FriendMapper;
import com.Mike12138210.SmartNote.mapper.UserMapper;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.Mike12138210.SmartNote.vo.UserSearchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private FriendApplyMapper friendApplyMapper;
    @Autowired
    private UserMapper userMapper;

    // 获取当前用户Id
    public Long getCurrentUserId() {
        Long userId = ThreadLocalUtil.get();
        if (userId == null) {
            throw new RuntimeException("用户未登录，请重试。");
        }
        return userId;
    }

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

    // 搜索用户
    public List<UserSearchVO> searchUsers(String keyword,Long currentUserId){
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone,keyword)
                .or()
                .eq(User::getEmail,keyword)
                .ne(User::getId,currentUserId); // 默认与前面的条件用AND连接，WHERE (phone = ? OR email = ?) AND id != ?
        List<User> users = userMapper.selectList(userWrapper);
        return users.stream()
                .map(UserSearchVO::new)
                .collect(Collectors.toList());
    }
}