package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.mapper.MajorInfoMapper;
import com.it.zwx.studenthub_system.pojo.entity.MajorInfo;
import com.it.zwx.studenthub_system.service.MajorInfoService;
import org.springframework.stereotype.Service;

@Service
public class MajorInfoServiceImpl extends ServiceImpl<MajorInfoMapper, MajorInfo> implements MajorInfoService {
}
