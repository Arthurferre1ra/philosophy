package com.philosophy.philosophy.dto;

import java.util.UUID;

public record Period(UUID id, String number, String name, String description, String range, String color) {
}
