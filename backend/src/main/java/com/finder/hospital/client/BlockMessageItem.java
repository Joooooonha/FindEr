package com.finder.hospital.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** E-Gen 응급실/중증질환 메시지 API 응답 항목 (getEmrrmSrsillDissMsgInqire). */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockMessageItem {

    @JsonProperty("hpid")          private String hpid;
    @JsonProperty("symBlkMsg")     private String message;          // 전달 메시지 (예: "장비부족")
    @JsonProperty("symBlkMsgTyp")  private String messageType;      // 메시지 구분 (예: "중증")
    @JsonProperty("symTypCodMag")  private String diseaseTypeName;  // 중증질환명 (예: "응급실")
    @JsonProperty("symBlkSttDtm")  private String startedAt;        // 차단 시작 yyyyMMddHHmmss
    @JsonProperty("symBlkEndDtm")  private String endedAt;          // 차단 종료 (없으면 진행 중)
}
