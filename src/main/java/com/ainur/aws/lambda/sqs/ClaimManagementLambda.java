package com.ainur.aws.lambda.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClaimManagementLambda {
    private final Logger logger = LoggerFactory.getLogger(ClaimManagementLambda.class);

    public void handler(SQSEvent event){
        event.getRecords().forEach(msg -> {
            logger.info(msg.getBody());
        });
    }

}
