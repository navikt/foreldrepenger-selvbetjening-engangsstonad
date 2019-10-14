package no.nav.foreldrepenger.selvbetjening.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import no.nav.foreldrepenger.selvbetjening.tjeneste.mellomlagring.S3Storage;
import no.nav.foreldrepenger.selvbetjening.tjeneste.mellomlagring.Storage;
import no.nav.foreldrepenger.selvbetjening.tjeneste.mellomlagring.StorageCrypto;

//@Configuration
//@ConditionalOnGCP
public class S3GCPStorageConfiguration {

    @Bean
    public Storage s3GCPStorage(AmazonS3 s3) {
        return new S3Storage(s3);
    }

    @Bean
    @Lazy
    public AmazonS3 s3(AWSCredentials s3Credentials, EndpointConfiguration endpointConfig) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                "https://storage.googleapis.com", "auto"))
                .withCredentials(new AWSStaticCredentialsProvider(s3Credentials))
                .build();
    }

    @Bean
    public AWSCredentials s3Credentials(@Value("${gcp.accessKey}") String accessKey,
            @Value("${gcp.secretKey}") String secretKey) {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public StorageCrypto storageCrypto(@Value("${storage.passphrase}") String encryptionPassphrase) {
        return new StorageCrypto(encryptionPassphrase);
    }

}
