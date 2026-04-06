package com.Mike12138210.SmartNote.mapper;

import com.Mike12138210.SmartNote.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface UserMapper extends BaseMapper<User> {
    // 不需要写任何方法！BaseMapper 提供了常用的 CRUD 方法：
    // insert, deleteById, updateById, selectById, selectList, selectPage 等
    // 如果需要复杂的多表查询，可以在这里定义方法，并编写对应的 XML 文件
}