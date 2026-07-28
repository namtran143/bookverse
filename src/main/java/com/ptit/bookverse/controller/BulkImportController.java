package com.ptit.bookverse.controller;

import com.ptit.bookverse.dto.ImportResult;
import com.ptit.bookverse.service.BulkImportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books/import")
@Tag(name = "Bulk Import", description = "Import books from CSV or Excel")
public class BulkImportController {
    private final BulkImportService service;
    public BulkImportController(BulkImportService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importBooks(@RequestPart("file") MultipartFile file) {
        return service.importFile(file);
    }
}
