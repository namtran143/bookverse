package com.ptit.bookverse.config;

import com.ptit.bookverse.entity.Book;
import com.ptit.bookverse.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import java.math.BigDecimal;

@Configuration
public class SeedDataConfig {
    @Bean CommandLineRunner seed(BookRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            repository.save(book("Clean Code", "Robert C. Martin", "9780132350884", 2008, "Programming", "4.70"));
            repository.save(book("Effective Java", "Joshua Bloch", "9780134685991", 2018, "Programming", "4.80"));
            repository.save(book("The Pragmatic Programmer", "David Thomas", "9780135957059", 2019, "Programming", "4.75"));
        };
    }
    private Book book(String title, String author, String isbn, int year, String category, String rating) {
        Book b = new Book(); b.setTitle(title); b.setAuthor(author); b.setIsbn(isbn); b.setYear(year);
        b.setCategory(category); b.setRating(new BigDecimal(rating)); b.setDescription("Sample data for BookVerse"); return b;
    }
}
