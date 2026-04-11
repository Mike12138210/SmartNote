package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.entity.Friend;
import com.Mike12138210.SmartNote.entity.FriendApply;
import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.mapper.FriendApplyMapper;
import com.Mike12138210.SmartNote.mapper.FriendMapper;
import com.Mike12138210.SmartNote.mapper.UserMapper;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.Mike12138210.SmartNote.vo.FriendVO;
import com.Mike12138210.SmartNote.vo.PendingApplyVO;
import com.Mike12138210.SmartNote.vo.UserSearchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            throw new RuntimeException("用户未登录，请重试");
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
            throw new RuntimeException("抱歉，您不能添加自己为好友");
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

    // 同意好友申请
    @Transactional
    public void approveApply(Long applyId,Long currentUsrId){
        FriendApply apply = friendApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new RuntimeException("对不起，该申请记录不存在");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }
        if (!apply.getToUserId().equals(currentUsrId)) {
            throw new RuntimeException("抱歉，您无权处理该申请");
        }

        User fromUser = userMapper.selectById(apply.getFromUserId());
        if(fromUser == null || fromUser.getDeleted() == 1){
            throw new RuntimeException("抱歉，申请人账号已注销，无法添加其为好友");
        }

        apply.setStatus(1);
        friendApplyMapper.updateById(apply);
        Friend friend1 = new Friend();
        friend1.setUserId(apply.getToUserId());
        friend1.setFriendId(apply.getFromUserId());
        Friend friend2 = new Friend();
        friend2.setUserId(apply.getFromUserId());
        friend2.setFriendId(apply.getToUserId());
        friendMapper.insert(friend1);
        friendMapper.insert(friend2);
    }

    // 拒绝好友申请
    @Transactional
    public void rejectApply(Long applyId,Long currentUsrId){
        FriendApply apply = friendApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new RuntimeException("对不起，该申请记录不存在");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }
        if (!apply.getToUserId().equals(currentUsrId)) {
            throw new RuntimeException("抱歉，您无权处理该申请");
        }

        apply.setStatus(2);
        friendApplyMapper.updateById(apply);
    }

    // 查看好友列表
    public Page<FriendVO> getFriendList(int pageNum, int pageSize, String groupName, Long userId){
        // 构造分页对象
        Page<Friend> page = new Page<>(pageNum,pageSize);
        // 查询条件：user_id = 当前用户ID，可选 group_name
        LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
        friendWrapper.eq(Friend::getUserId,userId);
        if(groupName != null && !groupName.isEmpty()){
            friendWrapper.eq(Friend::getGroupName,groupName);
        }

        // 分页查询 friend 表
        Page<Friend> friendPage = friendMapper.selectPage(page,friendWrapper);
        List<Friend> friends = friendPage.getRecords();
        if(friends.isEmpty()){
            return new Page<>(pageNum,pageSize,0);
        }

        // 收集好友ID列表
        List<Long> friendIds =  friends.stream()
                .map(Friend::getFriendId)
                .toList();

        // 根据ID列表批量查询用户信息
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().in(User::getId,friendIds)
        );

        // 将用户信息转为 Map，方便快速查找
        Map<Long,User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId,user -> user));

        // 组装 FriendVO 列表
        List<FriendVO> voList = new ArrayList<>();
        for(Friend friend : friends){
            User user = userMap.get(friend.getFriendId());
            if(user != null && user.getDeleted() == 0){
                FriendVO vo = new FriendVO();
                vo.setFriendId(user.getId());
                vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar());
                vo.setMotto(user.getMotto());
                vo.setGroupName(friend.getGroupName());
                voList.add(vo);
            }
        }

        // 创建新的分页对象返回
        Page<FriendVO> resultPage = new Page<>(friendPage.getCurrent(),friendPage.getSize(),friendPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
}