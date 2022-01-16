package com.trodix.clipystream.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.text.MessageFormat;
import javax.servlet.http.HttpServletRequest;
import com.trodix.clipystream.core.exception.BadRequestException;
import com.trodix.clipystream.core.exception.ResourceNotFoundException;
import com.trodix.clipystream.core.exception.UnprocessableEntity;
import com.trodix.clipystream.model.FileResponse;
import com.trodix.clipystream.service.StorageService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/public/file")
@Log4j2
public class FileController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public FileResponse uploadFile(final HttpServletRequest request, @RequestParam("file") final MultipartFile file) {

        if (!storageService.isAllowedFileExtension(file.getOriginalFilename())) {
            log.info(MessageFormat.format("Rejected file {0}. Allowed extentions not match. Configured file extenstions are {1}",
                    file.getOriginalFilename(), String.join(", ", storageService.allowedFileExtensions)));
            throw new BadRequestException("File extension forbidden. Allowed file extensions are : " + String.join(", ", storageService.allowedFileExtensions));
        }

        try {
            final String fileObjectName = storageService.save(file);

            final String scheme = request.getScheme();
            final String host = request.getHeader(HttpHeaders.HOST);
            final String fileUrl = MessageFormat.format("{0}://{1}{2}{3}", scheme, host, "/api/public/file/", fileObjectName);

            final FileResponse response = new FileResponse();
            response.setLink(fileUrl);

            return response;

        } catch (IOException | IllegalArgumentException ex) {
            log.info(MessageFormat.format("Error while processing file {0} with size of {1} kb", file.getOriginalFilename(), file.getSize() / 1024), ex);
            throw new UnprocessableEntity("Error while uploading the file");
        }
    }

    @GetMapping(value = "/{encodedUri}")
    public ResponseEntity<byte[]> getFile(@PathVariable("encodedUri") final String encodedUri) {
        try {
            final File file = storageService.getFile(encodedUri);
            final byte[] bytes = FileUtils.readFileToByteArray(file);
            final String mimeType = URLConnection.guessContentTypeFromName(encodedUri);
            final HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + encodedUri);
            responseHeaders.add(HttpHeaders.CONTENT_TYPE, mimeType);
            responseHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length));
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(bytes);
        } catch (final IOException e) {
            throw new ResourceNotFoundException("Unable to retreive file for uri: " + encodedUri);
        }
    }

}
