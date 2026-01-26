package io.github.drawat123.geo_logistics_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableCaching
public class GeoLogisticsOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoLogisticsOrchestratorApplication.class, args);
    }

}
