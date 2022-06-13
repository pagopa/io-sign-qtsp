package it.pagopa.firmaconio.firma_qtsp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import it.pagopa.firmaconio.firma_qtsp.exception.FileStorageException;
import it.pagopa.firmaconio.firma_qtsp.config.FileStorageProperties;
import it.pagopa.firmaconio.firma_qtsp.exception.UploadedFileException;

@Service
public class FileStorageService {

    // Temp DB
    LinkedHashMap<String, Resource> fileNameStorage = new LinkedHashMap<>();

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
    }

    public File storeFile(@NotNull MultipartFile file) {
        try {
            // Normalize file name
            String originalFileName = file.getOriginalFilename();
            String ext = "." + StringUtils.getFilenameExtension(originalFileName);

            File tmpFile = Files.createTempFile(StringUtils.stripFilenameExtension(originalFileName), ext).toFile();
            String fileName = tmpFile.getName();

            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = tmpFile.toPath();

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Resource resource = new UrlResource(targetLocation.toUri());
            fileNameStorage.put(fileName, resource);
            System.out.println(fileNameStorage);

            return tmpFile;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(@NotBlank String fileName) {
        Resource resource = fileNameStorage.get(fileName);
        if (resource.exists()) {
            return resource;
        } else {
            throw new UploadedFileException("File not found " + fileName);
        }
    }
}