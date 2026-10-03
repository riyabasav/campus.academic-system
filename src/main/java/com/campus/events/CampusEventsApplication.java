package com.campus.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CampusEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusEventsApplication.class, args);
    }
}
