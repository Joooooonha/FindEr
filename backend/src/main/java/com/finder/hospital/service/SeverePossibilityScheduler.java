package com.finder.hospital.service;

import com.finder.hospital.client.SeverePossibilityClient;
import com.finder.hospital.client.SeverePossibilityItem;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 중증질환 시술 가능 정보 주기적 폴링. 시술 가능 여부는 거의 변하지 않아 1시간 주기로 충분. */
@Component
@RequiredArgsConstructor
public class SeverePossibilityScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeverePossibilityScheduler.class);

    private final SeverePossibilityClient client;
    private final SeverePossibilityCache cache;

    @Scheduled(fixedDelayString = "${severe.possibility.refresh-interval-ms}")
    public void refresh() {
        List<SeverePossibilityItem> items = client.getAllPossibilities();
        if (items.isEmpty()) {
            log.warn("중증질환 시술 정보 갱신 실패 또는 응답 비어있음. 기존 캐시 유지");
            return;
        }

        // 같은 hpid가 여러 시도에서 중복될 수 있으므로 union으로 병합한다.
        Map<String, Set<String>> grouped = new HashMap<>();
        for (SeverePossibilityItem item : items) {
            if (item.hpid() == null || item.hpid().isBlank()) continue;
            grouped.merge(item.hpid(), item.availableCodes(), (existing, incoming) -> {
                existing.addAll(incoming);
                return existing;
            });
        }
        cache.replaceAll(grouped);
        log.info("중증질환 시술 정보 갱신 완료. 병원 {}개", grouped.size());
    }
}
