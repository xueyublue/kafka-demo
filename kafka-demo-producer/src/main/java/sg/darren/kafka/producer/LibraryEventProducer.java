package sg.darren.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import sg.darren.kafka.domain.LibraryEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventProducer {

    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Long key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ListenableFuture<SendResult<Long, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Long, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Long, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    public ListenableFuture<SendResult<Long, String>> sendLibraryEvent2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Long key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ProducerRecord<Long, String> pr = buildProducerRecord(key, value, "library-events");
        ListenableFuture<SendResult<Long, String>> listenableFuture = kafkaTemplate.send(pr);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Long, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Long, String> result) {
                handleSuccess(key, value, result);
            }
        });
        return listenableFuture;
    }

    public SendResult<Long, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent)
            throws ExecutionException, InterruptedException, JsonProcessingException, TimeoutException {
        Long key = libraryEvent.getId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Long, String> sendResult = null;
        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            throw e;
        }
        log.info("Message sent successfully. {}", sendResult);
        return sendResult;
    }

    private ProducerRecord<Long, String> buildProducerRecord(Long key, String value, String topic) {
        List<Header> headers = Collections.singletonList(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<Long, String>(topic, null, key, value, headers);
    }

    private void handleFailure(Long key, String value, Throwable ex) {
        log.error("Error sending message for the key: {} and value is: {}, the exception is {}", key, value, ex.getMessage());
    }

    private void handleSuccess(Long key, String value, SendResult<Long, String> result) {
        log.info("Message sent successfully for the key: {} and value is: {}, partition is: {}", key, value, result.getRecordMetadata().partition());
    }
}
