package com.sample.model;

import io.grpc.CallCredentials;
import io.grpc.Metadata;

import java.util.concurrent.Executor;

public class PubSubStubCredentials extends CallCredentials {

    private static final String INSTANCE_URL = "instanceUrl";
    public static final Metadata.Key<String> INSTANCE_URL_KEY = keyOf(INSTANCE_URL);
    private static final String ACCESS_TOKEN = "accessToken";
    public static final Metadata.Key<String> SESSION_TOKEN_KEY = keyOf(ACCESS_TOKEN);
    private static final String TENANT_ID = "tenantId";
    public static final Metadata.Key<String> TENANT_ID_KEY = keyOf(TENANT_ID);
    private final String instanceUrl;
    private final String tenantId;
    private final String token;

    public PubSubStubCredentials(String tenantId, String instanceUrl, String token) {
        this.instanceUrl = instanceUrl;
        this.tenantId = tenantId;
        this.token = token;
    }

    private static Metadata.Key<String> keyOf(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        Metadata headers = new Metadata();
        headers.put(INSTANCE_URL_KEY, instanceUrl);
        headers.put(TENANT_ID_KEY, tenantId);
        headers.put(SESSION_TOKEN_KEY, token);
        metadataApplier.apply(headers);
    }

    @Override
    public void thisUsesUnstableApi() {
    }
}