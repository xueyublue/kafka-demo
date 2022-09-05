package sg.darren.kafka.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sg.darren.kafka.entity.LibraryEvent;

@Repository
public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Long> {

}
