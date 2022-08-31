package sg.darren.kafka.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

    private Long id;
    private LibraryEventType libraryEventType;
    private Book book;

}
