package sg.darren.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import sg.darren.kafka.domain.Book;
import sg.darren.kafka.domain.LibraryEvent;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerUnitTest {

    @Mock
    private KafkaTemplate<Long, String> kafkaTemplate;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private LibraryEventProducer libraryEventProducer;

    @Test
    void sendLibraryEvent2_failure() {
        // given
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(new Date().getTime())
                        .name("Kafka Crash Course")
                        .author("Udemy")
                        .build())
                .build();

        SettableListenableFuture<SendResult<Long, String>> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException("Exception."));
        Mockito.when(kafkaTemplate.send(Mockito.isA(ProducerRecord.class)))
                .thenReturn(future);
        // when
        Assertions.assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEvent2(le).get());
    }

    @Test
    void sendLibraryEvent2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        // given
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(new Date().getTime())
                        .name("Kafka Crash Course")
                        .author("Udemy")
                        .build())
                .build();

        SettableListenableFuture<SendResult<Long, String>> future = new SettableListenableFuture<>();
        ProducerRecord<Long, String> pr = new ProducerRecord<>(
                "library-events",
                le.getId(),
                objectMapper.writeValueAsString(le)
        );
        RecordMetadata rm = new RecordMetadata(
                new TopicPartition("library-events", 1),
                1,
                1,
                1,
                System.currentTimeMillis(),
                1,
                1
        );
        SendResult<Long, String> sr = new SendResult<>(pr, rm);
        future.set(sr);
        Mockito.when(kafkaTemplate.send(Mockito.isA(ProducerRecord.class)))
                .thenReturn(future);

        // when
        ListenableFuture<SendResult<Long, String>> lf = libraryEventProducer.sendLibraryEvent2(le);

        // then
        Assertions.assertEquals(1, lf.get().getRecordMetadata().partition());
    }
}
