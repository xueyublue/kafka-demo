package sg.darren.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import sg.darren.kafka.entity.Book;
import sg.darren.kafka.entity.LibraryEvent;
import sg.darren.kafka.entity.LibraryEventType;
import sg.darren.kafka.repository.LibraryEventsRepository;
import sg.darren.kafka.service.LibraryEventsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events", "library-events.RETRY", "library-events.DLT"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class LibraryEventsConsumerIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumer;

    @SpyBean
    LibraryEventsService libraryEventsService;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @Value("${topics.retry}")
    String retryTopic;

    @Value("${topics.dlt}")
    String deadLetterTopic;

    Consumer<Long, String> consumer;


    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getAllListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    @Test
    void pushLibraryEvent_New() throws ExecutionException, InterruptedException, JsonProcessingException {
        // given
        Book b = Book.builder()
                .id(Long.parseLong("1"))
                .name("Kafka Crash Course")
                .author("Udemy")
                .build();
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(b)
                .build();
        String json = objectMapper.writeValueAsString(le);
        kafkaTemplate.sendDefault(json).get();

        // when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // then
        Mockito.verify(libraryEventsConsumer, Mockito.times(1))
                .onMessage(Mockito.isA(ConsumerRecord.class));
        Mockito.verify(libraryEventsService, Mockito.times(1))
                .processLibraryEvent(Mockito.isA(ConsumerRecord.class));
        List<LibraryEvent> list = (List<LibraryEvent>) libraryEventsRepository.findAll();

        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(1, list.get(0).getBook().getId());
    }

    @Test
    void pushLibraryEvent_Update() throws ExecutionException, InterruptedException, JsonProcessingException {
        // given
        // - insert
        Book b = Book.builder()
                .id(Long.parseLong("1"))
                .name("Kafka Crash Course")
                .author("Udemy")
                .build();
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(b)
                .build();
        b.setLibraryEvent(le);
        libraryEventsRepository.save(le);
        // - update
        Book b2 = Book.builder()
                .id(Long.parseLong("1"))
                .name("Kafka Crash Course 2.X")
                .author("Udemy")
                .build();
        le.setBook(b2);
        le.setLibraryEventType(LibraryEventType.UPDATE);
        String json = objectMapper.writeValueAsString(le);
        kafkaTemplate.sendDefault(json).get();

        // when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // then
        LibraryEvent dbLe = libraryEventsRepository.findById(le.getId()).get();
        Assertions.assertEquals("Kafka Crash Course 2.X", dbLe.getBook().getName());
    }

    @Test
    void pushLibraryEvent_null_LibraryEventId() throws ExecutionException, InterruptedException, JsonProcessingException {
        // given
        Book b = Book.builder()
                .id(Long.parseLong("1"))
                .name("Kafka Crash Course")
                .author("Udemy")
                .build();
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .libraryEventType(LibraryEventType.UPDATE)
                .book(b)
                .build();
        String json = objectMapper.writeValueAsString(le);
        kafkaTemplate.sendDefault(json).get();

        // when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        // then
        Mockito.verify(libraryEventsConsumer, Mockito.times(1))
                .onMessage(Mockito.isA(ConsumerRecord.class));
        Mockito.verify(libraryEventsService, Mockito.times(1))
                .processLibraryEvent(Mockito.isA(ConsumerRecord.class));
    }

    @Test
    void pushLibraryEvent_999_LibraryEventId() throws ExecutionException, InterruptedException, JsonProcessingException {
        // given
        Book b = Book.builder()
                .id(Long.parseLong("1"))
                .name("Kafka Crash Course")
                .author("Udemy")
                .build();
        LibraryEvent le = LibraryEvent.builder()
                .id(Long.parseLong("999"))
                .libraryEventType(LibraryEventType.UPDATE)
                .book(b)
                .build();
        String json = objectMapper.writeValueAsString(le);
        kafkaTemplate.sendDefault(json).get();

        // when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        // then
        Mockito.verify(libraryEventsConsumer, Mockito.times(3))
                .onMessage(Mockito.isA(ConsumerRecord.class));
        Mockito.verify(libraryEventsService, Mockito.times(3))
                .processLibraryEvent(Mockito.isA(ConsumerRecord.class));

        // RETRY
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group-1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<Long, String>(configs, new LongDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, retryTopic);

        ConsumerRecord<Long, String> cr = KafkaTestUtils.getSingleRecord(consumer, retryTopic);
        Assertions.assertTrue(cr.value().contains("Kafka Crash Course"));
    }

}
