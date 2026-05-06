package com.finder.hospital.service;

import com.finder.hospital.domain.HospitalInfo;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 응급의료기관 마스터 캐시. hpid → 기본정보 매핑. 사용자 위치 조회의 단일 소스. */
@Component
public class HospitalInfoCache {

    private volatile Map<String, HospitalInfo> infos = Map.of();

    /** 새 스냅샷으로 통째 교체한다. */
    public void replaceAll(Map<String, HospitalInfo> newInfos) {
        this.infos = Map.copyOf(newInfos);
    }

    public Optional<HospitalInfo> find(String hpid) {
        return Optional.ofNullable(infos.get(hpid));
    }

    /** 캐시에 보관된 모든 병원 정보를 반환한다. 거리 기반 조회의 입력으로 사용. */
    public Collection<HospitalInfo> all() {
        return List.copyOf(infos.values());
    }

    public int size() {
        return infos.size();
    }

    public boolean isEmpty() {
        return infos.isEmpty();
    }
}
