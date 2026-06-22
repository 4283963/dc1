package com.jubensha.booking.service;

import com.jubensha.booking.dto.AddPlayerRequest;
import com.jubensha.booking.dto.CreateGroupOrderRequest;
import com.jubensha.booking.dto.GroupOrderVO;
import com.jubensha.booking.dto.RoomStatusVO;
import com.jubensha.booking.dto.UpdatePlayerCountRequest;
import com.jubensha.booking.entity.GroupOrder;
import com.jubensha.booking.entity.OrderStatus;
import com.jubensha.booking.entity.Room;
import com.jubensha.booking.entity.Script;
import com.jubensha.booking.repository.GroupOrderRepository;
import com.jubensha.booking.repository.RoomRepository;
import com.jubensha.booking.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupOrderService {

    private final GroupOrderRepository groupOrderRepository;
    private final ScriptRepository scriptRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public GroupOrderVO createGroupOrder(CreateGroupOrderRequest request) {
        Script script = scriptRepository.findById(request.getScriptId())
                .orElseThrow(() -> new RuntimeException("剧本不存在"));

        GroupOrder order = new GroupOrder();
        order.setScriptId(request.getScriptId());
        order.setStartTime(request.getStartTime());

        int duration = script.getDuration() != null ? script.getDuration() : 180;
        order.setEndTime(request.getStartTime().plusMinutes(duration));

        int male = request.getInitialMale() != null ? request.getInitialMale() : 0;
        int female = request.getInitialFemale() != null ? request.getInitialFemale() : 0;
        order.setCurrentMale(male);
        order.setCurrentFemale(female);
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(request.getRemark());

        GroupOrder saved = groupOrderRepository.save(order);

        Script freshScript = scriptRepository.findById(saved.getScriptId()).orElse(null);
        if (isOrderFull(saved, freshScript)) {
            Room room = allocateRoomWithLock(saved, freshScript);
            if (room != null) {
                saved.setRoomId(room.getId());
                saved.setStatus(OrderStatus.CONFIRMED);
                saved = groupOrderRepository.save(saved);
            }
        }

        Room room = saved.getRoomId() != null ? roomRepository.findById(saved.getRoomId()).orElse(null) : null;
        return convertToVO(saved, freshScript, room);
    }

    @Transactional
    public GroupOrderVO addPlayer(AddPlayerRequest request) {
        GroupOrder order = groupOrderRepository.findByIdForUpdate(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("拼单不存在"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("当前拼单状态不允许添加玩家");
        }

        int addMale = request.getAddMale() != null ? request.getAddMale() : 0;
        int addFemale = request.getAddFemale() != null ? request.getAddFemale() : 0;

        order.setCurrentMale(order.getCurrentMale() + addMale);
        order.setCurrentFemale(order.getCurrentFemale() + addFemale);

        Script script = scriptRepository.findById(order.getScriptId()).orElse(null);

        if (isOrderFull(order, script)) {
            Room room = allocateRoomWithLock(order, script);
            if (room != null) {
                order.setRoomId(room.getId());
                order.setStatus(OrderStatus.CONFIRMED);
            }
        }

        GroupOrder saved = groupOrderRepository.save(order);

        Room room = saved.getRoomId() != null ? roomRepository.findById(saved.getRoomId()).orElse(null) : null;
        return convertToVO(saved, script, room);
    }

    @Transactional
    public GroupOrderVO updatePlayerCount(UpdatePlayerCountRequest request) {
        GroupOrder order = groupOrderRepository.findByIdForUpdate(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("拼单不存在"));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("当前拼单状态不允许修改人数");
        }

        int maleDelta = request.getMaleDelta() != null ? request.getMaleDelta() : 0;
        int femaleDelta = request.getFemaleDelta() != null ? request.getFemaleDelta() : 0;

        int newMale = order.getCurrentMale() + maleDelta;
        int newFemale = order.getCurrentFemale() + femaleDelta;

        if (newMale < 0 || newFemale < 0) {
            throw new RuntimeException("人数不能为负数");
        }

        order.setCurrentMale(newMale);
        order.setCurrentFemale(newFemale);

        Script script = scriptRepository.findById(order.getScriptId()).orElse(null);

        boolean wasConfirmedBefore = order.getStatus() == OrderStatus.CONFIRMED;
        boolean isFullNow = isOrderFull(order, script);

        if (wasConfirmedBefore && !isFullNow) {
            order.setRoomId(null);
            order.setStatus(OrderStatus.PENDING);
        } else if (!wasConfirmedBefore && isFullNow) {
            Room room = allocateRoomWithLock(order, script);
            if (room != null) {
                order.setRoomId(room.getId());
                order.setStatus(OrderStatus.CONFIRMED);
            }
        }

        GroupOrder saved = groupOrderRepository.save(order);

        Room room = saved.getRoomId() != null ? roomRepository.findById(saved.getRoomId()).orElse(null) : null;
        return convertToVO(saved, script, room);
    }

    private Room allocateRoomWithLock(GroupOrder order, Script script) {
        if (script == null) {
            return null;
        }

        int requiredCapacity = script.getTotalPlayers();

        List<Room> suitableRooms = roomRepository.findAll().stream()
                .filter(room -> room.getCapacity() >= requiredCapacity)
                .sorted(Comparator.comparingInt(Room::getCapacity))
                .toList();

        if (suitableRooms.isEmpty()) {
            return null;
        }

        for (Room candidate : suitableRooms) {
            Room lockedRoom = roomRepository.findByIdForUpdate(candidate.getId()).orElse(null);
            if (lockedRoom == null) {
                continue;
            }

            List<GroupOrder> conflicts = groupOrderRepository.findConflictingOrdersLocked(
                    lockedRoom.getId(),
                    order.getStartTime(),
                    order.getEndTime());

            if (conflicts.isEmpty()) {
                return lockedRoom;
            }
        }

        return null;
    }

    private boolean isOrderFull(GroupOrder order, Script script) {
        if (script == null) {
            return false;
        }

        int currentTotal = order.getCurrentTotal();
        int requiredTotal = script.getTotalPlayers();

        if (currentTotal < requiredTotal) {
            return false;
        }

        if (script.getMinMale() != null && order.getCurrentMale() < script.getMinMale()) {
            return false;
        }

        if (script.getMinFemale() != null && order.getCurrentFemale() < script.getMinFemale()) {
            return false;
        }

        return true;
    }

    public List<GroupOrderVO> getActiveOrders() {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.IN_PROGRESS
        );

        List<GroupOrder> orders = groupOrderRepository.findByStatusInOrderByStartTimeAsc(activeStatuses);

        List<Long> scriptIds = orders.stream().map(GroupOrder::getScriptId).distinct().toList();
        List<Long> roomIds = orders.stream().map(GroupOrder::getRoomId).filter(id -> id != null).distinct().toList();

        Map<Long, Script> scriptMap = scriptRepository.findAllById(scriptIds).stream()
                .collect(Collectors.toMap(Script::getId, s -> s));
        Map<Long, Room> roomMap = roomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(Room::getId, r -> r));

        return orders.stream()
                .map(order -> convertToVO(
                        order,
                        scriptMap.get(order.getScriptId()),
                        roomMap.get(order.getRoomId())))
                .collect(Collectors.toList());
    }

    public List<GroupOrderVO> getPendingOrders() {
        List<GroupOrder> orders = groupOrderRepository.findByStatusOrderByStartTimeAsc(OrderStatus.PENDING);

        List<Long> scriptIds = orders.stream().map(GroupOrder::getScriptId).distinct().toList();
        Map<Long, Script> scriptMap = scriptRepository.findAllById(scriptIds).stream()
                .collect(Collectors.toMap(Script::getId, s -> s));

        return orders.stream()
                .map(order -> convertToVO(order, scriptMap.get(order.getScriptId()), null))
                .collect(Collectors.toList());
    }

    public List<RoomStatusVO> getRoomStatuses() {
        List<Room> rooms = roomRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        List<GroupOrder> activeOrders = groupOrderRepository.findActiveOrdersInTimeRange(now, endOfDay);

        Map<Long, List<GroupOrder>> roomOrdersMap = activeOrders.stream()
                .filter(o -> o.getRoomId() != null)
                .collect(Collectors.groupingBy(GroupOrder::getRoomId));

        List<Long> scriptIds = activeOrders.stream().map(GroupOrder::getScriptId).distinct().toList();
        Map<Long, Script> scriptMap = scriptRepository.findAllById(scriptIds).stream()
                .collect(Collectors.toMap(Script::getId, s -> s));

        List<RoomStatusVO> result = new ArrayList<>();
        for (Room room : rooms) {
            RoomStatusVO vo = new RoomStatusVO();
            vo.setId(room.getId());
            vo.setName(room.getName());
            vo.setCapacity(room.getCapacity());

            List<GroupOrder> roomOrders = roomOrdersMap.getOrDefault(room.getId(), List.of());

            GroupOrder currentOrder = null;
            GroupOrder nextOrder = null;
            for (GroupOrder o : roomOrders) {
                if (!o.getStartTime().isAfter(now) && !o.getEndTime().isBefore(now)) {
                    currentOrder = o;
                } else if (o.getStartTime().isAfter(now)) {
                    if (nextOrder == null || o.getStartTime().isBefore(nextOrder.getStartTime())) {
                        nextOrder = o;
                    }
                }
            }

            if (currentOrder != null) {
                vo.setStatus("使用中");
                Script script = scriptMap.get(currentOrder.getScriptId());
                vo.setCurrentOrderScript(script != null ? script.getName() : "");
                vo.setCurrentOrderTime(formatTime(currentOrder.getStartTime()) + " - " + formatTime(currentOrder.getEndTime()));
            } else if (nextOrder != null) {
                vo.setStatus("待使用");
                Script script = scriptMap.get(nextOrder.getScriptId());
                vo.setCurrentOrderScript(script != null ? script.getName() : "");
                vo.setCurrentOrderTime(formatTime(nextOrder.getStartTime()) + " 开场");
            } else {
                vo.setStatus("空闲");
                vo.setCurrentOrderScript("");
                vo.setCurrentOrderTime("");
            }

            result.add(vo);
        }

        return result;
    }

    private String formatTime(LocalDateTime time) {
        return time.getHour() + ":" + String.format("%02d", time.getMinute());
    }

    private GroupOrderVO convertToVO(GroupOrder order, Script script, Room room) {
        GroupOrderVO vo = new GroupOrderVO();
        vo.setId(order.getId());
        vo.setScriptId(order.getScriptId());
        vo.setRoomId(order.getRoomId());
        vo.setStartTime(order.getStartTime());
        vo.setEndTime(order.getEndTime());
        vo.setCurrentMale(order.getCurrentMale());
        vo.setCurrentFemale(order.getCurrentFemale());
        vo.setTotalPlayers(order.getTotalPlayers());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());

        if (script != null) {
            vo.setScriptName(script.getName());
            vo.setDuration(script.getDuration());
            vo.setPrice(script.getPrice());

            int needTotal = Math.max(0, script.getTotalPlayers() - order.getCurrentTotal());
            int needMale = script.getMinMale() != null ?
                    Math.max(0, script.getMinMale() - order.getCurrentMale()) : 0;
            int needFemale = script.getMinFemale() != null ?
                    Math.max(0, script.getMinFemale() - order.getCurrentFemale()) : 0;

            vo.setNeedTotal(needTotal);
            vo.setNeedMale(needMale);
            vo.setNeedFemale(needFemale);
        }

        if (room != null) {
            vo.setRoomName(room.getName());
        }

        return vo;
    }

    @Transactional
    public GroupOrderVO cancelOrder(Long id) {
        GroupOrder order = groupOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("拼单不存在"));
        order.setStatus(OrderStatus.CANCELLED);
        GroupOrder saved = groupOrderRepository.save(order);
        Script script = scriptRepository.findById(order.getScriptId()).orElse(null);
        Room room = order.getRoomId() != null ? roomRepository.findById(order.getRoomId()).orElse(null) : null;
        return convertToVO(saved, script, room);
    }

    @Transactional
    public GroupOrderVO startOrder(Long id) {
        GroupOrder order = groupOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("拼单不存在"));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("只有已成团的订单才能开始");
        }
        order.setStatus(OrderStatus.IN_PROGRESS);
        GroupOrder saved = groupOrderRepository.save(order);
        Script script = scriptRepository.findById(order.getScriptId()).orElse(null);
        Room room = order.getRoomId() != null ? roomRepository.findById(order.getRoomId()).orElse(null) : null;
        return convertToVO(saved, script, room);
    }

    @Transactional
    public GroupOrderVO finishOrder(Long id) {
        GroupOrder order = groupOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("拼单不存在"));
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new RuntimeException("只有进行中的订单才能结束");
        }
        order.setStatus(OrderStatus.FINISHED);
        GroupOrder saved = groupOrderRepository.save(order);
        Script script = scriptRepository.findById(order.getScriptId()).orElse(null);
        Room room = order.getRoomId() != null ? roomRepository.findById(order.getRoomId()).orElse(null) : null;
        return convertToVO(saved, script, room);
    }

    public GroupOrderVO getOrderById(Long id) {
        GroupOrder order = groupOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("拼单不存在"));
        Script script = scriptRepository.findById(order.getScriptId()).orElse(null);
        Room room = order.getRoomId() != null ? roomRepository.findById(order.getRoomId()).orElse(null) : null;
        return convertToVO(order, script, room);
    }
}
