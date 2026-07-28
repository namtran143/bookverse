package com.ptit.bookverse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.bookverse.dto.BookRequest;
import com.ptit.bookverse.dto.BookResponse;
import com.ptit.bookverse.entity.Book;
import com.ptit.bookverse.exception.BadRequestException;
import com.ptit.bookverse.service.BookService;
import com.ptit.bookverse.service.CoverImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "CRUD, search and cover management")
public class BookController {
    private final BookService service;
    private final CoverImageService coverService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public BookController(BookService service, CoverImageService coverService,
                          ObjectMapper objectMapper, Validator validator) {
        this.service = service;
        this.coverService = coverService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping
    public Page<BookResponse> list(@RequestParam(defaultValue="0") int page,
                                   @RequestParam(defaultValue="12") int size,
                                   @RequestParam(defaultValue="title,asc") String sort,
                                   @RequestParam(required=false) String category,
                                   @RequestParam(required=false) Integer year) {
        return service.list(page, size, sort, category, year);
    }

    @GetMapping("/search")
    public Page<BookResponse> search(@RequestParam(required=false) String q,
                                     @RequestParam(required=false) String category,
                                     @RequestParam(defaultValue="0") int page,
                                     @RequestParam(defaultValue="12") int size,
                                     @RequestParam(defaultValue="title,asc") String sort) {
        return service.search(q, category, page, size, sort);
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) { return service.get(id); }

    @Operation(summary = "Create a book with an optional cover image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@RequestPart("book") String bookJson,
                               @RequestPart(value="cover", required=false) MultipartFile cover) {
        return service.create(parseAndValidate(bookJson), normalizeCover(cover));
    }

    @Operation(summary = "Update a book with an optional cover image")
    @PutMapping(value="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BookResponse update(@PathVariable Long id,
                               @RequestPart("book") String bookJson,
                               @RequestPart(value="cover", required=false) MultipartFile cover) {
        return service.update(id, parseAndValidate(bookJson), normalizeCover(cover));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    @GetMapping("/{id}/cover")
    public ResponseEntity<Resource> cover(@PathVariable Long id,
                                          @RequestParam(defaultValue="large") String size) {
        Book book = service.find(id);
        Resource resource = coverService.load(book.getCoverPath(), size);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(resource);
    }

    private BookRequest parseAndValidate(String json) {
        try {
            BookRequest request = objectMapper.readValue(json, BookRequest.class);
            Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String message = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                throw new BadRequestException(message);
            }
            return request;
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Phần book phải là JSON hợp lệ: " + ex.getOriginalMessage());
        }
    }

    private MultipartFile normalizeCover(MultipartFile cover) {
        return cover == null || cover.isEmpty() ? null : cover;
    }
}
