package com.therdnotes.tweet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.therdnotes.KinesisService;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Controller
public class TweetController {
    Logger log = LoggerFactory.getLogger(TweetController.class);

    @Inject
    KinesisService kinesisService;

    public TweetController(KinesisService kinesisService) {
        this.kinesisService = kinesisService;
    }

    @Post(value = "/tweet", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public void postTweet(@Body Tweet tweet) {
        String reqId = UUID.randomUUID().toString();
        try {
            kinesisService.addRecord(tweet);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong for the req(" + reqId + ")");
        }

    }
}
