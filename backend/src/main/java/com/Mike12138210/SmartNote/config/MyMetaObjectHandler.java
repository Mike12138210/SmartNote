package com.Mike12138210.SmartNote.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject){
        // strictInsertFill 方法：如果实体类中该字段为空，则填充指定值
        this.strictInsertFill(metaObject,"createTime", LocalDateTime.class,LocalDateTime.now());
        this.strictInsertFill(metaObject,"updateTime", LocalDateTime.class,LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 强制覆盖,每次更新都把 updateTime 设为当前时间
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
    }
}
