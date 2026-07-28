package com.ptit.bookverse.service;

import com.ptit.bookverse.dto.*;
import com.ptit.bookverse.entity.Book;
import com.ptit.bookverse.exception.*;
import com.ptit.bookverse.mapper.BookMapper;
import com.ptit.bookverse.repository.BookRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository repository;
    private final BookMapper mapper;
    private final CoverImageService coverService;

    public BookService(BookRepository repository, BookMapper mapper, CoverImageService coverService) {
        this.repository = repository; this.mapper = mapper; this.coverService = coverService;
    }

    public Page<BookResponse> list(int page, int size, String sort, String category, Integer year) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isBlank()) predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            if (year != null) predicates.add(cb.equal(root.get("year"), year));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Cacheable(cacheNames = "books", key = "#id")
    public BookResponse get(Long id) { return mapper.toResponse(find(id)); }

    public Page<BookResponse> search(String q, String category, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like), cb.like(cb.lower(root.get("author")), like)));
            }
            if (category != null && !category.isBlank()) p.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            return cb.and(p.toArray(Predicate[]::new));
        };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional @CacheEvict(cacheNames = "books", allEntries = true)
    public BookResponse create(BookRequest request, MultipartFile cover) {
        if (repository.existsByIsbn(request.isbn())) throw new BadRequestException("ISBN đã tồn tại");
        Book book = repository.save(mapper.toEntity(request));
        if (cover != null && !cover.isEmpty()) {
            book.setCoverPath(coverService.save(book.getId(), cover));
            repository.save(book);
        }
        return mapper.toResponse(book);
    }

    @Transactional @CacheEvict(cacheNames = "books", allEntries = true)
    public BookResponse update(Long id, BookRequest request, MultipartFile cover) {
        Book book = find(id);
        if (repository.existsByIsbnAndIdNot(request.isbn(), id)) throw new BadRequestException("ISBN đã tồn tại");
        mapper.update(request, book);
        if (cover != null && !cover.isEmpty()) {
            coverService.deleteAll(book.getCoverPath());
            book.setCoverPath(coverService.save(id, cover));
        }
        return mapper.toResponse(repository.save(book));
    }

    @Transactional @CacheEvict(cacheNames = "books", allEntries = true)
    public void delete(Long id) {
        Book book = find(id);
        coverService.deleteAll(book.getCoverPath());
        repository.delete(book);
    }

    public Book find(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách id=" + id)); }

    private Sort parseSort(String raw) {
        String value = raw == null || raw.isBlank() ? "title,asc" : raw;
        String[] parts = value.split(",");
        String field = parts[0];
        if (!List.of("title", "year", "rating").contains(field)) throw new BadRequestException("Chỉ được sort theo title, year hoặc rating");
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
