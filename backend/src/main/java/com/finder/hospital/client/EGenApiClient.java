package com.finder.hospital.client;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** E-Gen 응급의료기관 기본정보 API 클라이언트 */
public interface EGenApiClient {

    /** 위치 기반 응급의료기관 목록을 조회한다. (캐시가 비었을 때 폴백 용도) */
    List<EGenItem> getHospitalsByLocation(double lat, double lng, int numOfRows);

    /** 기관 ID로 응급의료기관 상세 정보를 조회한다. */
    Optional<EGenItem> getHospitalById(String hpid);

    /** 실시간 가용병상 API로 전국 응급실 운영 기관의 hpid 화이트리스트를 조회한다. */
    Set<String> getEmergencyHpids();
}
