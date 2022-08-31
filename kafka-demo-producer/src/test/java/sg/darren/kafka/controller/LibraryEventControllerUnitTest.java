package sg.darren.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sg.darren.kafka.domain.Book;
import sg.darren.kafka.domain.LibraryEvent;
import sg.darren.kafka.producer.LibraryEventProducer;

import java.util.Date;

@WebMvcTest(LibraryEventController.class)
@AutoConfigureMockMvc
class LibraryEventControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibraryEventProducer libraryEventProducer;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postLibraryEvent() throws Exception {
        // given
        LibraryEvent le = LibraryEvent.builder()
                .id(null)
                .book(Book.builder()
                        .id(new Date().getTime())
                        .name("Kafka Crash Course")
                        .author("Udemy")
                        .build())
                .build();
        String json = objectMapper.writeValueAsString(le);
        Mockito.doNothing().when(libraryEventProducer).sendLibraryEvent2(Mockito.isA(LibraryEvent.class));

        // when
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/v1/library-event")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // then

    }

}
