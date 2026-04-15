package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.entity.User;
import com.Mike12138210.SmartNote.mapper.UserMapper;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    // 从 application.yml 中读取配置的上传目录
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private UserMapper userMapper;

    // 上传头像
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        Long userId = ThreadLocalUtil.get();
        if (userId == null) {
            return Result.error("未登录");
        }

        User user = userMapper.selectById(userId);
        String oldAvatarUrl = user.getAvatar();

        try {
            // 1. 保存新文件
            String projectRoot = System.getProperty("user.dir");
            File dir = new File(projectRoot, "uploads");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID() + ext;
            File dest = new File(dir, newFilename);
            file.transferTo(dest);
            String newAvatarUrl = "/uploads/" + newFilename;

            // 2. 更新数据库中的头像 URL
            user.setAvatar(newAvatarUrl);
            userMapper.updateById(user);

            // 3. 删除旧头像文件（如果存在且不是新文件）
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                String oldFileName = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
                File oldFile = new File(projectRoot, "uploads/" + oldFileName);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            return Result.success(newAvatarUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败：" + e.getMessage());
        }
    }
}