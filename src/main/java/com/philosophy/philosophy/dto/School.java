package com.philosophy.philosophy.dto;

import java.util.UUID;

public record School(UUID id, String initial, String name, String period, String description, String color) {
}
