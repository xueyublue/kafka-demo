package sg.darren.kafka.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import sg.darren.kafka.domain.Book;
import sg.darren.kafka.domain.LibraryEvent;

import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"}
)
class LibraryEventControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
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
    }

}
