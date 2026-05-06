package com.finder.hospital.service;

import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 중증질환 시술 가능 정보 메모리 캐시. hpid → 가능 시술 코드 집합. */
@Component
public class SeverePossibilityCache {

    private volatile Map<String, Set<String>> codes = Map.of();

    /** 새 스냅샷으로 통째 교체한다. 외부 변경이 캐시로 전파되지 않도록 키·값 모두 불변 사본으로 저장. */
    public void replaceAll(Map<String, Set<String>> newCodes) {
        this.codes = newCodes.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), Set.copyOf(e.getValue())))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** 특정 병원의 가용 시술 코드 집합. 데이터 없으면 빈 집합. */
    public Set<String> findCodes(String hpid) {
        return codes.getOrDefault(hpid, Set.of());
    }

    public int size() {
        return codes.size();
    }
}
