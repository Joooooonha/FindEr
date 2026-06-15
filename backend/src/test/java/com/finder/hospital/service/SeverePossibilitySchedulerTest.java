package com.finder.hospital.service;

import com.finder.hospital.client.SeverePossibilityClient;
import com.finder.hospital.client.SeverePossibilityItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 콜드 스타트 재시도는 캐시가 비어 있을 때만 동작하고, 채워져 있으면 외부 호출 없이 빠져나가는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SeverePossibilitySchedulerTest {

    @Mock
    private SeverePossibilityClient client;

    @Mock
    private SeverePossibilityCache cache;

    @InjectMocks
    private SeverePossibilityScheduler scheduler;

    @Test
    void 캐시가_채워져있으면_콜드재시도는_외부호출을_하지_않는다() {
        when(cache.size()).thenReturn(427);

        scheduler.retryWhileEmpty();

        verifyNoInteractions(client);
        verify(cache, never()).replaceAll(any());
    }

    @Test
    void 캐시가_비어있으면_콜드재시도가_갱신을_시도한다() {
        when(cache.size()).thenReturn(0);
        when(client.getAllPossibilities())
                .thenReturn(List.of(new SeverePossibilityItem("A1100001", Set.of("mkioskty1"))));

        scheduler.retryWhileEmpty();

        verify(cache).replaceAll(argThat(grouped ->
                grouped.containsKey("A1100001") && grouped.get("A1100001").contains("mkioskty1")));
    }

    @Test
    void 정기갱신은_받은_시술코드를_hpid별로_병합해_캐시에_적재한다() {
        when(client.getAllPossibilities()).thenReturn(List.of(
                new SeverePossibilityItem("A1100001", Set.of("mkioskty1")),
                new SeverePossibilityItem("A1100001", Set.of("mkioskty3")),
                new SeverePossibilityItem("A1100002", Set.of("mkioskty5"))
        ));

        scheduler.refresh();

        verify(cache).replaceAll(argThat((Map<String, Set<String>> grouped) ->
                grouped.get("A1100001").containsAll(Set.of("mkioskty1", "mkioskty3"))
                        && grouped.get("A1100002").contains("mkioskty5")));
    }

    @Test
    void 응답이_비어있으면_캐시를_교체하지_않는다() {
        when(client.getAllPossibilities()).thenReturn(List.of());
        when(cache.size()).thenReturn(0);

        scheduler.refresh();

        verify(cache, never()).replaceAll(any());
    }
}
