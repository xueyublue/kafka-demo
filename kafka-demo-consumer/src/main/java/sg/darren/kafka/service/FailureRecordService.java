package sg.darren.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import sg.darren.kafka.entity.FailureRecord;
import sg.darren.kafka.repository.FailureRecordRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailureRecordService {

    private final ObjectMapper objectMapper;
    private final FailureRecordRepository failureRecordRepository;

    public void saveFailureRecord(ConsumerRecord<String, String> consumerRecord, Exception e, String status) {
        FailureRecord fr = FailureRecord.builder()
                .id(null)
                .topic(consumerRecord.topic())
                .key(consumerRecord.key())
                .errorRecord(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offset_value(consumerRecord.offset())
                .exception(e.getCause().getMessage())
                .status(status)
                .build();
        log.info("FailureRecord: {}", fr);
        failureRecordRepository.save(fr);
    }
}
