package com.it.zwx.studenthub_system.pojo.dto.extend;

import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MessagePageDTO extends PageDTO {
    private Integer messageType;
    private Integer isRead;
}

