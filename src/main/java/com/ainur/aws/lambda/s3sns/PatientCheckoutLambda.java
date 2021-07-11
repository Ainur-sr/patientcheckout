package com.ainur.aws.lambda.s3sns;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PatientCheckoutLambda {

    private static final String PATIENT_CHECKOUT_TOPIC = System.getenv("PATIENT_CHECKOUT_TOPIC");

    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();

    public void handler(S3Event event) {
        event.getRecords().forEach(record -> {
            S3Object object = s3.getObject(record.getS3().getBucket().getName(), record.getS3().getObject().getKey());
            S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();
            try {
                List<PatientCheckoutEvent> patientCheckoutEvents
                        = Arrays.asList(objectMapper.readValue(s3ObjectInputStream, PatientCheckoutEvent[].class));
                System.out.println(patientCheckoutEvents);
                s3ObjectInputStream.close();
                publishMessageToSNS(patientCheckoutEvents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void publishMessageToSNS(List<PatientCheckoutEvent> patientCheckoutEvents) {
        patientCheckoutEvents.forEach(checkoutEvent -> {
            try {
                sns.publish(PATIENT_CHECKOUT_TOPIC, objectMapper.writeValueAsString(checkoutEvent));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

}
