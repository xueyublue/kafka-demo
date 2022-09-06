package sg.darren.kafka.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sg.darren.kafka.entity.RecoverableRecord;
import sg.darren.kafka.entity.RecoverableStatus;
import sg.darren.kafka.repository.RecoverableRecordRepository;
import sg.darren.kafka.service.LibraryEventsService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

    private final RecoverableRecordRepository recoverableRecordRepository;
    private final LibraryEventsService libraryEventsService;

    @Scheduled(fixedRate = 10000)
    public void retryRecoverableRecord() {
        List<RecoverableRecord> list = recoverableRecordRepository.findAllByStatus(RecoverableStatus.RETRY);
        if(list.isEmpty()) {
            return;
        }
        log.info("Retrying recoverable records...");
        list.forEach(recoverableRecord -> {
            log.info("Retrying recoverable records: {}", recoverableRecord);
            ConsumerRecord<Long, String> cr = buildConsumerRecord(recoverableRecord);
            try {
                libraryEventsService.processLibraryEvent(cr);
                recoverableRecord.setStatus(RecoverableStatus.SUCCESS);
            } catch (Exception ex) {
                log.error("Retrying Failed with Exception: {}", ex.getMessage());
            }
        });
        log.info("Retrying recoverable records completed!");
    }

    private ConsumerRecord<Long, String> buildConsumerRecord(RecoverableRecord recoverableRecord) {
        return new ConsumerRecord<>(
                recoverableRecord.getTopic(),
                recoverableRecord.getPartition(),
                recoverableRecord.getOffsetValue(),
                recoverableRecord.getKey(),
                recoverableRecord.getValue()
        );
    }
}
