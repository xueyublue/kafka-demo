package sg.darren.kafka.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sg.darren.kafka.entity.RecoverableRecord;
import sg.darren.kafka.entity.RecoverableStatus;

import java.util.List;

@Repository
public interface RecoverableRecordRepository extends CrudRepository<RecoverableRecord, Long> {

    List<RecoverableRecord> findAllByStatus(RecoverableStatus status);

}
