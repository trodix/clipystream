package com.trodix.clipystream.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import javax.annotation.PostConstruct;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.trodix.clipystream.core.exception.ResourceNotFoundException;
import com.trodix.clipystream.core.interfaces.StorageProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class S3StorageProvider implements StorageProvider {

    public static final String NOT_FOUND_OBJECT_ERROR_CODE = "NoSuchKey";

    private AmazonS3 s3client;

    @Value("${app.storage.provider.s3.access-key}")
    private String accessKey;

    @Value("${app.storage.provider.s3.secret-key}")
    private String secretKey;

    @Value("${app.storage.provider.s3.region}")
    private String region;

    @Value("${app.storage.provider.s3.endpoint-bucket-url}")
    private String endpointBucketUrl;

    @Value("${app.storage.provider.s3.bucket-name}")
    private String bucketName;

    @PostConstruct
    private void init() {
        final AWSCredentials credentials = new BasicAWSCredentials(getAccesKey(), getSecretKey());

        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new EndpointConfiguration(getEndpointBucketUrl(), getBucketRegion()))
                .build();
    }

    @Override
    public String getEndpointBucketUrl() {
        return endpointBucketUrl;
    }

    @Override
    public String getBucketRegion() {
        return region;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    private String getAccesKey() {
        return accessKey;
    }

    private String getSecretKey() {
        return secretKey;
    }

    @Override
    public String upload(final File file) throws AmazonServiceException, SdkClientException, IOException {
        final String objectName = generateObjectName(file.getName());
        s3client.putObject(getBucketName(), objectName, file);
        return objectName;
    }

    @Override
    public File download(final String objectKey) throws IOException, ResourceNotFoundException {
        S3Object s3object = null;
        try {
            s3object = s3client.getObject(getBucketName(), objectKey);
        } catch (final AmazonServiceException e) {
            final String errorCode = e.getErrorCode();

            log.error(MessageFormat.format("Error while downloading file {0}", objectKey), e);
            if (errorCode.equals(NOT_FOUND_OBJECT_ERROR_CODE)) {
                throw new ResourceNotFoundException("Object not found for key " + objectKey);
            }

            throw new IOException(e);
        }

        final File tmpFile = Files.createTempFile("s3storage", ".tmp").toFile();
        tmpFile.deleteOnExit();

        try (final InputStream inputStream = s3object.getObjectContent()) {
            FileUtils.copyInputStreamToFile(inputStream, tmpFile);
            return tmpFile;
        }

    }

    private String generateObjectName(final String fileObjectName) {
        final String baseName = FilenameUtils.getBaseName(fileObjectName);
        final String extension = FilenameUtils.getExtension(fileObjectName);

        final String cleanBaseName = baseName.replaceAll("[^\\w\\.-]", "_");
        return cleanBaseName + "." + extension;
    }

}
