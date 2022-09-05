package sg.darren.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

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

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(fixedBackOff);

		Collections.singletonList(IllegalArgumentException.class)
				.forEach(errorHandler::addNotRetryableExceptions);

//		Collections.singletonList(RecoverableDataAccessException.class)
//				.forEach(errorHandler::addRetryableExceptions);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt)
                -> log.info("Failed record in RetryListener, Exception: {}, deliveryAttempt: {}", ex.getMessage(), deliveryAttempt));
        return errorHandler;
    }

}
