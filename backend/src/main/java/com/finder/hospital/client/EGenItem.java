package com.finder.hospital.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** E-Gen API 기관 기본정보 응답 항목 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EGenItem {

    @JsonProperty("hpid") private String hpid;
    @JsonProperty("dutyName") private String dutyName;
    @JsonProperty("dutyAddr") private String dutyAddr;
    @JsonProperty("dutyTel1") private String dutyTel1;
    @JsonProperty("wgs84Lon") private String wgs84Lon;
    @JsonProperty("wgs84Lat") private String wgs84Lat;
    @JsonProperty("hvs01") private String hvs01;       // 수술실 가용 여부
    @JsonProperty("hvctayn") private String hvctayn;   // CT 가용 여부
    @JsonProperty("hvmriayn") private String hvmriayn; // MRI 가용 여부
}
