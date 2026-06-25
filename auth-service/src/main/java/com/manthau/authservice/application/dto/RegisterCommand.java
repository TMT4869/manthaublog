package com.manthau.authservice.application.dto;

public record RegisterCommand(String username, String email, String rawPassword, String displayName) {}
