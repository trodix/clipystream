package com.trodix.clipystream.model;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class FileUploadRequestLimitUpdateDto {

    private String ipAddress;
    private MultipartFile requestFile;

}
