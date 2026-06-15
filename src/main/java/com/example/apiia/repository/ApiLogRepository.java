package com.example.apiia.repository;

import com.example.apiia.model.ApiLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ApiLogRepository extends ReactiveCrudRepository<ApiLog, Long> {
    Flux<ApiLog> findAllByStatus(String status);
}
