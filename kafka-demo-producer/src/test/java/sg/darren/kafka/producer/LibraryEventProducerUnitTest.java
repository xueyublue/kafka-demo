package sg.darren.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import org.springframework.util.concurrent.SettableListenableFuture;
import sg.darren.kafka.domain.Book;
import sg.darren.kafka.domain.LibraryEvent;

import java.util.Date;

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
}
