package com.finder.hospital.client;

import java.util.List;

/** 실시간 병상정보 API 클라이언트 */
public interface BedApiClient {

    /** 전국 모든 응급의료기관 실시간 병상정보를 조회한다. */
    List<BedInfoItem> getAllBedInfo();
}
