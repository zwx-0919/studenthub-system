package com.it.zwx.studenthub_system.pojo.dto;

import lombok.Data;

@Data
public class InteractionDTO {
    private Integer userId;
    private Integer productId;
    private Integer type;
}
