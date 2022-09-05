package sg.darren.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import sg.darren.kafka.entity.RecoverableStatus;
import sg.darren.kafka.entity.RecoverableRecord;
import sg.darren.kafka.repository.FailureRecordRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailureRecordService {

    private final FailureRecordRepository failureRecordRepository;

    public void saveRecoverableRecord(ConsumerRecord<Long, String> consumerRecord, Exception ex, RecoverableStatus status) {
        RecoverableRecord rr = RecoverableRecord.builder()
                .id(null)
                .topic(consumerRecord.topic())
                .key(consumerRecord.key())
                .value(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offset(consumerRecord.offset())
                .exception(ex.getCause().getMessage())
                .status(status)
                .build();
        failureRecordRepository.save(rr);
        log.info("Saved RecoverableRecord with status {}: {}", status, rr);
    }
}
