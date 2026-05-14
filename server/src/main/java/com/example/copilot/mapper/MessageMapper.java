package com.example.copilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.copilot.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
