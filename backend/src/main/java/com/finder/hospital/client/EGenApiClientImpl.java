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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String SUCCESS_RESULT_CODE = "00";

    private static final Pattern XML_AUTH_MSG_PATTERN = Pattern.compile("<returnAuthMsg>(.*?)</returnAuthMsg>");
    private static final Pattern XML_REASON_CODE_PATTERN = Pattern.compile("<returnReasonCode>(.*?)</returnReasonCode>");

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
            JsonNode body = parseResponseBody(response);
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
        } catch (EGenAuthException e) {
            log.error("응급실 hpid 화이트리스트 조회 실패 - E-Gen API 인증/요청 오류 의심 (서비스 키 확인 필요): {}", e.getMessage());
            return Set.of();
        } catch (Exception e) {
            log.error("응급실 hpid 화이트리스트 조회 실패: {}", e.getMessage());
            return Set.of();
        }
    }

    /** 단건/복수 응답을 모두 처리한다. 호출 실패 시 빈 리스트로 폴백. */
    private List<EGenItem> fetchItems(String url) {
        try {
            return parseItems(restTemplate.getForObject(url, String.class));
        } catch (EGenAuthException e) {
            log.error("E-Gen API 인증/요청 오류 의심 (서비스 키 확인 필요): {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("E-Gen API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /** E-Gen API는 단건일 때 배열 대신 객체로 반환한다. */
    private List<EGenItem> parseItems(String response) throws Exception {
        JsonNode body = parseResponseBody(response);
        JsonNode itemNode = body.path("items").path("item");
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

    /**
     * 응답 본문을 검증하고 `/response/body` 노드를 반환한다.
     * <p>data.go.kr는 서비스 키 미등록·만료·할당량 초과 시 {@code _type=json} 요청을 무시하고
     * {@code <OpenAPI_ServiceResponse>} XML 오류 응답을 돌려주거나, JSON 형식이라도
     * {@code header.resultCode}가 {@code 00}이 아닌 값으로 응답한다. 두 경우 모두
     * "정상 응답이지만 데이터 0건"과 구분되는 {@link EGenAuthException}으로 변환해
     * 호출부에서 원인을 명확히 로그로 남길 수 있게 한다.
     */
    private JsonNode parseResponseBody(String response) throws Exception {
        String trimmed = response == null ? "" : response.strip();
        if (trimmed.startsWith("<")) {
            throw new EGenAuthException(extractXmlAuthMessage(trimmed));
        }

        JsonNode root = objectMapper.readTree(response);
        JsonNode header = root.at("/response/header");
        String resultCode = header.path("resultCode").asText("");
        if (!resultCode.isEmpty() && !SUCCESS_RESULT_CODE.equals(resultCode)) {
            throw new EGenAuthException("resultCode=" + resultCode + ", resultMsg=" + header.path("resultMsg").asText(""));
        }

        return root.at("/response/body");
    }

    /** XML 오류 응답에서 인증 실패 사유(returnAuthMsg, returnReasonCode)를 추출한다. */
    private static String extractXmlAuthMessage(String xml) {
        Matcher authMsg = XML_AUTH_MSG_PATTERN.matcher(xml);
        Matcher reasonCode = XML_REASON_CODE_PATTERN.matcher(xml);
        return "returnAuthMsg=" + (authMsg.find() ? authMsg.group(1) : "UNKNOWN")
                + ", returnReasonCode=" + (reasonCode.find() ? reasonCode.group(1) : "UNKNOWN");
    }

    /** E-Gen API가 정상 JSON 대신 인증/할당량 오류 응답을 반환했음을 나타낸다. */
    private static final class EGenAuthException extends RuntimeException {
        EGenAuthException(String message) {
            super(message);
        }
    }
}
