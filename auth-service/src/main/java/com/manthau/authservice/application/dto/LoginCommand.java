package com.manthau.authservice.application.dto;

public record LoginCommand(String email, String rawPassword) {}
