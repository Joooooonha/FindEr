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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** E-Gen API 실제 호출 구현체 */
@Component
@RequiredArgsConstructor
public class EGenApiClientImpl implements EGenApiClient {

    private static final Logger log = LoggerFactory.getLogger(EGenApiClientImpl.class);
    private static final String LOCATION_PATH = "/getEgytLcinfoInqire";
    private static final String DETAIL_PATH   = "/getEgytBassInfoInqire";
    // 응급실 실시간 가용병상 — 응답이 응급실 운영 기관으로 한정되므로 hpid 화이트리스트 출처로 사용한다.
    private static final String RLTM_PATH     = "/getEmrrmRltmUsefulSckbdInfoInqire";
    private static final int HPID_FETCH_SIZE = 1000;  // 전국 응급실 약 420개 대비 충분.

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

    /**
     * 기본정보 API는 일반 의원·치과까지 10만 건 이상 반환해 응급실 식별이 어렵다.
     * 실시간 가용병상 API는 응급실 운영 기관만 응답하므로 hpid 셋을 화이트리스트로 활용한다.
     */
    @Override
    public Set<String> getEmergencyHpids() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("E-Gen API 키 미설정");
            return Set.of();
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + RLTM_PATH)
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", HPID_FETCH_SIZE)
                .queryParam("_type", "json")
                .build(true)
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode body = objectMapper.readTree(response).at("/response/body");
            JsonNode itemNode = body.path("items").path("item");
            if (itemNode.isMissingNode() || itemNode.isNull()) return Set.of();

            Set<String> hpids = new HashSet<>();
            if (itemNode.isArray()) {
                for (JsonNode node : itemNode) {
                    String hpid = node.path("hpid").asText("");
                    if (!hpid.isBlank()) hpids.add(hpid);
                }
            } else {
                String hpid = itemNode.path("hpid").asText("");
                if (!hpid.isBlank()) hpids.add(hpid);
            }

            int totalCount = body.path("totalCount").asInt(hpids.size());
            if (totalCount > HPID_FETCH_SIZE) {
                log.warn("응급실 hpid 응답이 페이지 한계({}) 초과 (totalCount={}). 일부 응급실 누락 가능",
                        HPID_FETCH_SIZE, totalCount);
            }
            return hpids;
        } catch (Exception e) {
            log.error("응급실 hpid 화이트리스트 조회 실패: {}", e.getMessage());
            return Set.of();
        }
    }

    /** 단건/복수 응답을 모두 처리한다. 호출 실패 시 빈 리스트로 폴백. */
    private List<EGenItem> fetchItems(String url) {
        try {
            return parseItems(restTemplate.getForObject(url, String.class));
        } catch (Exception e) {
            log.error("E-Gen API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /** E-Gen API는 단건일 때 배열 대신 객체로 반환한다. */
    private List<EGenItem> parseItems(String response) throws Exception {
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
    }
}
