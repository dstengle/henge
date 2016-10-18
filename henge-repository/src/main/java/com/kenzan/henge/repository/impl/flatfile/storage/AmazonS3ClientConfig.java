package com.kenzan.henge.repository.impl.flatfile.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Configuration class for the Amazon S3 Client.
 * It takes the credentials from the default file (~/.aws/credentials)
 *
 * @author wmatsushita
 */
@Profile("flatfile_s3")
@Configuration
public class AmazonS3ClientConfig {

    @Value("${amazon.profile.name}")
    private String profileName;
    
    @Bean
    public AmazonS3 amazonS3Client() {
        
        final AWSCredentials credentials = new ProfileCredentialsProvider(profileName).getCredentials();
        
        return new AmazonS3Client(credentials);
        
    }
    
}
