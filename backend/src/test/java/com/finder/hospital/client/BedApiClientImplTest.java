package com.finder.hospital.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * safetydata.go.kr는 TLS 핸드셰이크가 간헐적으로 끊기지만 직후 재시도는 대부분 성공한다 (#35).
 * 일시 실패를 재시도로 흡수하고, 연속 실패 시 빈 리스트 fallback이 유지되는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class BedApiClientImplTest {

    private static final String NORMAL_RESPONSE = """
            {
              "header": {"resultCode": "00", "resultMsg": "NORMAL SERVICE"},
              "body": [
                {"BFR_INST_ID": "A1100001", "EMRO": 5},
                {"BFR_INST_ID": "A1100002", "EMRO": 0}
              ]
            }
            """;

    private static final String NO_BODY_RESPONSE = """
            {
              "header": {"resultCode": "99", "resultMsg": "SERVICE ERROR"}
            }
            """;

    @Mock
    private RestTemplate restTemplate;

    private BedApiClientImpl client;

    @BeforeEach
    void setUp() {
        client = new BedApiClientImpl(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "baseUrl", "https://www.safetydata.go.kr/V2/api/DSSP-IF-00242");
        ReflectionTestUtils.setField(client, "retryBackoffMs", 0L);
    }

    @Test
    void 정상응답_병상정보_반환() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(NORMAL_RESPONSE);

        List<BedInfoItem> items = client.getAllBedInfo();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getHpid()).isEqualTo("A1100001");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void 일시적_핸드셰이크_실패후_재시도_성공() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Remote host terminated the handshake"))
                .thenReturn(NORMAL_RESPONSE);

        List<BedInfoItem> items = client.getAllBedInfo();

        assertThat(items).hasSize(2);
        verify(restTemplate, times(2)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void 연속실패시_최대횟수까지_재시도후_빈리스트() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Remote host terminated the handshake"));

        List<BedInfoItem> items = client.getAllBedInfo();

        assertThat(items).isEmpty();
        verify(restTemplate, times(3)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void 응답에_body없으면_재시도없이_빈리스트() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(NO_BODY_RESPONSE);

        List<BedInfoItem> items = client.getAllBedInfo();

        assertThat(items).isEmpty();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void API키_미설정이면_호출없이_빈리스트() {
        ReflectionTestUtils.setField(client, "apiKey", "");

        List<BedInfoItem> items = client.getAllBedInfo();

        assertThat(items).isEmpty();
        verify(restTemplate, times(0)).getForObject(anyString(), eq(String.class));
    }
}
