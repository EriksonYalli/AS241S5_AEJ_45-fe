package com.example.apiia.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final DatabaseClient databaseClient;

    public DatabaseInitializer(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            String createTableSql = "CREATE TABLE IF NOT EXISTS api_logs (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "api_name VARCHAR(50) NOT NULL," +
                    "request_text CLOB NOT NULL," +
                    "response_json CLOB," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "status CHAR(1) NOT NULL DEFAULT 'A'" +
                    ")";

            databaseClient.sql(createTableSql)
                    .fetch()
                    .rowsUpdated()
                    .doOnSuccess(count -> log.info("Database table initialized successfully"))
                    .onErrorResume(error -> {
                        log.warn("Table may already exist: {}", error.getMessage());
                        return Mono.just(0);
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error during database initialization (continuing anyway): {}", e.getMessage());
        }
    }
}
