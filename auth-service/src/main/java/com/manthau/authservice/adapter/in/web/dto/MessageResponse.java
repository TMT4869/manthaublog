package com.manthau.authservice.adapter.in.web.dto;

public record MessageResponse(String message) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
