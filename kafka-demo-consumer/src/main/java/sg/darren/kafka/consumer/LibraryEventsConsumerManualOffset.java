package sg.darren.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class LibraryEventsConsumerManualOffset implements AcknowledgingMessageListener<Long, String> {

	@Override
	@KafkaListener(topics = { "library-events" })
	public void onMessage(ConsumerRecord<Long, String> consumerRecord, Acknowledgment acknowledgment) {
		log.info("Received - Manual ACK: {}", consumerRecord);
		acknowledgment.acknowledge();
	}

}
