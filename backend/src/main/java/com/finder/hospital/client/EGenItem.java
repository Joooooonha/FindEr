package com.finder.hospital.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** E-Gen API 응답 항목. 위치조회(getEgytLcinfoInqire)와 기본정보조회(getEgytBassInfoInqire) 응답을 통합한다. */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EGenItem {

    @JsonProperty("hpid")     private String hpid;
    @JsonProperty("dutyName") private String dutyName;
    @JsonProperty("dutyAddr") private String dutyAddr;
    @JsonProperty("dutyTel1") private String dutyTel1;
    @JsonProperty("dutyTel3") private String dutyTel3;   // 응급실 직통전화 (기본정보 API)

    // 위치조회 API 좌표 필드
    @JsonProperty("latitude")  private String latitude;
    @JsonProperty("longitude") private String longitude;

    // 기본정보 API 좌표 필드
    @JsonProperty("wgs84Lat") private String wgs84Lat;
    @JsonProperty("wgs84Lon") private String wgs84Lon;

    // 기본정보 API 가용 장비 여부 (Y/N)
    @JsonProperty("hvctayn")  private String hvctayn;   // CT 가용
    @JsonProperty("hvmriayn") private String hvmriayn;  // MRI 가용
    @JsonProperty("hvventiayn") private String hvventiayn; // 인공호흡기 가용
    @JsonProperty("hpopyn")   private String hpopyn;    // 수술실 수 (0이면 불가)

    /** 위치조회/기본정보 API 모두 처리하는 위도 반환 */
    public String getLatCoord() {
        return wgs84Lat != null ? wgs84Lat : latitude;
    }

    /** 위치조회/기본정보 API 모두 처리하는 경도 반환 */
    public String getLngCoord() {
        return wgs84Lon != null ? wgs84Lon : longitude;
    }

    /** 응급실 직통전화 우선, 없으면 대표전화 반환 */
    public String getContactPhone() {
        return dutyTel3 != null ? dutyTel3 : dutyTel1;
    }
}
