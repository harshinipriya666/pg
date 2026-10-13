package com.pgmanagement.entity;

import jakarta.persistence.*;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomNumber;

    private int floor;
    private int capacity;
    private int occupied = 0;
    private String status = "AVAILABLE";

    private Double rent;
    private String roomType;
    private String facilities;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupied() {
        return occupied;
    }

    public void setOccupied(int occupied) {
        this.occupied = occupied;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getRent() {
        if (rent != null && rent > 0) {
            return rent;
        }
        if (capacity == 1) return 8500.0;
        if (capacity == 2) return 6500.0;
        if (capacity == 3) return 5500.0;
        return 4500.0;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public String getRoomType() {
        if (roomType != null && !roomType.isBlank()) {
            return roomType;
        }
        if (capacity == 1) return "Single Sharing";
        if (capacity == 2) return "Double Sharing";
        if (capacity == 3) return "Triple Sharing";
        return capacity + "-Sharing";
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getFacilities() {
        if (facilities != null && !facilities.isBlank()) {
            return facilities;
        }
        return "High-speed Wi-Fi, Geyser, Cupboard, Study Desk, Attached Bathroom, Daily Housekeeping";
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }
}