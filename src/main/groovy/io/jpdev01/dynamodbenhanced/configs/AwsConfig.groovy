package io.jpdev01.dynamodbenhanced.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@ConditionalOnProperty(value = 'application.localstack', havingValue = 'false', matchIfMissing = true)
@Configuration
class AwsConfig {

    private static final AwsCredentialsProvider AWS_CREDENTIALS

    static {
        AWS_CREDENTIALS = getCredentials()
    }

    private final Region region

    AwsConfig(@Value('${application.aws-region}') String region) {
        this.region = Optional
            .ofNullable(region)
                .map(Region::of)
                .orElse(Region.SA_EAST_1)
    }

    @Bean
    DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .region(region)
            .credentialsProvider(AWS_CREDENTIALS)
            .build()
    }

    @Bean
    DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(client)
            .build()
    }

    private static AwsCredentialsProvider getCredentials() {
        try {
            return DefaultCredentialsProvider
                .create()
                .tap {
                    it.resolveCredentials()
                }
        } catch (SdkClientException exception) {
            return ProfileCredentialsProvider.create()
        }
    }

}
