package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.entity.Friend;
import com.Mike12138210.SmartNote.entity.FriendApply;
import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.mapper.FriendApplyMapper;
import com.Mike12138210.SmartNote.mapper.FriendMapper;
import com.Mike12138210.SmartNote.mapper.UserMapper;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.Mike12138210.SmartNote.vo.PendingApplyVO;
import com.Mike12138210.SmartNote.vo.UserSearchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if(keyword == null || keyword.trim().isEmpty()){
            return new ArrayList<>();
        }

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        // WHERE (phone = ? OR email = ? OR username LIKE ?) AND id != ?
        userWrapper.and(wrapper -> wrapper
                .eq(User::getPhone,keyword)
                .or()
                .eq(User::getEmail,keyword)
                .or()
                .like(User::getUsername,keyword)
        );
        userWrapper.ne(User::getId,currentUserId);
        List<User> users = userMapper.selectList(userWrapper);
        return users.stream()
                .map(UserSearchVO::new)
                .collect(Collectors.toList());
    }

    // 发送好友申请
    public UserSearchVO sendFriendApply(Long fromUserId,Long toUserId){
        User targetUser = userMapper.selectById(toUserId);
        if(targetUser == null || targetUser.getDeleted() == 1){
            throw new RuntimeException("对不起，该用户不存在或已注销");
        }
        if(fromUserId.equals(toUserId)){
            throw new RuntimeException("抱歉，您不能添加自己为好友。");
        }
        if(isFriend(fromUserId,toUserId)){
            return new UserSearchVO(targetUser);
        }
        LambdaQueryWrapper<FriendApply> friendApplyWrapper = new LambdaQueryWrapper<>();
        friendApplyWrapper.eq(FriendApply::getFromUserId,fromUserId)
                .eq(FriendApply::getToUserId,toUserId)
                .eq(FriendApply::getStatus,0);
        if(friendApplyMapper.selectCount(friendApplyWrapper) > 0){
            throw new RuntimeException("您已发送过好友申请，请等待对方处理");
        }
        FriendApply apply = new FriendApply();
        apply.setFromUserId(fromUserId);
        apply.setToUserId(toUserId);
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        friendApplyMapper.insert(apply);
        return null;
    }

    // 查看好友申请
    public List<PendingApplyVO> getPendingApplications(Long userId){
        LambdaQueryWrapper<FriendApply> applicationsWrapper = new LambdaQueryWrapper<>();
        applicationsWrapper.eq(FriendApply::getToUserId,userId)
                .eq(FriendApply::getStatus,0)
                .orderByDesc(FriendApply::getApplyTime);
        List<FriendApply> applies = friendApplyMapper.selectList(applicationsWrapper);

        List<PendingApplyVO> result = new ArrayList<>();
        for(FriendApply apply : applies){
            User fromUser = userMapper.selectById(apply.getFromUserId());
            if(fromUser != null && fromUser.getDeleted() == 0){
                PendingApplyVO vo = new PendingApplyVO();
                vo.setApplyId(apply.getId());
                vo.setFromUserId(apply.getFromUserId());
                vo.setUsername(fromUser.getUsername());
                vo.setAvatar(fromUser.getAvatar());
                vo.setMotto(fromUser.getMotto());
                vo.setApplyTime(apply.getApplyTime());
                result.add(vo);
            }
        }
        return result;
    }
}