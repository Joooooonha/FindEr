package com.finder.hospital.dto;

import com.finder.hospital.domain.BedSnapshot;
import com.finder.hospital.domain.HospitalInfo;
import com.finder.hospital.domain.HospitalStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HospitalResponseTest {

    private static final HospitalInfo HOSPITAL = new HospitalInfo(
            "A1234",
            "테스트 응급실",
            "서울시 테스트구",
            "02-0000-0000",
            37.5,
            127.0,
            true,
            true,
            false,
            true
    );

    @Test
    void 오래된_병상정보는_목록에서_숫자를_숨기고_UNKNOWN으로_표시한다() {
        HospitalResponse response = HospitalResponse.of(
                HOSPITAL,
                1.24,
                staleSnapshot(),
                30,
                List.of(),
                Set.of()
        );

        assertThat(response.status()).isEqualTo(HospitalStatus.UNKNOWN);
        assertThat(response.availableBeds()).isNull();
        assertThat(response.stale()).isTrue();
    }

    @Test
    void 오래된_병상정보는_상세_수치를_숨기고_기본정보의_장비값을_사용한다() {
        HospitalDetailResponse response = HospitalDetailResponse.from(
                HOSPITAL,
                staleSnapshot(),
                30,
                List.of(),
                Set.of()
        );

        assertThat(response.status()).isEqualTo(HospitalStatus.UNKNOWN);
        assertThat(response.availableBeds()).isNull();
        assertThat(response.operatingRooms()).isNull();
        assertThat(response.generalWardBeds()).isNull();
        assertThat(response.surgeryAvailable()).isTrue();
        assertThat(response.ctAvailable()).isTrue();
        assertThat(response.mriAvailable()).isFalse();
        assertThat(response.ventilatorAvailable()).isTrue();
    }

    private static BedSnapshot staleSnapshot() {
        return new BedSnapshot(
                8,
                2,
                7,
                3,
                1,
                1,
                false,
                true,
                false,
                LocalDateTime.now().minusMinutes(31)
        );
    }
}
