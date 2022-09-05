package sg.darren.kafka.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sg.darren.kafka.entity.FailureRecord;
import sg.darren.kafka.entity.LibraryEvent;

@Repository
public interface FailureRecordRepository extends CrudRepository<FailureRecord, Long> {

}
