package com.university.booking.dto;

import lombok.Data;

@Data
public class RoomDto {
    private String id;
    private String name;
    private String type;
    private int capacity;
    private String location;
}
