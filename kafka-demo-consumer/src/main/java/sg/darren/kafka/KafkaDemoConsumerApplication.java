package sg.darren.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaDemoConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaDemoConsumerApplication.class, args);
	}

}
