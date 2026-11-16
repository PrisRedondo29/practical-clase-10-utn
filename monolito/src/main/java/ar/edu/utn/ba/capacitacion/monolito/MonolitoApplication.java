package ar.edu.utn.ba.capacitacion.monolito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonolitoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolitoApplication.class, args);
    }
}

