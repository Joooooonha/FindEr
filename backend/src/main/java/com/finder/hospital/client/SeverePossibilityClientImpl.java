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
import java.util.Set;

/** E-Gen 중증질환자 수용가능정보 API 호출 구현체. 시도 단위로 반복 조회 후 누적 반환. */
@Component
@RequiredArgsConstructor
public class SeverePossibilityClientImpl implements SeverePossibilityClient {

    private static final Logger log = LoggerFactory.getLogger(SeverePossibilityClientImpl.class);
    private static final String PATH = "/getSrsillDissAceptncPosblInfoInqire";
    private static final int FETCH_SIZE = 500;
    private static final int TREATMENT_COUNT = 28;

    private static final List<String> STAGE1_LIST = List.of(
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
            "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도",
            "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도",
            "경상남도", "제주특별자치도"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${egen.api.key}")
    private String apiKey;

    @Value("${egen.api.base-url}")
    private String baseUrl;

    @Override
    public List<SeverePossibilityItem> getAllPossibilities() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("E-Gen API 키 미설정");
            return List.of();
        }

        List<SeverePossibilityItem> all = new ArrayList<>();
        for (String stage1 : STAGE1_LIST) {
            all.addAll(fetchByStage1(stage1));
        }
        return all;
    }

    private List<SeverePossibilityItem> fetchByStage1(String stage1) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + PATH)
                .queryParam("serviceKey", apiKey)
                .queryParam("STAGE1", stage1)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", FETCH_SIZE)
                .queryParam("_type", "json")
                .build()
                .encode()
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode itemNode = objectMapper.readTree(response).at("/response/body/items/item");
            if (itemNode.isMissingNode() || itemNode.isNull()) return List.of();

            List<SeverePossibilityItem> items = new ArrayList<>();
            if (itemNode.isArray()) {
                for (JsonNode node : itemNode) {
                    items.add(toItem(node));
                }
            } else {
                items.add(toItem(itemNode));
            }
            return items;
        } catch (Exception e) {
            log.warn("중증질환 API 호출 실패 ({}): {}", stage1, e.getMessage());
            return List.of();
        }
    }

    /** mkioskty1~28 필드를 검사해 'Y' 값만 가용 코드 집합에 담는다. */
    private SeverePossibilityItem toItem(JsonNode node) {
        String hpid = textOrEmpty(node, "hpid");
        Set<String> codes = new HashSet<>();
        for (int i = 1; i <= TREATMENT_COUNT; i++) {
            String value = readMkiosktyValue(node, i);
            if ("Y".equalsIgnoreCase(value.trim())) {
                codes.add("mkioskty" + i);
            }
        }
        return new SeverePossibilityItem(hpid, codes);
    }

    /** API 응답이 mkiosktyN과 MKioskTyN 두 가지 케이싱을 모두 사용하므로 둘 다 시도한다. */
    private String readMkiosktyValue(JsonNode node, int idx) {
        JsonNode field = node.get("mkioskty" + idx);
        if (field == null || field.isNull()) field = node.get("MKioskTy" + idx);
        return field != null ? field.asText("") : "";
    }

    private String textOrEmpty(JsonNode node, String key) {
        JsonNode field = node.get(key);
        return field != null ? field.asText("") : "";
    }
}
