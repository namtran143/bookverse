package com.ptit.bookverse.service;

import com.ptit.bookverse.exception.BadRequestException;
import com.ptit.bookverse.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

@Service
public class CoverImageService {
    private static final Map<String,Integer> WIDTHS = Map.of("thumbnail", 200, "medium", 500, "large", 1200);
    private final Path uploadRoot;

    public CoverImageService(@Value("${bookverse.upload-root:uploads/covers}") String uploadRoot) {
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    public String save(Long bookId, MultipartFile file) {
        validate(file);
        try {
            BufferedImage source = ImageIO.read(file.getInputStream());
            if (source == null) throw new BadRequestException("Không đọc được nội dung ảnh");
            LocalDate now = LocalDate.now();
            Path directory = uploadRoot.resolve(String.valueOf(now.getYear())).resolve(String.format("%02d", now.getMonthValue()));
            Files.createDirectories(directory);
            for (var entry : WIDTHS.entrySet()) {
                BufferedImage resized = resize(source, entry.getValue());
                Path output = directory.resolve(bookId + "-" + entry.getKey() + ".webp");
                if (!ImageIO.write(resized, "webp", output.toFile())) {
                    throw new IOException("Không tìm thấy WebP ImageIO writer");
                }
            }
            return uploadRoot.relativize(directory).resolve(bookId + "-{size}.webp").toString().replace('\\','/');
        } catch (IOException ex) {
            throw new BadRequestException("Không thể xử lý ảnh bìa: " + ex.getMessage());
        }
    }

    public Resource load(String pattern, String size) {
        if (!WIDTHS.containsKey(size)) throw new BadRequestException("size chỉ nhận thumbnail, medium hoặc large");
        try {
            Path path = uploadRoot.resolve(pattern.replace("{size}", size)).normalize();
            if (!path.startsWith(uploadRoot)) throw new BadRequestException("Đường dẫn ảnh không hợp lệ");
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) throw new ResourceNotFoundException("Không tìm thấy ảnh bìa");
            return resource;
        } catch (IOException ex) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh bìa");
        }
    }

    public void deleteAll(String pattern) {
        if (pattern == null || pattern.isBlank()) return;
        for (String size : WIDTHS.keySet()) {
            try { Files.deleteIfExists(uploadRoot.resolve(pattern.replace("{size}", size)).normalize()); }
            catch (IOException ignored) { }
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Ảnh bìa không được để trống");
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!type.equals("image/jpeg") && !type.equals("image/png") && !type.equals("image/webp")) {
            throw new BadRequestException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WebP");
        }
    }

    private BufferedImage resize(BufferedImage source, int targetWidth) {
        int width = Math.min(targetWidth, source.getWidth());
        int height = Math.max(1, (int) Math.round(source.getHeight() * (width / (double) source.getWidth())));
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, Color.WHITE, null);
        graphics.dispose();
        return output;
    }
}
