package com.finder.hospital.service;

import com.finder.hospital.domain.BedSnapshot;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 실시간 병상정보 메모리 캐시. hpid → 병상 스냅샷 매핑. */
@Component
public class BedInfoCache {

    private volatile Map<String, BedSnapshot> snapshots = Map.of();

    /** 새 스냅샷으로 통째로 교체한다. 부분 업데이트보다 단순하고 정합성 안전. */
    public void replaceAll(Map<String, BedSnapshot> newSnapshots) {
        this.snapshots = Map.copyOf(newSnapshots);
    }

    public Optional<BedSnapshot> find(String hpid) {
        return Optional.ofNullable(snapshots.get(hpid));
    }

    public int size() {
        return snapshots.size();
    }
}
