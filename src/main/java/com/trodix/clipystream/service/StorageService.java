package com.trodix.clipystream.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import com.trodix.clipystream.core.exception.ResourceNotFoundException;
import com.trodix.clipystream.core.interfaces.StorageProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    @Autowired
    private StorageProvider storageProvider;

    @Value("#{'${app.storage.allowed-file-extentions}'.split(',')}")
    public List<String> allowedFileExtensions;

    public String save(final MultipartFile requestFile) throws IOException, IllegalArgumentException {

        final String baseName = FilenameUtils.getBaseName(requestFile.getOriginalFilename());
        final String extension = FilenameUtils.getExtension(requestFile.getOriginalFilename());
        final File tmpFile = Files.createTempFile(baseName + "-", "." + extension).toFile();
        tmpFile.deleteOnExit();

        try (final InputStream is = requestFile.getInputStream()) {
            FileUtils.copyInputStreamToFile(is, tmpFile);
        }

        return storageProvider.upload(tmpFile);
    }

    public File getFile(final String fileObjectName) throws IOException, ResourceNotFoundException {
        return storageProvider.download(fileObjectName);
    }

    public boolean isAllowedFileExtension(final String filename) {
        final String fileExtension = FilenameUtils.getExtension(filename);
        return allowedFileExtensions.contains(fileExtension);
    }

}
