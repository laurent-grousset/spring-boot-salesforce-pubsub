package com.sample.subscriber;

import com.salesforce.eventbus.protobuf.*;
import com.sample.service.PubSubSchemaService;
import com.sample.util.EventUtil;
import io.grpc.stub.StreamObserver;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class OpportunitySubscriber implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpportunitySubscriber.class);

    @Value("${salesforce.pubsub.topic.opportunity-change-event}")
    private String salesforcePubsubTopicOpportunityChangeEvent;

    @Value("${salesforce.pubsub.batch-size}")
    private Integer salesforcePubsubBatchSize;

    @Autowired
    private PubSubGrpc.PubSubStub pubSubStub;

    @Autowired
    private PubSubSchemaService pubSubSchemaService;

    private StreamObserver<FetchRequest> serverStream;

    @Override
    public void run(ApplicationArguments args) {
        if (serverStream == null)
            serverStream = pubSubStub.subscribe(processChangeEvent());

        serverStream.onNext(getFetchRequest());
    }

    @Scheduled(fixedDelay = 45 * 1000, initialDelay = 45 * 1000)
    public void keepAlive() {
        LOGGER.debug("Avoiding timeout, keeping alive.");
        serverStream.onNext(getFetchRequest());
    }

    private FetchRequest getFetchRequest() {
        return FetchRequest.newBuilder()
                .setReplayPreset(ReplayPreset.LATEST)
                .setTopicName(salesforcePubsubTopicOpportunityChangeEvent)
                .setNumRequested(salesforcePubsubBatchSize).build();
    }

    public StreamObserver<FetchResponse> processChangeEvent() {
        return new StreamObserver<>() {
            @Override
            public void onNext(FetchResponse fetchResponse) {
                for (ConsumerEvent ce : fetchResponse.getEventsList()) {
                    ProducerEvent pe = ce.getEvent();
                    Schema schema = pubSubSchemaService.getSchema(pe.getSchemaId());

                    try {
                        GenericRecord eventPayload = EventUtil.deserialize(schema, pe);
                        LOGGER.info("Received event with Payload: " + eventPayload.toString());

                        List<String> changedFields = EventUtil.getFieldListFromBitmap(schema, (GenericData.Record) eventPayload.get("ChangeEventHeader"), "changedFields");
                        if (!changedFields.isEmpty()) {
                            LOGGER.info("============================");
                            LOGGER.info("       Changed Fields       ");
                            LOGGER.info("============================");
                            for (String field : changedFields) {
                                LOGGER.info(field);
                            }
                            LOGGER.info("============================");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                LOGGER.error("Error during SubscribeStream", t);
            }

            @Override
            public void onCompleted() {
                LOGGER.info("Received requested number of events! Call completed by server.");
            }
        };
    }
}
