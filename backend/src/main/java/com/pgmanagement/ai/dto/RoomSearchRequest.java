package com.pgmanagement.ai.dto;

public class RoomSearchRequest {

    private String query; // e.g. "Find double sharing under 7000 with wifi"

    public RoomSearchRequest() {
    }

    public RoomSearchRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
