package com.jubensha.booking.dto;

import lombok.Data;

@Data
public class RoomStatusVO {

    private Long id;
    private String name;
    private Integer capacity;
    private String status;
    private String currentOrderScript;
    private String currentOrderTime;
}
