package sg.darren.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import sg.darren.kafka.entity.LibraryEvent;
import sg.darren.kafka.repository.LibraryEventsRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsService {

    private final ObjectMapper objectMapper;
    private final LibraryEventsRepository libraryEventsRepository;

    public void processLibraryEvent(ConsumerRecord<Long, String> consumerRecord)
            throws JsonMappingException, JsonProcessingException {
        LibraryEvent le = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("{}", le);

        switch (le.getLibraryEventType()) {
            case NEW:
                save(le);
                break;

            case UPDATE:
                validate(le);
                save(le);
                break;

            default:
                break;
        }
    }

    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getId() == null) {
            throw new IllegalArgumentException("Library event id is empty.");
        }
        Optional<LibraryEvent> optional = libraryEventsRepository.findById(libraryEvent.getId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Library event not found.");
        }
        log.info("Validation passed.");
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Saved to database.");
    }
}
