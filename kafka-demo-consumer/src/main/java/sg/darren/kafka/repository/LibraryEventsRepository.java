package sg.darren.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.darren.kafka.entity.LibraryEvent;

@Repository
public interface LibraryEventsRepository extends JpaRepository<Long, LibraryEvent> {

}
