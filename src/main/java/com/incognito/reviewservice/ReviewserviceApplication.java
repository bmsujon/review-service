package com.incognito.reviewservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(
		info = @Info(
				title = "Review Service API",
				version = "v1",
				description = "This API provides endpoints for managing reviews and comments.",
				contact = @Contact(
						name = "Md. Wahidul Azam",
						email = "bmsujon@gmail.com",
						url = "https://www.linkedin.com/in/md-wahidul-azam-6614a724/"
				),
				license = @License(
						name = "Apache 2.0",
						url = "https://www.apache.org/licenses/LICENSE-2.0.html"
				)
		)
)
@SpringBootApplication
public class ReviewserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewserviceApplication.class, args);
	}

}
