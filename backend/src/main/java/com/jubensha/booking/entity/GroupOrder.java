package com.jubensha.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_order")
public class GroupOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "script_id", nullable = false)
    private Long scriptId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer currentMale;

    @Column(nullable = false)
    private Integer currentFemale;

    @Column(nullable = false)
    private Integer totalPlayers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (currentMale == null) {
            currentMale = 0;
        }
        if (currentFemale == null) {
            currentFemale = 0;
        }
        totalPlayers = currentMale + currentFemale;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        totalPlayers = (currentMale != null ? currentMale : 0)
                     + (currentFemale != null ? currentFemale : 0);
    }

    public Integer getCurrentTotal() {
        return (currentMale != null ? currentMale : 0) + (currentFemale != null ? currentFemale : 0);
    }
}
