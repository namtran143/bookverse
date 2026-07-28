package com.ptit.bookverse.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 150) String author,
        @NotBlank @Size(max = 30) String isbn,
        @NotNull @Min(0) @Max(2100) Integer year,
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
        @Size(max = 3000) String description
) {}
