package com.example.flexdb.dto;

import java.time.ZonedDateTime;

public record ErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}

