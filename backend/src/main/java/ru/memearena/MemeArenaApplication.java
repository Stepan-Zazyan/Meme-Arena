package ru.memearena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MemeArenaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemeArenaApplication.class, args);
    }
}
