package com.example.apiia.controller;

import com.example.apiia.model.ApiLog;
import com.example.apiia.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiController {

    private final ApiService apiService;

    @PostMapping("/textgears")
    public Mono<String> textgears(@RequestBody RequestDto request) {
        return apiService.callTextGears(request.getText());
    }

    @PostMapping("/copilot")
    public Mono<String> copilot(@RequestBody RequestDto request) {
        return apiService.callCopilot(request.getText());
    }

    @GetMapping("/logs")
    public Flux<ApiLog> getLogs() {
        return apiService.getAllLogs();
    }

    @PutMapping("/logs/{id}")
    public Mono<ApiLog> updateLog(@PathVariable Long id, @RequestBody UpdateLogDto request) {
        return apiService.updateLog(id, request.getRequestText());
    }

    @DeleteMapping("/logs/{id}")
    public Mono<Void> deleteLog(@PathVariable Long id) {
        return apiService.deleteLog(id);
    }

    public static class RequestDto {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class UpdateLogDto {
        private String requestText;

        public String getRequestText() {
            return requestText;
        }

        public void setRequestText(String requestText) {
            this.requestText = requestText;
        }
    }
}
