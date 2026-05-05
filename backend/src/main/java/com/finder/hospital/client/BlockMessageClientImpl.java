package com.finder.hospital.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/** 응급실/중증질환 차단메시지 API 호출 구현체. E-Gen 베이스 URL과 키를 공유한다. */
@Component
@RequiredArgsConstructor
public class BlockMessageClientImpl implements BlockMessageClient {

    private static final Logger log = LoggerFactory.getLogger(BlockMessageClientImpl.class);
    private static final String MESSAGE_PATH = "/getEmrrmSrsillDissMsgInqire";
    private static final int FETCH_SIZE = 500;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egen.api.key}")
    private String apiKey;

    @Value("${egen.api.base-url}")
    private String baseUrl;

    @Override
    public List<BlockMessageItem> getAllMessages() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("E-Gen API 키 미설정");
            return List.of();
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + MESSAGE_PATH)
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", FETCH_SIZE)
                .queryParam("_type", "json")
                .build(true)
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode itemNode = objectMapper.readTree(response).at("/response/body/items/item");

            if (itemNode.isMissingNode() || itemNode.isNull()) return List.of();

            List<BlockMessageItem> items = new ArrayList<>();
            if (itemNode.isArray()) {
                for (JsonNode node : itemNode) {
                    items.add(objectMapper.treeToValue(node, BlockMessageItem.class));
                }
            } else {
                items.add(objectMapper.treeToValue(itemNode, BlockMessageItem.class));
            }
            return items;
        } catch (Exception e) {
            log.error("차단메시지 API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
