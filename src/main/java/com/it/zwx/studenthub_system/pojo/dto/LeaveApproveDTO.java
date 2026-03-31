package com.it.zwx.studenthub_system.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveApproveDTO {
    @NotNull(message = "审批状态不能为空")
    private Integer approveStatus; // 1通过 2驳回
    private String approveRemark;
}

