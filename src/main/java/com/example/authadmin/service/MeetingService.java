package com.example.authadmin.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeetingService {

	public record StoredFile(String id, String fileName, Path path, MediaType contentType, Instant createdAt) {}

	private final Map<String, StoredFile> filesById = new ConcurrentHashMap<>();

	public String createRoomId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public StoredFile storeFile(String roomId, MultipartFile file) throws IOException {
		String original = file.getOriginalFilename();
		String safeName = StringUtils.hasText(original) ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "document";
		String id = UUID.randomUUID().toString().replace("-", "");

		Path baseDir = Path.of(System.getProperty("java.io.tmpdir"), "intern-pro", "meetings", roomId);
		Files.createDirectories(baseDir);
		Path target = baseDir.resolve(id + "_" + safeName);
		file.transferTo(target.toFile());

		MediaType ct = MediaType.APPLICATION_OCTET_STREAM;
		if (StringUtils.hasText(file.getContentType())) {
			try {
				ct = MediaType.parseMediaType(file.getContentType());
			} catch (Exception ignored) {
				// keep octet-stream
			}
		}

		StoredFile stored = new StoredFile(id, safeName, target, ct, Instant.now());
		filesById.put(id, stored);
		return stored;
	}

	public StoredFile getFile(String fileId) {
		StoredFile stored = filesById.get(fileId);
		if (stored == null) throw new IllegalStateException("File not found");
		return stored;
	}

	public Resource asResource(StoredFile stored) {
		return new FileSystemResource(stored.path());
	}
}

