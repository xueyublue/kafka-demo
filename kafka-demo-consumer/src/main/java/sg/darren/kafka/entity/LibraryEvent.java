package sg.darren.kafka.entity;

import lombok.*;
import lombok.ToString.Exclude;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEvent {

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private LibraryEventType libraryEventType;

	@Exclude
	@OneToOne(mappedBy = "libraryEvent", cascade = { CascadeType.ALL })
	private Book book;

}
