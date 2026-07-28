package com.ptit.bookverse.service;

import com.ptit.bookverse.dto.BookRequest;
import com.ptit.bookverse.dto.ImportResult;
import com.ptit.bookverse.exception.BadRequestException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class BulkImportService {
    private final BookService bookService;

    public BulkImportService(BookService bookService) { this.bookService = bookService; }

    public ImportResult importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Vui lòng chọn file CSV hoặc XLSX");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        try {
            if (name.endsWith(".csv")) return importCsv(file);
            if (name.endsWith(".xlsx")) return importExcel(file);
        } catch (IOException ex) {
            throw new BadRequestException("Không thể đọc file import: " + ex.getMessage());
        }
        throw new BadRequestException("Chỉ hỗ trợ file .csv hoặc .xlsx");
    }

    private ImportResult importCsv(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        int total = 0, imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line == null) return new ImportResult(0,0,0,List.of());
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                total++;
                try {
                    List<String> c = parseCsvLine(line);
                    if (c.size() < 7) throw new IllegalArgumentException("Thiếu cột");
                    bookService.create(toRequest(c), null);
                    imported++;
                } catch (Exception ex) {
                    errors.add("Dòng " + row + ": " + ex.getMessage());
                }
            }
        }
        return new ImportResult(total, imported, total-imported, errors);
    }

    private ImportResult importExcel(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        int total = 0, imported = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            for (int r=1; r<=sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                total++;
                try {
                    List<String> c = new ArrayList<>();
                    for (int i=0;i<7;i++) c.add(fmt.formatCellValue(row.getCell(i)).trim());
                    bookService.create(toRequest(c), null);
                    imported++;
                } catch (Exception ex) {
                    errors.add("Dòng " + (r+1) + ": " + ex.getMessage());
                }
            }
        }
        return new ImportResult(total, imported, total-imported, errors);
    }

    private BookRequest toRequest(List<String> c) {
        return new BookRequest(c.get(0), c.get(1), c.get(2), Integer.valueOf(c.get(3)),
                c.get(4), new BigDecimal(c.get(5)), c.get(6));
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i=0;i<line.length();i++) {
            char ch=line.charAt(i);
            if (ch=='"') {
                if (quoted && i+1<line.length() && line.charAt(i+1)=='"') { value.append('"'); i++; }
                else quoted=!quoted;
            } else if (ch==',' && !quoted) { out.add(value.toString().trim()); value.setLength(0); }
            else value.append(ch);
        }
        out.add(value.toString().trim());
        return out;
    }
}
