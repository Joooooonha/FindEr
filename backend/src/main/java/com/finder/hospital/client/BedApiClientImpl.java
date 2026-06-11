package com.finder.hospital.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/** safetydata.go.kr 실시간 병상정보 API 호출 구현체 */
@Component
@RequiredArgsConstructor
public class BedApiClientImpl implements BedApiClient {

    private static final Logger log = LoggerFactory.getLogger(BedApiClientImpl.class);
    private static final int FETCH_SIZE = 500;  // 전국 응급의료기관 약 425개
    // safetydata.go.kr는 TLS 핸드셰이크가 간헐적으로 끊기는데 직후 재시도는 대부분 성공한다 (#35).
    private static final int MAX_ATTEMPTS = 3;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /** 재시도 대기 기준(ms). 시도 횟수에 비례해 증가. 테스트에서 0으로 줄인다. */
    private long retryBackoffMs = 1000;

    @Value("${bed.api.key}")
    private String apiKey;

    @Value("${bed.api.base-url}")
    private String baseUrl;

    @Override
    public List<BedInfoItem> getAllBedInfo() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("실시간 병상정보 API 키 미설정");
            return List.of();
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", FETCH_SIZE)
                .queryParam("returnType", "json")
                .build(true)
                .toUriString();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                JsonNode body = root.get("body");

                if (body == null || !body.isArray()) {
                    log.warn("실시간 병상정보 API 응답 비정상: {}", root.path("header"));
                    return List.of();
                }

                return objectMapper.convertValue(body, new TypeReference<List<BedInfoItem>>() {});
            } catch (Exception e) {
                if (attempt == MAX_ATTEMPTS) {
                    log.error("실시간 병상정보 API 호출 실패 ({}회 시도): {}", MAX_ATTEMPTS, e.getMessage());
                    return List.of();
                }
                log.warn("실시간 병상정보 API 호출 실패 (시도 {}/{}): {}. 재시도", attempt, MAX_ATTEMPTS, e.getMessage());
                if (!backoff(attempt)) return List.of();
            }
        }
        return List.of();
    }

    /** 시도 횟수에 비례해 대기한다. 인터럽트되면 false를 반환해 호출을 중단시킨다. */
    private boolean backoff(int attempt) {
        try {
            Thread.sleep(retryBackoffMs * attempt);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
