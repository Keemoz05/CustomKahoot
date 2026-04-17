package com.syed.QuizYa.config;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CLOUDINARY CONFIGURATION:
 * Creates a single shared Cloudinary SDK instance available throughout the application.
 *
 * @Value("${cloudinary.cloud-name}") reads the value from application.properties.
 * Spring Boot then injects this single instance wherever Cloudinary is used (e.g. MediaService).
 *
 * The class name 'CloudinaryConfig' avoids a conflict with the SDK's own 'Cloudinary' class.
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    public com.cloudinary.Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        return new com.cloudinary.Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
