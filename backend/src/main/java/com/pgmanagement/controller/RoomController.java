package com.pgmanagement.controller;

import com.pgmanagement.entity.Room;
import com.pgmanagement.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    public List<Room> getAll() {
        return service.getAll();
    }

    @GetMapping("/available")
    public List<Room> getAvailable() {
        return service.getAvailable();
    }

    @GetMapping("/{id}")
    public Room getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Room create(@RequestBody Room room) {
        return service.save(room);
    }

    @PutMapping("/{id}")
    public Room update(
            @PathVariable Long id,
            @RequestBody Room room) {

        return service.update(id, room);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Room deleted successfully";
    }
}