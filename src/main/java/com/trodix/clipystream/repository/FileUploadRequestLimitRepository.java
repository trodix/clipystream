package com.trodix.clipystream.repository;

import com.trodix.clipystream.model.FileUploadRequestLimitDto;
import com.trodix.clipystream.model.FileUploadRequestLimitUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FileUploadRequestLimitRepository {

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    public FileUploadRequestLimitDto save(final FileUploadRequestLimitUpdateDto fileUploadRequestLimitUpdateDto) {
        final Long totalUploadRequestSize =
                redisTemplate.opsForValue().increment(fileUploadRequestLimitUpdateDto.getIpAddress(),
                        fileUploadRequestLimitUpdateDto.getRequestFile().getSize());

        final FileUploadRequestLimitDto fileUploadRequestLimitDto = new FileUploadRequestLimitDto();
        fileUploadRequestLimitDto.setIpAddress(fileUploadRequestLimitUpdateDto.getIpAddress());
        fileUploadRequestLimitDto.setTotalUploadedSize(totalUploadRequestSize);

        return fileUploadRequestLimitDto;
    }

    /**
     * Get the current consumed quota in bytes for the given ipAddress
     */
    public Long getQuota(final String ipAddress) {
        final Long quota = redisTemplate.opsForValue().get(ipAddress);
        if (quota == null) {
            return 0L;
        }
        return quota;
    }

}
