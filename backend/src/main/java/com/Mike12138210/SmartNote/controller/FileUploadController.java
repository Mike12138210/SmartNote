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
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        try{
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            File dir = new File(projectRoot,"uploads");
            if(!dir.exists()){
                dir.mkdirs(); // 创建目录（如果不存在）
            }

            // 提取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if(originalFilename != null && originalFilename.contains(".")){
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID() + ext;
            File dest = new File(dir,newFilename);
            file.transferTo(dest); // 保存文件

            // 返回访问URL
            String url = "/uploads/" + newFilename;
            return Result.success(url);
        }catch (IOException e){
            e.printStackTrace();
            return Result.error("文件保存失败：" + e.getMessage());
        }
    }
}