package com.ptit.bookverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BookVerseApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookVerseApplication.class, args);
    }
}
