package com.ptit.bookverse.dto;

import java.util.List;

public record ImportResult(int totalRows, int imported, int skipped, List<String> errors) {}
