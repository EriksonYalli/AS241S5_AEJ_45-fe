package com.example.apiia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("api_logs")
public class ApiLog {
    @Id
    private Long id;
    private String apiName;
    private String requestText;
    private String responseJson;
    private LocalDateTime createdAt;
    private String status;
}
