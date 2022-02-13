package com.trodix.clipystream.model;

import lombok.Data;

@Data
public class FileUploadRequestLimitDto {

    private String ipAddress;
    private long totalUploadedSize;

}
