package com.ptit.bookverse.dto;

import java.util.List;

public record BulkCoverResult(int total, int updated, int failed, List<String> errors) {
}
