package com.it.zwx.studenthub_system.pojo.dto.extend;

import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostPageDTO extends PageDTO {
    private String category;
    private String keyword;
    private Integer status; // 管理员用：0=已下架，1=已发布
}

