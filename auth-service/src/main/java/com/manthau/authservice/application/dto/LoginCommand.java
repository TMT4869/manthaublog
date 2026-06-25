package com.manthau.authservice.application.dto;

public record LoginCommand(String identifier, String rawPassword) {}
