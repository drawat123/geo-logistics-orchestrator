package io.github.drawat123.geo_logistics_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GeoLogisticsOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoLogisticsOrchestratorApplication.class, args);
    }

}
