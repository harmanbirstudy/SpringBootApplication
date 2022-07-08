package com.webapp.shoppingwebsite;

//exclude = {DataSourceAutoConfiguration.class .. I dont remember why I added this one
import com.webapp.shoppingwebsite.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EnableConfigurationProperties(AppProperties.class)
public class ShoppingwebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingwebsiteApplication.class, args);
	}

}
