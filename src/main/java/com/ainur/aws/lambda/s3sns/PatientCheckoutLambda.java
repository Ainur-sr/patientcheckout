package com.ainur.aws.lambda.s3sns;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class PatientCheckoutLambda {

    private static final String PATIENT_CHECKOUT_TOPIC = System.getenv("PATIENT_CHECKOUT_TOPIC");

    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();
    private final Logger logger = LoggerFactory.getLogger(PatientCheckoutLambda.class);

    public void handler(S3Event event, Context context) {

        event.getRecords().forEach(record -> {
            S3Object object = s3.getObject(record.getS3().getBucket().getName(), record.getS3().getObject().getKey());
            S3ObjectInputStream s3ObjectInputStream = object.getObjectContent();
            try {
                logger.info("Reading data from S3");
                List<PatientCheckoutEvent> patientCheckoutEvents
                        = Arrays.asList(objectMapper.readValue(s3ObjectInputStream, PatientCheckoutEvent[].class));
                logger.info(patientCheckoutEvents.toString());
                s3ObjectInputStream.close();
                logger.info("Message being published to SNS");
                publishMessageToSNS(patientCheckoutEvents);
            } catch (IOException e) {
                logger.error("Exception is: ", e);
                throw new RuntimeException("Error while processing S3 event", e);
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
