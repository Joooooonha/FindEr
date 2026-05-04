package com.finder.hospital.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** safetydata.go.kr 실시간 병상정보 API 응답 항목 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BedInfoItem {

    @JsonProperty("BFR_INST_ID") private String hpid;       // E-Gen hpid와 동일 형식
    @JsonProperty("EMRO")        private Integer emro;       // 응급실 가용 병상 수
    @JsonProperty("OPRO")        private Integer opro;       // 수술실 가용
    @JsonProperty("WARD")        private Integer ward;       // 입원실
    @JsonProperty("GNRL_ICU")    private Integer gnrlIcu;    // 일반중환자실
    @JsonProperty("CT_AVBL_YN")  private String ctAvailable;
    @JsonProperty("MRI_AVBL_YN") private String mriAvailable;
    @JsonProperty("VENT_AVBL_YN") private String ventAvailable;
    @JsonProperty("MDFCN_DT")    private String modifiedAt;  // "2026-05-04 06:25:10.000"
}
