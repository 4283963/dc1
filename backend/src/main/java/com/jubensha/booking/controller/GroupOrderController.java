package com.jubensha.booking.controller;

import com.jubensha.booking.dto.AddPlayerRequest;
import com.jubensha.booking.dto.CreateGroupOrderRequest;
import com.jubensha.booking.dto.GroupOrderVO;
import com.jubensha.booking.dto.RoomStatusVO;
import com.jubensha.booking.service.GroupOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class GroupOrderController {

    private final GroupOrderService groupOrderService;

    @GetMapping("/active")
    public ResponseEntity<List<GroupOrderVO>> getActiveOrders() {
        return ResponseEntity.ok(groupOrderService.getActiveOrders());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<GroupOrderVO>> getPendingOrders() {
        return ResponseEntity.ok(groupOrderService.getPendingOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupOrderVO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(groupOrderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<GroupOrderVO> createOrder(@Valid @RequestBody CreateGroupOrderRequest request) {
        return ResponseEntity.ok(groupOrderService.createGroupOrder(request));
    }

    @PostMapping("/add-player")
    public ResponseEntity<GroupOrderVO> addPlayer(@Valid @RequestBody AddPlayerRequest request) {
        return ResponseEntity.ok(groupOrderService.addPlayer(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<GroupOrderVO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(groupOrderService.cancelOrder(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<GroupOrderVO> startOrder(@PathVariable Long id) {
        return ResponseEntity.ok(groupOrderService.startOrder(id));
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<GroupOrderVO> finishOrder(@PathVariable Long id) {
        return ResponseEntity.ok(groupOrderService.finishOrder(id));
    }

    @GetMapping("/rooms/status")
    public ResponseEntity<List<RoomStatusVO>> getRoomStatuses() {
        return ResponseEntity.ok(groupOrderService.getRoomStatuses());
    }
}
