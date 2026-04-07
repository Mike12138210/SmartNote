package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.dto.LoginRequest;
import com.Mike12138210.SmartNote.dto.ProfileUpdateRequest;
import com.Mike12138210.SmartNote.dto.UserInfoResponse;
import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.mapper.UserMapper;
import com.Mike12138210.SmartNote.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 注册新用户
    public void register(User user){
        checkUnique(user.getUsername(),user.getEmail(),user.getPhone());

        String rawPassword = user.getPassword(); // 用户输入明文密码
        String encodedPassword  = passwordEncoder.encode(rawPassword); // 加密
        user.setPassword(encodedPassword); // 存密文到数据库

        if(user.getNickname() == null || user.getNickname().isEmpty()){
            user.setNickname(user.getUsername());
        }
        userMapper.insert(user);
    }

    // 唯一性检查
    private void checkUnique(String username,String email,String phone){
        LambdaQueryWrapper<User>wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,username)
                .or()
                .eq(User::getEmail,email)
                .or()
                .eq(User::getPhone,phone);

        // 执行查询，如果存在一条记录，count > 0
        Long count = userMapper.selectCount(wrapper);

        if(count > 0){
            if (isUsernameExist(username)) {
                throw new RuntimeException("该用户名已存在，请重试");
            }
            if (isEmailExist(email)) {
                throw new RuntimeException("该邮箱已存在，请重试");
            }
            if (isPhoneExist(phone)) {
                throw new RuntimeException("该手机号已存在，请重试");
            }
        }
    }

    // 判断用户名是否存在
    private boolean isUsernameExist(String username){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,username);
        return userMapper.selectCount(wrapper) > 0;
    }

    // 判断邮箱是否存在
    private boolean isEmailExist(String email){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail,email);
        return userMapper.selectCount(wrapper) > 0;
    }

    // 判断手机号是否存在
    private boolean isPhoneExist(String phone){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone,phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    // 登录
    public String login(LoginRequest request){
        String account = request.getAccount();
        String password = request.getPassword(); // 用户输入的密码明文

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail,account)
                .or()
                .eq(User::getPhone,account);
        User user =userMapper.selectOne(wrapper);

        if(user == null){
            throw new RuntimeException("账号不存在，请稍后重试。");
        }

        String storedHash = user.getPassword(); // 从数据库查出用户的密文 storedHash
        boolean isMatch = passwordEncoder.matches(password,storedHash); // 验证：明文 + 密文 -> true/false
        if(!isMatch){
            throw new RuntimeException("密码错误，请稍后重试。");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        return jwtUtil.genToken(claims);
    }

    // 获取当前用户信息（不含密码）
    public UserInfoResponse getUserInfo(Long userId){
        User user = userMapper.selectById(userId);
        if(user == null){throw new RuntimeException("该用户不存在，请稍后重试");}
        return new UserInfoResponse(user);
    }

    // 修改个人资料
    public void profileUpdate(Long userId, ProfileUpdateRequest request){
        User user = new User();
        user.setId(userId);
        if(request.getNickname() != null){user.setNickname(request.getNickname());}
        if(request.getAvatar() != null){user.setAvatar(request.getAvatar());}
        if(request.getMotto() != null){user.setMotto(request.getMotto());}
        userMapper.updateById(user);
    }
}