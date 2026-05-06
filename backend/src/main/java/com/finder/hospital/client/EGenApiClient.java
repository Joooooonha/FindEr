package com.finder.hospital.client;

import java.util.List;
import java.util.Optional;

/** E-Gen 응급의료기관 기본정보 API 클라이언트 */
public interface EGenApiClient {

    /** 위치 기반 응급의료기관 목록을 조회한다. (캐시가 비었을 때 폴백 용도) */
    List<EGenItem> getHospitalsByLocation(double lat, double lng, int numOfRows);

    /** 기관 ID로 응급의료기관 상세 정보를 조회한다. */
    Optional<EGenItem> getHospitalById(String hpid);

    /** 전국 응급의료기관 기본정보를 페이지네이션으로 모두 조회한다. */
    List<EGenItem> getAllHospitals();
}
