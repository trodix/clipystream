package com.trodix.clipystream.config;

import com.trodix.clipystream.core.interfaces.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
public class StorageProviderConfig {

    @Value("${app.storage.provider.class}")
    private String providerClassPath;

    @Bean
    public StorageProvider storageProvider(final GenericApplicationContext context) throws ClassNotFoundException, IllegalArgumentException, SecurityException {
        final Class<? extends StorageProvider> clazz = (Class<? extends StorageProvider>) Class.forName(providerClassPath);
        context.registerBean("StorageProvider", clazz);
        return context.getBean(clazz);
    }

}
