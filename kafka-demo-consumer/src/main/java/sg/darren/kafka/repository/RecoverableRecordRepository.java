package sg.darren.kafka.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sg.darren.kafka.entity.RecoverableRecord;

@Repository
public interface RecoverableRecordRepository extends CrudRepository<RecoverableRecord, Long> {

}
