package com.pgmanagement.service;

import com.pgmanagement.entity.Allocation;
import com.pgmanagement.entity.Room;
import com.pgmanagement.entity.Student;
import com.pgmanagement.repository.AllocationRepository;
import com.pgmanagement.repository.RoomRepository;
import com.pgmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;

    public AllocationService(
            AllocationRepository allocationRepository,
            StudentRepository studentRepository,
            RoomRepository roomRepository) {

        this.allocationRepository = allocationRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
    }

    public List<Allocation> getAll() {
        return allocationRepository.findAll();
    }

    public Allocation getById(Long id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));
    }

    public Allocation allocate(Long studentId, Long roomId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getOccupied() >= room.getCapacity()) {
            throw new RuntimeException("Room is full");
        }

        Allocation allocation = new Allocation();

        allocation.setStudent(student);
        allocation.setRoom(room);
        allocation.setAllocationDate(LocalDate.now());
        allocation.setStatus("ACTIVE");

        room.setOccupied(room.getOccupied() + 1);

        if (room.getOccupied() >= room.getCapacity()) {
            room.setStatus("FULL");
        } else {
            room.setStatus("AVAILABLE");
        }

        roomRepository.save(room);

        return allocationRepository.save(allocation);
    }

    public Allocation updateStatus(Long id, String status) {

        Allocation allocation = getById(id);

        allocation.setStatus(status);

        return allocationRepository.save(allocation);
    }

    public void delete(Long id) {

        Allocation allocation = getById(id);

        if ("ACTIVE".equalsIgnoreCase(allocation.getStatus())) {

            Room room = allocation.getRoom();

            if (room.getOccupied() > 0) {
                room.setOccupied(room.getOccupied() - 1);
            }

            if (room.getOccupied() < room.getCapacity()) {
                room.setStatus("AVAILABLE");
            }

            roomRepository.save(room);
        }

        allocationRepository.deleteById(id);
    }
}