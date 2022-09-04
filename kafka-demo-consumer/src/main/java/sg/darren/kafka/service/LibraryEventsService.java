package sg.darren.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sg.darren.kafka.entity.LibraryEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsService {

	private final ObjectMapper objectMapper;

	public void processLibraryEvent(ConsumerRecord<Long, String> consumerRecord)
			throws JsonMappingException, JsonProcessingException {
		LibraryEvent le = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
		log.info("{}", le);

		switch (le.getLibraryEventType()) {
		case NEW:

			break;

		case UPDATE:
			break;

		default:
			break;
		}
	}
}
