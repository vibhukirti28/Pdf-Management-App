package com.pdfmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

/**
 * Service for handling file storage operations, specifically for PDF files.
 * <p>
 * This service provides functionality to store uploaded PDF files to a specified directory.
 * The upload directory is configured via the {@code file.upload-dir} property.
 * Only files with a ".pdf" extension are allowed; attempts to store other file types will result in an exception.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * String storedPath = fileStorageService.storeFile(multipartFile);
 * }
 * </pre>
 * </p>
 *
 * @author YourName
 * @since 1.0
 */
@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        if (!filename.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Only PDF files are allowed.");
        }

        Path storagePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(storagePath);

        Path targetLocation = storagePath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }
}
