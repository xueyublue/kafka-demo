package sg.darren.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sg.darren.kafka.service.LibraryEventsService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumer {

	private final LibraryEventsService libraryEventsService;

	@KafkaListener(topics = { "library-events" })
	public void onMessage(ConsumerRecord<Long, String> consumerRecord)
			throws JsonMappingException, JsonProcessingException {
		log.info("Received: {}", consumerRecord);
		libraryEventsService.processLibraryEvent(consumerRecord);
	}

}
