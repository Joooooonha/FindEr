package com.finder.hospital.client;

import java.util.List;

/** E-Gen 중증질환자 수용가능정보 API 클라이언트 */
public interface SeverePossibilityClient {

    /** 전국 응급의료기관의 시술 가능 정보를 조회한다. */
    List<SeverePossibilityItem> getAllPossibilities();
}
