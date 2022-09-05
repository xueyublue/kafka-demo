package sg.darren.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.darren.kafka.domain.LibraryEvent;
import sg.darren.kafka.domain.LibraryEventType;
import sg.darren.kafka.producer.LibraryEventProducer;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/library-event")
public class LibraryEventController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEvent2(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping
    public ResponseEntity<Object> putLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        if (libraryEvent.getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please input Event ID.");
        }
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEventProducer.sendLibraryEvent2(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

}
