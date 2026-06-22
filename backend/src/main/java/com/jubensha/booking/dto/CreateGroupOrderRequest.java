package com.jubensha.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateGroupOrderRequest {

    @NotNull(message = "剧本ID不能为空")
    private Long scriptId;

    @NotNull(message = "开场时间不能为空")
    private LocalDateTime startTime;

    private Integer initialMale = 0;

    private Integer initialFemale = 0;

    private String remark;
}
