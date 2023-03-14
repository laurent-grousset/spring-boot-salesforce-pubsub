package com.sample.service;

import com.salesforce.eventbus.protobuf.PubSubGrpc;
import com.salesforce.eventbus.protobuf.SchemaRequest;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PubSubSchemaService {
    @Autowired
    private PubSubGrpc.PubSubBlockingStub pubSubBlockingStub;

    @Cacheable("schema")
    public Schema getSchema(String schemaId) {
        SchemaRequest request = buildRequest(schemaId);
        String schemaJson = pubSubBlockingStub.getSchema(request).getSchemaJson();
        return (new Schema.Parser()).parse(schemaJson);
    }

    private SchemaRequest buildRequest(String schemaId) {
        return SchemaRequest.newBuilder().setSchemaId(schemaId).build();
    }
}
