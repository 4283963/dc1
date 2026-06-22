package com.jubensha.booking.dto;

import com.jubensha.booking.entity.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupOrderVO {

    private Long id;
    private Long scriptId;
    private String scriptName;
    private Long roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer currentMale;
    private Integer currentFemale;
    private Integer totalPlayers;
    private Integer needMale;
    private Integer needFemale;
    private Integer needTotal;
    private OrderStatus status;
    private String remark;
    private Integer duration;
    private Integer price;
    private LocalDateTime createdAt;
}
