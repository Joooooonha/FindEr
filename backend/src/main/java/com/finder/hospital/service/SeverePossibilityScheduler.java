package com.finder.hospital.service;

import com.finder.hospital.client.SeverePossibilityClient;
import com.finder.hospital.client.SeverePossibilityItem;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** 중증질환 시술 가능 정보 주기적 폴링. 시술 가능 여부는 거의 변하지 않아 1시간 주기로 충분. */
@Component
@RequiredArgsConstructor
public class SeverePossibilityScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeverePossibilityScheduler.class);

    private final SeverePossibilityClient client;
    private final SeverePossibilityCache cache;

    // 정기 갱신과 콜드 재시도가 같은 갱신을 동시에 수행하지 않도록 막는다.
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    /** 정기 갱신. 시술 가능 정보는 변동이 드물어 긴 주기로 폴링한다. */
    @Scheduled(fixedDelayString = "${severe.possibility.refresh-interval-ms}")
    public void refresh() {
        doRefresh();
    }

    /**
     * 콜드 스타트 복구용 재시도. 캐시가 비어 있을 때만 짧은 주기로 갱신을 시도한다.
     * 캐시가 채워지면 즉시 빠져나가는 저비용 점검이라 정상 운영 중에는 부하가 없다.
     * 1시간 주기 정기 갱신만으로는 재시작 직후 첫 호출이 실패하면 최대 1시간 동안 증상 데이터가 비어
     * 증상 필터가 모든 병원을 걸러내므로, 빈 동안만 빠르게 재시도해 공백을 줄인다.
     */
    @Scheduled(fixedDelayString = "${severe.possibility.cold-retry-interval-ms}")
    public void retryWhileEmpty() {
        if (cache.size() > 0) return;
        doRefresh();
    }

    private void doRefresh() {
        if (!refreshing.compareAndSet(false, true)) return;
        try {
            List<SeverePossibilityItem> items = client.getAllPossibilities();
            if (items.isEmpty()) {
                if (cache.size() == 0) {
                    log.error("중증질환 시술 정보 비어있음 - 콜드 캐시 상태 (증상 필터 비활성). 다음 재시도 대기");
                } else {
                    log.warn("중증질환 시술 정보 갱신 실패 또는 응답 비어있음. 기존 캐시 유지");
                }
                return;
            }

            // 같은 hpid가 여러 시도에서 중복될 수 있으므로 union으로 병합한다.
            // record가 반환하는 불변 Set을 직접 변형하지 않도록 가변 사본을 만들어 누적한다.
            Map<String, Set<String>> grouped = new HashMap<>();
            for (SeverePossibilityItem item : items) {
                if (item.hpid() == null || item.hpid().isBlank()) continue;
                grouped.computeIfAbsent(item.hpid(), k -> new HashSet<>())
                        .addAll(item.availableCodes());
            }
            cache.replaceAll(grouped);
            log.info("중증질환 시술 정보 갱신 완료. 병원 {}개", grouped.size());
        } finally {
            refreshing.set(false);
        }
    }
}
