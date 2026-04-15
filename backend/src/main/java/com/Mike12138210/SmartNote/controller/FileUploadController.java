package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.utils.Result;
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

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 1. 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 2. 获取原始文件名，提取扩展名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 3. 生成唯一文件名（避免重名覆盖）
        String newFileName = UUID.randomUUID().toString() + ext;

        // 4. 创建保存目录（如果不存在）
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 5. 保存文件
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败");
        }

        // 6. 返回可访问的 URL（前端通过 /uploads/ 访问）
        String url = "/uploads/" + newFileName;
        return Result.success(url);
    }
}