package com.ptit.bookverse.service;

import com.ptit.bookverse.dto.BulkCoverResult;
import com.ptit.bookverse.entity.Book;
import com.ptit.bookverse.exception.BadRequestException;
import com.ptit.bookverse.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BulkCoverService {
    private final BookRepository repository;
    private final CoverImageService coverImageService;

    public BulkCoverService(BookRepository repository, CoverImageService coverImageService) {
        this.repository = repository;
        this.coverImageService = coverImageService;
    }

    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public BulkCoverResult upload(List<Long> bookIds, List<MultipartFile> covers) {
        if (bookIds == null || covers == null || bookIds.isEmpty()) {
            throw new BadRequestException("Phải gửi bookIds và covers");
        }
        if (bookIds.size() != covers.size()) {
            throw new BadRequestException("Số bookIds phải bằng số file ảnh");
        }

        List<String> errors = new ArrayList<>();
        int updated = 0;
        for (int i = 0; i < bookIds.size(); i++) {
            Long id = bookIds.get(i);
            try {
                Book book = repository.findById(id)
                        .orElseThrow(() -> new BadRequestException("Không tìm thấy sách id=" + id));
                coverImageService.deleteAll(book.getCoverPath());
                book.setCoverPath(coverImageService.save(id, covers.get(i)));
                repository.save(book);
                updated++;
            } catch (Exception ex) {
                errors.add("id=" + id + ": " + ex.getMessage());
            }
        }
        return new BulkCoverResult(bookIds.size(), updated, bookIds.size() - updated, errors);
    }
}
