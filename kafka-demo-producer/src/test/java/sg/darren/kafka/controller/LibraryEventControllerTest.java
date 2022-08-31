package sg.darren.kafka.controller;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import sg.darren.kafka.domain.Book;
import sg.darren.kafka.domain.LibraryEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"}
)
class LibraryEventControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Long, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group-1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<Long, String>(configs, new LongDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(5)
    void postLibraryEvent() {
        // given
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(new Date().getTime())
                        .name("Kafka Crash Course")
                        .author("Udemy")
                        .build())
                .build();
        HttpHeaders hh = new HttpHeaders();
        hh.set("Content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> he = new HttpEntity<>(le, hh);

        // when
        ResponseEntity<LibraryEvent> re = testRestTemplate.exchange("/v1/library-event", HttpMethod.POST, he, LibraryEvent.class);

        // then
        Assertions.assertEquals(HttpStatus.CREATED, re.getStatusCode());

        // additional
        ConsumerRecord<Long, String> cr = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        Assertions.assertTrue(cr.value().contains("Kafka Crash Course"));
    }

}
