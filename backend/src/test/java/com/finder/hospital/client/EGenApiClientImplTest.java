package com.finder.hospital.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * data.go.kr는 서비스 키 미등록/만료/할당량 초과 시 {@code _type=json}을 무시하고 XML 오류를 반환하거나,
 * JSON이라도 {@code header.resultCode}가 {@code 00}이 아닌 값으로 응답한다.
 * 이 두 경우가 "정상 응답이지만 데이터 0건"과 구분되어 로그로 남는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class EGenApiClientImplTest {

    private static final String NORMAL_LOCATION_RESPONSE = """
            {
              "response": {
                "header": {"resultCode": "00", "resultMsg": "NORMAL_SERVICE"},
                "body": {
                  "items": {
                    "item": [
                      {"hpid": "A1100001", "dutyName": "서울병원", "wgs84Lat": "37.50", "wgs84Lon": "127.10"},
                      {"hpid": "A1100002", "dutyName": "분당병원", "wgs84Lat": "37.40", "wgs84Lon": "127.12"}
                    ]
                  },
                  "totalCount": 2
                }
              }
            }
            """;

    private static final String NORMAL_SINGLE_ITEM_RESPONSE = """
            {
              "response": {
                "header": {"resultCode": "00", "resultMsg": "NORMAL_SERVICE"},
                "body": {
                  "items": {
                    "item": {"hpid": "A1100001", "dutyName": "서울병원", "wgs84Lat": "37.50", "wgs84Lon": "127.10"}
                  },
                  "totalCount": 1
                }
              }
            }
            """;

    private static final String JSON_AUTH_ERROR_RESPONSE = """
            {
              "response": {
                "header": {"resultCode": "30", "resultMsg": "SERVICE_KEY_IS_NOT_REGISTERED_ERROR"}
              }
            }
            """;

    private static final String XML_AUTH_ERROR_RESPONSE = """
            <OpenAPI_ServiceResponse>
                <cmmMsgHeader>
                    <errMsg>SERVICE ERROR</errMsg>
                    <returnAuthMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</returnAuthMsg>
                    <returnReasonCode>30</returnReasonCode>
                </cmmMsgHeader>
            </OpenAPI_ServiceResponse>
            """;

    private static final String EMPTY_ITEMS_RESPONSE = """
            {
              "response": {
                "header": {"resultCode": "00", "resultMsg": "NORMAL_SERVICE"},
                "body": {"totalCount": 0}
              }
            }
            """;

    @Mock
    private RestTemplate restTemplate;

    private EGenApiClientImpl client;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        client = new EGenApiClientImpl(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "baseUrl", "http://apis.data.go.kr/B552657/ErmctInfoInqireService");

        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(EGenApiClientImpl.class)).addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(EGenApiClientImpl.class)).detachAppender(logAppender);
    }

    @Test
    void getHospitalsByLocation_정상응답_배열_파싱() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(NORMAL_LOCATION_RESPONSE);

        List<EGenItem> items = client.getHospitalsByLocation(37.45, 127.11, 100);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getHpid()).isEqualTo("A1100001");
        assertThat(items.get(1).getHpid()).isEqualTo("A1100002");
    }

    @Test
    void getHospitalById_정상응답_단건객체_파싱() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(NORMAL_SINGLE_ITEM_RESPONSE);

        Optional<EGenItem> item = client.getHospitalById("A1100001");

        assertThat(item).isPresent();
        assertThat(item.get().getHpid()).isEqualTo("A1100001");
    }

    @Test
    void getHospitalsByLocation_정상응답_데이터없음_빈리스트() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(EMPTY_ITEMS_RESPONSE);

        List<EGenItem> items = client.getHospitalsByLocation(37.45, 127.11, 100);

        assertThat(items).isEmpty();
        assertThat(errorLogs()).noneMatch(msg -> msg.contains("서비스 키 확인 필요"));
    }

    @Test
    void getHospitalsByLocation_JSON_resultCode_오류_빈리스트와_인증오류로그() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(JSON_AUTH_ERROR_RESPONSE);

        List<EGenItem> items = client.getHospitalsByLocation(37.45, 127.11, 100);

        assertThat(items).isEmpty();
        assertThat(errorLogs())
                .anyMatch(msg -> msg.contains("서비스 키 확인 필요") && msg.contains("resultCode=30"));
    }

    @Test
    void getHospitalsByLocation_XML_오류응답_빈리스트와_인증오류로그() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(XML_AUTH_ERROR_RESPONSE);

        List<EGenItem> items = client.getHospitalsByLocation(37.45, 127.11, 100);

        assertThat(items).isEmpty();
        assertThat(errorLogs())
                .anyMatch(msg -> msg.contains("서비스 키 확인 필요")
                        && msg.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")
                        && msg.contains("returnReasonCode=30"));
    }

    @Test
    void getEmergencyHpids_정상응답_hpid셋_반환() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(NORMAL_LOCATION_RESPONSE);

        Set<String> hpids = client.getEmergencyHpids();

        assertThat(hpids).containsExactlyInAnyOrder("A1100001", "A1100002");
    }

    @Test
    void getEmergencyHpids_JSON_resultCode_오류_빈셋과_인증오류로그() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(JSON_AUTH_ERROR_RESPONSE);

        Set<String> hpids = client.getEmergencyHpids();

        assertThat(hpids).isEmpty();
        assertThat(errorLogs())
                .anyMatch(msg -> msg.contains("서비스 키 확인 필요") && msg.contains("resultCode=30"));
    }

    @Test
    void getEmergencyHpids_XML_오류응답_빈셋과_인증오류로그() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(XML_AUTH_ERROR_RESPONSE);

        Set<String> hpids = client.getEmergencyHpids();

        assertThat(hpids).isEmpty();
        assertThat(errorLogs())
                .anyMatch(msg -> msg.contains("서비스 키 확인 필요") && msg.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR"));
    }

    @Test
    void getEmergencyHpids_API_키_미설정_빈셋() {
        ReflectionTestUtils.setField(client, "apiKey", "");

        Set<String> hpids = client.getEmergencyHpids();

        assertThat(hpids).isEmpty();
    }

    private List<String> errorLogs() {
        return logAppender.list.stream()
                .filter(e -> e.getLevel() == Level.ERROR)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }
}
