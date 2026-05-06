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
import java.util.Optional;

/** E-Gen API 실제 호출 구현체 */
@Component
@RequiredArgsConstructor
public class EGenApiClientImpl implements EGenApiClient {

    private static final Logger log = LoggerFactory.getLogger(EGenApiClientImpl.class);
    private static final String LOCATION_PATH = "/getEgytLcinfoInqire";
    private static final String DETAIL_PATH   = "/getEgytBassInfoInqire";
    private static final int BULK_PAGE_SIZE = 200;
    private static final int BULK_MAX_PAGES = 20;  // 200 × 20 = 4000건. 응급의료기관 약 530개 대비 충분.

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egen.api.key}")
    private String apiKey;

    @Value("${egen.api.base-url}")
    private String baseUrl;

    @Override
    public List<EGenItem> getHospitalsByLocation(double lat, double lng, int numOfRows) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + LOCATION_PATH)
                .queryParam("serviceKey", apiKey)
                .queryParam("WGS84_LAT", lat)
                .queryParam("WGS84_LON", lng)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", "json")
                .build(true)
                .toUriString();

        return fetchItems(url);
    }

    @Override
    public Optional<EGenItem> getHospitalById(String hpid) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + DETAIL_PATH)
                .queryParam("serviceKey", apiKey)
                .queryParam("HPID", hpid)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .queryParam("_type", "json")
                .build(true)
                .toUriString();

        List<EGenItem> items = fetchItems(url);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    @Override
    public List<EGenItem> getAllHospitals() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("E-Gen API 키 미설정");
            return List.of();
        }

        List<EGenItem> all = new ArrayList<>();
        for (int page = 1; page <= BULK_MAX_PAGES; page++) {
            List<EGenItem> pageItems = fetchPage(page);
            if (pageItems.isEmpty()) break;
            all.addAll(pageItems);
            if (pageItems.size() < BULK_PAGE_SIZE) break;
        }
        return all;
    }

    private List<EGenItem> fetchPage(int pageNo) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + DETAIL_PATH)
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", BULK_PAGE_SIZE)
                .queryParam("_type", "json")
                .build(true)
                .toUriString();
        return fetchItems(url);
    }

    /** 단건/복수 응답을 모두 처리한다. E-Gen API는 단건일 때 배열 대신 객체로 반환한다. */
    private List<EGenItem> fetchItems(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode itemNode = objectMapper.readTree(response).at("/response/body/items/item");

            if (itemNode.isMissingNode() || itemNode.isNull()) return List.of();

            List<EGenItem> items = new ArrayList<>();
            if (itemNode.isArray()) {
                for (JsonNode node : itemNode) {
                    items.add(objectMapper.treeToValue(node, EGenItem.class));
                }
            } else {
                items.add(objectMapper.treeToValue(itemNode, EGenItem.class));
            }
            return items;
        } catch (Exception e) {
            log.error("E-Gen API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
