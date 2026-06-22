package com.jubensha.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePlayerCountRequest {

    @NotNull(message = "拼单ID不能为空")
    private Long orderId;

    private Integer maleDelta = 0;

    private Integer femaleDelta = 0;
}
