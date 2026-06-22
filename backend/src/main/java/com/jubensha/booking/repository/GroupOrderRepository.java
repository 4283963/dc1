package com.jubensha.booking.repository;

import com.jubensha.booking.entity.GroupOrder;
import com.jubensha.booking.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long> {

    List<GroupOrder> findByStatusInOrderByStartTimeAsc(List<OrderStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM GroupOrder o WHERE o.id = :id")
    Optional<GroupOrder> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT o FROM GroupOrder o WHERE o.roomId = :roomId " +
           "AND o.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "AND o.startTime < :endTime AND o.endTime > :startTime")
    List<GroupOrder> findConflictingOrders(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT o FROM GroupOrder o WHERE o.roomId = :roomId " +
           "AND o.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "AND o.startTime < :endTime AND o.endTime > :startTime")
    List<GroupOrder> findConflictingOrdersLocked(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT o FROM GroupOrder o WHERE o.status IN ('CONFIRMED', 'IN_PROGRESS') " +
           "AND o.startTime < :endTime AND o.endTime > :startTime")
    List<GroupOrder> findActiveOrdersInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<GroupOrder> findByStatusOrderByStartTimeAsc(OrderStatus status);
}
