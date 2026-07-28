package com.ptit.bookverse.service;

import com.ptit.bookverse.dto.ImportResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkImportServiceTest {
    @Mock BookService bookService;

    @Test
    void importsValidCsvRows() {
        String csv = "title,author,isbn,year,category,rating,description\n" +
                "Refactoring,Martin Fowler,9780201485677,1999,Programming,4.7,Design improvement\n" +
                "Domain-Driven Design,Eric Evans,9780321125217,2003,Architecture,4.6,Domain modeling\n";
        MockMultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", csv.getBytes());
        BulkImportService service = new BulkImportService(bookService);

        ImportResult result = service.importFile(file);

        assertEquals(2, result.totalRows());
        assertEquals(2, result.imported());
        assertEquals(0, result.skipped());
        verify(bookService, times(2)).create(any(), isNull());
    }
}
