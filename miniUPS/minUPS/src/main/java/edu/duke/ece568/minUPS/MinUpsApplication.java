package edu.duke.ece568.minUPS;

import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MinUpsApplication{

	//private static Logger LOG =  LoggerFactory.getLogger(MinUpsApplication.class);
	public static void main(String[] args) {
		//LOG.info("\nSTARTING : Spring boot application starting");
		SpringApplication.run(MinUpsApplication.class, args);
		//LOG.info("\nSTOPPED  : Spring boot application stopped");
	}


}
