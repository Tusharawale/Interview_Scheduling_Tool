package com.example.authadmin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores user uploads on the local disk under {@code app.upload-dir}.
 * <ul>
 *   <li>Root folder and each subfolder (e.g. {@code documents/5/}) are created automatically if missing.</li>
 *   <li>MySQL keeps the relative path under the upload root.</li>
 * </ul>
 */
@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + rootLocation, e);
        }
    }

    /**
     * Save file; returns a relative path stored in MySQL (under the upload dir).
     */
    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) return null;
        return storeLocal(file, subDir);
    }

    private String storeLocal(MultipartFile file, String subDir) {
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
        Path targetDir = rootLocation.resolve(subDir);
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file under " + targetDir, e);
        }
    }

    public Resource loadAsResource(String storageKey) {
        if (storageKey == null || storageKey.isEmpty()) return null;
        return loadLocalAsResource(storageKey);
    }

    private Resource loadLocalAsResource(String relativePath) {
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            if (!file.startsWith(rootLocation)) return null;
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
        } catch (MalformedURLException e) {
            // ignore
        }
        return null;
    }

    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isEmpty()) return;
        deleteLocal(storageKey);
    }

    private void deleteLocal(String relativePath) {
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            if (!file.startsWith(rootLocation)) return;
            if (Files.exists(file)) Files.delete(file);
        } catch (IOException e) {
            // ignore
        }
    }

    /** Absolute path to upload root (for operators / logs). */
    public Path getUploadRoot() {
        return rootLocation;
    }

    private String getExtension(String filename) {
        if (filename == null) return null;
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i + 1) : null;
    }
}
