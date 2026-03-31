package com.it.zwx.studenthub_system.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private Integer messageType; // 1系统提醒 2私信 3互动提醒
    private String content;
    private Integer isRead; // 0未读 1已读
    private LocalDateTime sendTime;
    private Integer bizId;
    @TableField(exist = false)
    private String senderName;
}

