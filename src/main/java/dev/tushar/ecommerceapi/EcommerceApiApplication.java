package dev.tushar.ecommerceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApiApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(EcommerceApiApplication.class);
        SpringApplication.run(EcommerceApiApplication.class, args);

    }
}
