package com.it.zwx.studenthub_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.zwx.studenthub_system.pojo.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}

