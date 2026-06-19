package ru.memearena.error;
import java.time.Instant;
import java.util.List;
public record ApiError(String code, String message, Instant timestamp, String path, List<FieldErrorResponse> fieldErrors) {}
