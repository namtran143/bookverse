package com.ptit.bookverse.controller;

import com.ptit.bookverse.dto.BulkCoverResult;
import com.ptit.bookverse.service.BulkCoverService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books/covers")
@Tag(name = "Bulk Covers", description = "Upload multiple book covers in one request")
public class BulkCoverController {
    private final BulkCoverService service;

    public BulkCoverController(BulkCoverService service) {
        this.service = service;
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BulkCoverResult upload(
            @RequestParam("bookIds") List<Long> bookIds,
            @RequestPart("covers") List<MultipartFile> covers
    ) {
        return service.upload(bookIds, covers);
    }
}
