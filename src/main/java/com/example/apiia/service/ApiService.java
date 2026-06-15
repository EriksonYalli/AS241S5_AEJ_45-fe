package com.example.apiia.service;

import com.example.apiia.model.ApiLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ApiService {

    private final WebClient webClient;
    private final DatabaseClient databaseClient;

    @Value("${ai-api.textgears.url}")
    private String textGearsUrl;

    @Value("${ai-api.textgears.host}")
    private String textGearsHost;

    @Value("${ai-api.textgears.key}")
    private String textGearsKey;

    @Value("${ai-api.copilot.url}")
    private String copilotUrl;

    @Value("${ai-api.copilot.host}")
    private String copilotHost;

    @Value("${ai-api.copilot.key}")
    private String copilotKey;

    public ApiService(WebClient.Builder webClientBuilder, DatabaseClient databaseClient) {
        this.webClient = webClientBuilder.build();
        this.databaseClient = databaseClient;
    }

    public Mono<String> callTextGears(String text) {
        return webClient.post()
                .uri(textGearsUrl)
                .header("X-RapidAPI-Key", textGearsKey)
                .header("X-RapidAPI-Host", textGearsHost)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("text", text)
                        .with("language", "en-US"))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> logAndReturn("TextGears", text, response))
                .onErrorResume(error -> {
                    log.error("Error calling TextGears API: {}", error.getMessage());
                    String errorResponse = "{\"error\": \"API call failed: " + error.getMessage() + "\"}";
                    return logAndReturn("TextGears", text, errorResponse);
                });
    }

    public Mono<String> callCopilot(String text) {
        return webClient.post()
                .uri(copilotUrl)
                .header("Content-Type", "application/json")
                .header("X-RapidAPI-Key", copilotKey)
                .header("X-RapidAPI-Host", copilotHost)
                .body(BodyInserters.fromValue(new java.util.HashMap<String, Object>() {
                    {
                        put("message", text);
                        put("conversation_id", null);
                        put("mode", "CHAT");
                        put("markdown", true);
                    }
                }))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> logAndReturn("Copilot", text, response))
                .onErrorResume(error -> {
                    log.error("Error calling Copilot API: {}", error.getMessage());
                    String errorResponse = "{\"error\": \"API call failed: " + error.getMessage() + "\"}";
                    return logAndReturn("Copilot", text, errorResponse);
                });
    }

    public Flux<ApiLog> getAllLogs() {
        return databaseClient.sql("SELECT id, api_name, request_text, response_json, created_at, status FROM api_logs WHERE status = 'A' ORDER BY created_at DESC")
                .map((row, metadata) -> {
                    ApiLog log = new ApiLog();
                    log.setId(row.get("id", Long.class));
                    log.setApiName(row.get("api_name", String.class));
                    log.setRequestText(row.get("request_text", String.class));
                    log.setResponseJson(row.get("response_json", String.class));
                    log.setCreatedAt(row.get("created_at", java.time.LocalDateTime.class));
                    log.setStatus(row.get("status", String.class));
                    return log;
                })
                .all();
    }

    public Mono<ApiLog> updateLog(Long id, String requestText) {
        return databaseClient.sql("UPDATE api_logs SET request_text = :requestText WHERE id = :id AND status = 'A' RETURNING id, api_name, request_text, response_json, created_at, status")
                .bind("requestText", requestText)
                .bind("id", id)
                .map((row, metadata) -> {
                    ApiLog log = new ApiLog();
                    log.setId(row.get("id", Long.class));
                    log.setApiName(row.get("api_name", String.class));
                    log.setRequestText(row.get("request_text", String.class));
                    log.setResponseJson(row.get("response_json", String.class));
                    log.setCreatedAt(row.get("created_at", java.time.LocalDateTime.class));
                    log.setStatus(row.get("status", String.class));
                    return log;
                })
                .one();
    }

    public Mono<Void> deleteLog(Long id) {
        String sql = "UPDATE api_logs SET status = 'I' WHERE id = " + id;
        return databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<String> logAndReturn(String apiName, String requestText, String responseJson) {
        LocalDateTime createdAt = LocalDateTime.now();

        return databaseClient.sql("INSERT INTO api_logs (api_name, request_text, response_json, created_at, status) VALUES (:apiName, :requestText, :responseJson, :createdAt, :status)")
                .bind("apiName", apiName)
                .bind("requestText", requestText)
                .bind("responseJson", Json.of(responseJson))
                .bind("createdAt", createdAt)
                .bind("status", "A")
                .fetch()
                .rowsUpdated()
                .doOnSuccess(count -> log.info("Logged {} request to database", apiName))
                .thenReturn(responseJson);
    }
}
