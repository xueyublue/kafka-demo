package sg.darren.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import sg.darren.kafka.entity.RecoverableStatus;
import sg.darren.kafka.entity.RecoverableRecord;
import sg.darren.kafka.repository.RecoverableRecordRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoverableRecordService {

    private final RecoverableRecordRepository recoverableRecordRepository;

    public void saveRecoverableRecord(ConsumerRecord<Long, String> consumerRecord, Exception ex, RecoverableStatus status) {
        RecoverableRecord rr = RecoverableRecord.builder()
                .id(null)
                .topic(consumerRecord.topic())
                .key(consumerRecord.key())
                .value(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offsetValue(consumerRecord.offset())
                .exception(ex.getCause().getMessage())
                .status(status)
                .build();
        recoverableRecordRepository.save(rr);
        log.info("Saved RecoverableRecord with status {}: {}", status, rr);
    }
}
