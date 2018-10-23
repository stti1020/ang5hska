package at.oenb.dltrouting;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan({ "at.oenb.dlt", "at.oenb.dltrouting" })
public class DltRoutingApplication {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(DltRoutingApplication.class);

	public static void main(String[] args) {
		log.info(String.format("Starting DLT Routing Table Application at %s", LocalDateTime.now().toString()));

		SpringApplication springApplication = new SpringApplication(DltRoutingApplication.class);
		springApplication.addListeners(new ApplicationPidFileWriter());

		springApplication.run(args);
	}
}
