package com.it.zwx.studenthub_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.zwx.studenthub_system.mapper.ClassInfoMapper;
import com.it.zwx.studenthub_system.pojo.entity.ClassInfo;
import com.it.zwx.studenthub_system.service.ClassInfoService;
import org.springframework.stereotype.Service;

@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {
}
