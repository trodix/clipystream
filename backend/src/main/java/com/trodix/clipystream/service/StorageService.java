package com.trodix.clipystream.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;
import com.trodix.clipystream.core.exception.FileUploadQuotaLimitExceededException;
import com.trodix.clipystream.core.exception.ResourceNotFoundException;
import com.trodix.clipystream.core.interfaces.StorageProvider;
import com.trodix.clipystream.model.FileUploadRequestLimitUpdateDto;
import com.trodix.clipystream.repository.FileUploadRequestLimitRepository;
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

    @Autowired
    private FileUploadRequestLimitRepository fileUploadRequestLimitRepository;

    @Value("#{'${app.storage.allowed-file-extentions}'.split(',')}")
    public List<String> allowedFileExtensions;

    @Value("${app.storage.max-quota-per-day-mb}")
    public int maxFileUploadQuotaPerDay;

    public String save(final FileUploadRequestLimitUpdateDto fileUploadRequestLimitUpdateDto)
            throws IOException, IllegalArgumentException, FileUploadQuotaLimitExceededException {
        final MultipartFile requestFile = fileUploadRequestLimitUpdateDto.getRequestFile();


        if (!hasQuota(fileUploadRequestLimitUpdateDto)) {
            throw new FileUploadQuotaLimitExceededException(
                    MessageFormat.format("Your file upload size quota ({0} MB) has exceeded. The quota is reset every 24 hours after the first file upload.",
                            maxFileUploadQuotaPerDay));
        }

        final String baseName = FilenameUtils.getBaseName(requestFile.getOriginalFilename());
        final String extension = FilenameUtils.getExtension(requestFile.getOriginalFilename());
        final File tmpFile = Files.createTempFile(baseName + "-", "." + extension).toFile();
        tmpFile.deleteOnExit();

        try (final InputStream is = requestFile.getInputStream()) {
            FileUtils.copyInputStreamToFile(is, tmpFile);
        }

        String fileObjectName = null;
        fileObjectName = storageProvider.upload(tmpFile);
        fileUploadRequestLimitRepository.save(fileUploadRequestLimitUpdateDto);

        return fileObjectName;
    }

    public File getFile(final String fileObjectName) throws IOException, ResourceNotFoundException {
        return storageProvider.download(fileObjectName);
    }

    public boolean isAllowedFileExtension(final String filename) {
        final String fileExtension = FilenameUtils.getExtension(filename);
        return allowedFileExtensions.contains(fileExtension);
    }

    public boolean hasQuota(final FileUploadRequestLimitUpdateDto requestDto) {
        return fileUploadRequestLimitRepository.getQuota(requestDto.getIpAddress()) + requestDto.getRequestFile().getSize() < maxFileUploadQuotaPerDay * 1024
                * 1024;
    }

}
