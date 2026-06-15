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
 * 중증질환 수용가능정보는 apis.data.go.kr를 17개 시도(STAGE1)로 순회 조회한다.
 * 같은 호스트가 간헐적으로 504/타임아웃을 내므로, 일시 실패를 재시도로 흡수하고
 * mkioskty 코드 파싱(배열/단건/케이싱)이 정확한지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SeverePossibilityClientImplTest {

    private static final String ARRAY_RESPONSE = """
            {
              "response": {
                "body": {
                  "items": {
                    "item": [
                      {"hpid": "A1100001", "mkioskty1": "Y", "mkioskty2": "N", "mkioskty3": "Y"},
                      {"hpid": "A1100002", "mkioskty1": "N"}
                    ]
                  },
                  "totalCount": 2
                }
              }
            }
            """;

    private static final String SINGLE_ITEM_RESPONSE = """
            {
              "response": {
                "body": {
                  "items": {
                    "item": {"hpid": "A1100009", "mkioskty5": "Y"}
                  },
                  "totalCount": 1
                }
              }
            }
            """;

    private static final String UPPER_CASING_RESPONSE = """
            {
              "response": {
                "body": {
                  "items": {
                    "item": [{"hpid": "A1100003", "MKioskTy1": "Y"}]
                  },
                  "totalCount": 1
                }
              }
            }
            """;

    @Mock
    private RestTemplate restTemplate;

    private SeverePossibilityClientImpl client;

    @BeforeEach
    void setUp() {
        client = new SeverePossibilityClientImpl(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "baseUrl", "http://apis.data.go.kr/B552657/ErmctInfoInqireService");
        ReflectionTestUtils.setField(client, "retryBackoffMs", 0L);
    }

    @Test
    void 배열응답에서_Y인_시술코드만_추출한다() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(ARRAY_RESPONSE);

        List<SeverePossibilityItem> items = client.getAllPossibilities();

        assertThat(items).anySatisfy(item -> {
            assertThat(item.hpid()).isEqualTo("A1100001");
            assertThat(item.availableCodes()).contains("mkioskty1", "mkioskty3").doesNotContain("mkioskty2");
        });
    }

    @Test
    void 단건_객체응답도_파싱한다() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(SINGLE_ITEM_RESPONSE);

        List<SeverePossibilityItem> items = client.getAllPossibilities();

        assertThat(items).anySatisfy(item -> {
            assertThat(item.hpid()).isEqualTo("A1100009");
            assertThat(item.availableCodes()).containsExactly("mkioskty5");
        });
    }

    @Test
    void 대문자_케이싱_MKioskTy_필드도_읽는다() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(UPPER_CASING_RESPONSE);

        List<SeverePossibilityItem> items = client.getAllPossibilities();

        assertThat(items).anySatisfy(item -> {
            assertThat(item.hpid()).isEqualTo("A1100003");
            assertThat(item.availableCodes()).contains("mkioskty1");
        });
    }

    @Test
    void 일시적_호출실패는_재시도로_복구된다() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("504 Gateway Timeout"))
                .thenReturn(ARRAY_RESPONSE);

        List<SeverePossibilityItem> items = client.getAllPossibilities();

        assertThat(items).isNotEmpty();
    }

    @Test
    void 연속실패시_재시도후_빈리스트로_흡수한다() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("504 Gateway Timeout"));

        List<SeverePossibilityItem> items = client.getAllPossibilities();

        assertThat(items).isEmpty();
        // 17개 시도(STAGE1)가 각각 최대 3회 재시도를 모두 소진하므로 정확히 17*3회 호출된다.
        // 재시도가 사라지면 17회로 줄어 이 검증이 깨지므로 재시도 동작 자체를 보장한다.
        verify(restTemplate, times(17 * 3)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void API키_미설정이면_호출없이_빈리스트() {
        ReflectionTestUtils.setField(client, "apiKey", "");

        List<SeverePossibilityItem> items = client.getAllPossibilities();

        assertThat(items).isEmpty();
        verify(restTemplate, times(0)).getForObject(anyString(), eq(String.class));
    }
}
