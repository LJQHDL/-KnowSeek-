package com.example.copilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.copilot.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
