package sg.darren.kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name = "book")
public class Book {

	@Id
	private Long id;

	private String name;

	private String author;

	@OneToOne
	@JoinColumn(name = "library_event_id")
	private LibraryEvent libraryEvent;

}
