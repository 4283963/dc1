package com.jubensha.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPlayerRequest {

    @NotNull(message = "拼单ID不能为空")
    private Long orderId;

    private Integer addMale = 0;

    private Integer addFemale = 0;
}
