package com.ptit.bookverse.service;

import com.ptit.bookverse.dto.BookRequest;
import com.ptit.bookverse.dto.BookResponse;
import com.ptit.bookverse.entity.Book;
import com.ptit.bookverse.exception.BadRequestException;
import com.ptit.bookverse.exception.ResourceNotFoundException;
import com.ptit.bookverse.mapper.BookMapper;
import com.ptit.bookverse.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock BookRepository repository;
    @Mock BookMapper mapper;
    @Mock CoverImageService coverService;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(repository, mapper, coverService);
    }

    @Test
    void getReturnsMappedBook() {
        Book book = book(1L, "Clean Code", "9780132350884");
        BookResponse response = response(book);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(mapper.toResponse(book)).thenReturn(response);

        BookResponse actual = service.get(1L);

        assertEquals("Clean Code", actual.title());
        verify(repository).findById(1L);
        verify(mapper).toResponse(book);
    }

    @Test
    void getThrowsWhenBookDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.get(99L));
    }

    @Test
    void createRejectsDuplicateIsbn() {
        BookRequest request = request("Duplicate", "9780132350884");
        when(repository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.create(request, null));
        verify(repository, never()).save(any());
    }

    @Test
    void createSavesBookWithoutCover() {
        BookRequest request = request("Spring in Action", "9781617297571");
        Book entity = book(null, request.title(), request.isbn());
        Book saved = book(10L, request.title(), request.isbn());
        BookResponse response = response(saved);
        when(repository.existsByIsbn(request.isbn())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        BookResponse actual = service.create(request, null);

        assertEquals(10L, actual.id());
        verify(coverService, never()).save(anyLong(), any());
    }

    @Test
    void updateRejectsIsbnOwnedByAnotherBook() {
        Book existing = book(5L, "Old", "OLD-ISBN");
        BookRequest request = request("Updated", "NEW-ISBN");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByIsbnAndIdNot("NEW-ISBN", 5L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.update(5L, request, null));
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void deleteRemovesBookAndItsCovers() {
        Book existing = book(8L, "Delete Me", "DELETE-ISBN");
        existing.setCoverPath("2026/07/8-{size}.webp");
        when(repository.findById(8L)).thenReturn(Optional.of(existing));

        service.delete(8L);

        verify(coverService).deleteAll(existing.getCoverPath());
        verify(repository).delete(existing);
    }

    private BookRequest request(String title, String isbn) {
        return new BookRequest(title, "Author", isbn, 2024, "Programming",
                new BigDecimal("4.50"), "Description");
    }

    private Book book(Long id, String title, String isbn) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setYear(2024);
        book.setCategory("Programming");
        book.setRating(new BigDecimal("4.50"));
        book.setDescription("Description");
        return book;
    }

    private BookResponse response(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getYear(), book.getCategory(), book.getRating(), book.getDescription(), book.getCoverPath());
    }
}
