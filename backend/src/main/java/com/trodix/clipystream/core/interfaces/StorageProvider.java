package com.trodix.clipystream.core.interfaces;

import java.io.File;
import java.io.IOException;
import com.trodix.clipystream.core.exception.ResourceNotFoundException;

public interface StorageProvider {

    public String getEndpointBucketUrl();

    public String getBucketRegion();

    public String getBucketName();

    public String upload(final File video) throws IOException;

    public File download(final String objectKey) throws IOException, ResourceNotFoundException;

}
