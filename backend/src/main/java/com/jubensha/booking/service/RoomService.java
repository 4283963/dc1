package com.jubensha.booking.service;

import com.jubensha.booking.entity.Room;
import com.jubensha.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("房间不存在: " + id));
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room room) {
        Room existing = getRoomById(id);
        existing.setName(room.getName());
        existing.setCapacity(room.getCapacity());
        existing.setDescription(room.getDescription());
        return roomRepository.save(existing);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}
