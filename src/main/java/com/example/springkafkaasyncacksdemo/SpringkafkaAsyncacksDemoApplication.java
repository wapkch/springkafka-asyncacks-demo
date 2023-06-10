package com.example.springkafkaasyncacksdemo;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SpringkafkaAsyncacksDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringkafkaAsyncacksDemoApplication.class, args);
    }

    @Autowired
    private KafkaTemplate<Object, Object> template;

    @PostMapping(path = "/send/{what}")
    public void sendFoo(@PathVariable String what) {
        this.template.send("topic1", what);
    }

    @KafkaListener(id = "fooGroup", topics = "topic1")
    @RetryableTopic(attempts = "2", backoff = @Backoff(delay = 2_000, maxDelay = 2_000, multiplier = 2))
    public void onMessage(ConsumerRecord<String, ?> record, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        if (record.value().equals("fail")) {
            throw new RuntimeException("failed");
        }
        acknowledgment.acknowledge();
    }

}
