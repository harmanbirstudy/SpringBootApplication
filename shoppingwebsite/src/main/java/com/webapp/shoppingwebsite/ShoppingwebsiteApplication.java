package com.webapp.shoppingwebsite;

import com.webapp.shoppingwebsite.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ShoppingwebsiteApplication {

	private static final Logger logger = LoggerFactory.getLogger(ShoppingwebsiteApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ShoppingwebsiteApplication.class, args);
		logger.info("Shopping website application started");
	}

}
