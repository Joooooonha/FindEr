package com.finder.hospital.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** safetydata.go.kr realtime bed API response item. */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BedInfoItem {

    @JsonProperty("BFR_INST_ID") private String hpid;
    @JsonProperty("EMRO")        private Integer emro;
    @JsonProperty("OPRO")        private Integer opro;
    @JsonProperty("WARD")        private Integer ward;
    @JsonProperty("GNRL_ICU")    private Integer gnrlIcu;
    @JsonProperty("NRVS_ICU")    private Integer nrvsIcu;
    @JsonProperty("EMRGN_ICU")   private Integer emergnIcu;
    @JsonProperty("CT_AVBL_YN")  private String ctAvailable;
    @JsonProperty("MRI_AVBL_YN") private String mriAvailable;
    @JsonProperty("VENT_AVBL_YN") private String ventAvailable;
    @JsonProperty("MDFCN_DT")    private String modifiedAt;
}
