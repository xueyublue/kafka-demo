package sg.darren.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import sg.darren.kafka.entity.RecoverableStatus;
import sg.darren.kafka.service.FailureRecordService;

import java.util.Collections;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Autowired
    private KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    private FailureRecordService failureRecordService;

    @Value("${topics.retry}")
    private String retryTopic;

    @Value("${topics.dlt}")
    private String deadLetterTopic;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setCommonErrorHandler(errorHandler());
//		factory.setConcurrency(3);	// multiple threads within same consumer
//		factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        return factory;
    }

    private DefaultErrorHandler errorHandler() {

        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);

//		ExponentialBackOffWithMaxRetries exponentialBackOffWithMaxRetries = new ExponentialBackOffWithMaxRetries(2);
//		exponentialBackOffWithMaxRetries.setInitialInterval(1_000L);
//		exponentialBackOffWithMaxRetries.setMultiplier(2.0);
//		exponentialBackOffWithMaxRetries.setMaxInterval(2_000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
//                publishingRecoverer(),
                consumerRecordRecoverer,
                fixedBackOff
        );

        Collections.singletonList(IllegalArgumentException.class)
                .forEach(errorHandler::addNotRetryableExceptions);

        Collections.singletonList(RecoverableDataAccessException.class)
                .forEach(errorHandler::addRetryableExceptions);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt)
                -> log.info("Failed record in RetryListener, Exception: {}, deliveryAttempt: {}", ex.getMessage(), deliveryAttempt));
        return errorHandler;
    }

    public DeadLetterPublishingRecoverer publishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
            log.error("Exception in publishingRecoverer(): {}", e.getMessage());
            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(retryTopic, r.partition());
            } else {
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        });
    }

    ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, e) -> {
        log.error("Exception in consumerRecordRecoverer(): {}", e.getMessage());
        if (e.getCause() instanceof RecoverableDataAccessException) {
            failureRecordService.saveRecoverableRecord((ConsumerRecord<Long, String>) consumerRecord, e, RecoverableStatus.RETRY);
        } else {
            failureRecordService.saveRecoverableRecord((ConsumerRecord<Long, String>) consumerRecord, e, RecoverableStatus.NO_RETRY);
        }
    };

}
