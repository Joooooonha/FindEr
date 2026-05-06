package com.finder.hospital.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** 중증질환 시술 가능 정보 메모리 캐시. hpid → 가능 시술 코드 집합. */
@Component
public class SeverePossibilityCache {

    private volatile Map<String, Set<String>> codes = Map.of();

    /** 새 스냅샷으로 통째 교체한다. */
    public void replaceAll(Map<String, Set<String>> newCodes) {
        this.codes = Map.copyOf(newCodes);
    }

    /** 특정 병원의 가용 시술 코드 집합. 데이터 없으면 빈 집합. */
    public Set<String> findCodes(String hpid) {
        return codes.getOrDefault(hpid, Set.of());
    }

    public int size() {
        return codes.size();
    }
}
