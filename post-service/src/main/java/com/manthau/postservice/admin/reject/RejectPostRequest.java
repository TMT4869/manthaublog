package com.manthau.postservice.admin.reject;

import jakarta.validation.constraints.NotBlank;

public record RejectPostRequest(@NotBlank String reason) {}
