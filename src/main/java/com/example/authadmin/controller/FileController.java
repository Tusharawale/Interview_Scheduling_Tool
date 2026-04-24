package com.example.authadmin.controller;

import com.example.authadmin.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{*path}")
    public ResponseEntity<Resource> getFile(@PathVariable("path") String path) {
        if (path != null && path.startsWith("/")) path = path.substring(1);
        Resource resource = fileStorageService.loadAsResource(path);
        if (resource == null) return ResponseEntity.notFound().build();
        String filename = resource.getFilename();
        MediaType contentType = resolveMediaType(path, resource);
        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (filename != null ? filename : "file") + "\"")
            .body(resource);
    }

    private static MediaType resolveMediaType(String relativePath, Resource resource) {
        try {
            if (resource != null && resource.isFile()) {
                String probed = Files.probeContentType(resource.getFile().toPath());
                if (probed != null) {
                    return MediaType.parseMediaType(probed);
                }
            }
        } catch (IOException ignored) {
        }
        String p = "";
        if (resource != null && resource.getFilename() != null) {
            p = resource.getFilename().toLowerCase();
        } else if (relativePath != null) {
            p = relativePath.toLowerCase();
        }
        if (p.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (p.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (p.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (p.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (p.endsWith(".doc")) return MediaType.parseMediaType("application/msword");
        if (p.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
