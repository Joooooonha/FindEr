package com.finder.hospital.service;

import com.finder.hospital.client.BedApiClient;
import com.finder.hospital.client.BedInfoItem;
import com.finder.hospital.domain.BedSnapshot;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 실시간 병상정보 주기적 폴링 스케줄러. */
@Component
@RequiredArgsConstructor
public class BedInfoScheduler {

    private static final Logger log = LoggerFactory.getLogger(BedInfoScheduler.class);

    private final BedApiClient bedApiClient;
    private final BedInfoCache cache;

    /** 애플리케이션 시작 직후 1회 즉시 갱신. */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        refresh();
    }

    /** 설정된 주기로 캐시 갱신. */
    @Scheduled(fixedDelayString = "${bed.api.refresh-interval-ms}")
    public void refresh() {
        List<BedInfoItem> items = bedApiClient.getAllBedInfo();
        if (items.isEmpty()) {
            log.warn("실시간 병상정보 갱신 실패. 기존 캐시 유지");
            return;
        }

        Map<String, BedSnapshot> snapshots = new HashMap<>();
        for (BedInfoItem item : items) {
            if (item.getHpid() == null) continue;
            snapshots.put(item.getHpid(), BedSnapshot.from(item));
        }
        cache.replaceAll(snapshots);
        log.info("실시간 병상정보 갱신 완료. 병원 {}개", snapshots.size());
    }
}
