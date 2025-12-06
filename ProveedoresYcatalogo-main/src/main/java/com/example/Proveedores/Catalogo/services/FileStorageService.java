package com.example.Proveedores.Catalogo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadDir;

    public FileStorageService(@Value("${storage.upload-dir:uploads}") String dir) throws IOException {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
        log.info("[FileStorage] Directorio de subidas: {}", this.uploadDir);
    }

    public String store(MultipartFile file) {
        if (file == null) {
            log.warn("[FileStorage] No se recibió archivo (logo == null)");
            return null;
        }
        if (file.isEmpty()) {
            log.warn("[FileStorage] Archivo vacío: originalName={} size=0", file.getOriginalFilename());
            return null;
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot != -1) ext = original.substring(dot);
        String filename = "logo_" + Instant.now().toEpochMilli() + ext;
        Path target = uploadDir.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("[FileStorage] Guardado archivo: originalName={} size={}B path={} url=/files/{}",
                    original, file.getSize(), target.toString(), filename);
        } catch (IOException e) {
            log.error("[FileStorage] Error guardando archivo {}: {}", original, e.getMessage(), e);
            throw new RuntimeException("Error guardando archivo", e);
        }
        return "/files/" + filename;
    }
}
