package com.it.zwx.studenthub_system.pojo.dto.extend;

import com.it.zwx.studenthub_system.pojo.dto.base.PageDTO;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class LeaveApplyPageDTO extends PageDTO {
    private Integer studentId;
    private Integer counselorId;
    private String classNo;
    private Integer classId;
    private Integer approveStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
}

