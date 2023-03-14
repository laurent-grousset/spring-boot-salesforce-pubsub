package com.sample.config;

import com.salesforce.eventbus.protobuf.PubSubGrpc;
import com.sample.model.PubSubStubCredentials;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

@Configuration
public class PubSubStubConfig {
    @Bean
    public ManagedChannel managedChannel(@Value("${salesforce.pubsub.host}") String salesforcePubsubHost,
                                         @Value("${salesforce.pubsub.port}") Integer salesforcePubsubPort) {
        return ManagedChannelBuilder.forAddress(salesforcePubsubHost, salesforcePubsubPort).build();
    }

    @Bean
    public CallCredentials callCredentials(@Value("${salesforce.tenant-id}") String salesforceTenantId,
                                           @Value("${salesforce.instance-url}") String salesforceInstanceUrl,
                                           OAuth2AuthorizedClient salesforceAuthorizedClient) {
        return new PubSubStubCredentials(salesforceTenantId, salesforceInstanceUrl, salesforceAuthorizedClient.getAccessToken().getTokenValue());
    }

    @Bean
    public PubSubGrpc.PubSubStub pubSubStub(ManagedChannel managedChannel, CallCredentials callCredentials) {
        return PubSubGrpc.newStub(managedChannel).withCallCredentials(callCredentials);
    }

    @Bean
    public PubSubGrpc.PubSubBlockingStub pubSubBlockingStub(ManagedChannel managedChannel, CallCredentials callCredentials) {
        return PubSubGrpc.newBlockingStub(managedChannel).withCallCredentials(callCredentials);
    }
}
