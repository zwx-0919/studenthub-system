package com.it.zwx.studenthub_system.pojo.dto.extend;
import java.time.LocalDateTime;
import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NoticePageDTO extends PageDTO {

    private Integer authorId; // 按发布人ID查
    private Integer status; // 按状态查
    private String titleKeyword; // 标题模糊查询
    private LocalDateTime publishTimeStart; // 发布时间起始
    private LocalDateTime publishTimeEnd; // 发布时间结束

}
