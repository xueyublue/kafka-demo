package sg.darren.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sg.darren.kafka.service.LibraryEventsService;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsRetryConsumer {

    private final LibraryEventsService libraryEventsService;

    @KafkaListener(topics = {"${topics.retry}"}, groupId = "retry-listener-group")
    public void onMessage(ConsumerRecord<Long, String> consumerRecord) throws JsonProcessingException {
        log.info("Received Retry: {}", consumerRecord);
        consumerRecord.headers().forEach(header -> log.info("Key: {}, value: {}", header.key(), new String(header.value())));
        // uncomment the below code to retry, it was comment out to prevent infinite calls.
//        libraryEventsService.processLibraryEvent(consumerRecord);
    }

}
