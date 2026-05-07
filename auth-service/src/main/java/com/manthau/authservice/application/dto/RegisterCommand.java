package com.manthau.authservice.application.dto;

public record RegisterCommand(String email, String rawPassword, String displayName) {}
