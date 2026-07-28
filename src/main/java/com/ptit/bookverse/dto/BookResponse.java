package com.ptit.bookverse.dto;

import java.math.BigDecimal;

public record BookResponse(Long id, String title, String author, String isbn, Integer year,
                           String category, BigDecimal rating, String description, String coverPath) {}
