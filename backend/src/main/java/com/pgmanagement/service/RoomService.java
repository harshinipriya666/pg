package com.pgmanagement.service;

import com.pgmanagement.entity.Room;
import com.pgmanagement.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public List<Room> getAll() {
        return repository.findAll();
    }

    public List<Room> getAvailable() {
        return repository.findByStatus("AVAILABLE");
    }

    public Room getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public Room save(Room room) {

        room.setOccupied(0);

        if (room.getCapacity() > 0) {
            room.setStatus("AVAILABLE");
        }

        if (room.getRent() == null) {
            room.setRent(room.getRent());
        }
        if (room.getRoomType() == null) {
            room.setRoomType(room.getRoomType());
        }
        if (room.getFacilities() == null) {
            room.setFacilities(room.getFacilities());
        }

        return repository.save(room);
    }

    public Room update(Long id, Room data) {

        Room room = getById(id);

        room.setRoomNumber(data.getRoomNumber());
        room.setFloor(data.getFloor());
        room.setCapacity(data.getCapacity());

        if (data.getRent() != null) {
            room.setRent(data.getRent());
        }
        if (data.getRoomType() != null) {
            room.setRoomType(data.getRoomType());
        }
        if (data.getFacilities() != null) {
            room.setFacilities(data.getFacilities());
        }

        updateStatus(room);

        return repository.save(room);
    }

    public void updateStatus(Room room) {

        if (room.getOccupied() >= room.getCapacity()) {
            room.setStatus("FULL");
        } else {
            room.setStatus("AVAILABLE");
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}