package com.finder.hospital.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BedSnapshotTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 10, 12, 0);

    @Test
    void 최신_병상수는_구간에_맞는_상태로_분류한다() {
        assertThat(snapshot(5, NOW.minusMinutes(5)).toStatus(30, NOW)).isEqualTo(HospitalStatus.GREEN);
        assertThat(snapshot(2, NOW.minusMinutes(5)).toStatus(30, NOW)).isEqualTo(HospitalStatus.YELLOW);
        assertThat(snapshot(0, NOW.minusMinutes(5)).toStatus(30, NOW)).isEqualTo(HospitalStatus.RED);
    }

    @Test
    void 갱신_기준을_넘은_병상정보는_UNKNOWN으로_분류한다() {
        BedSnapshot stale = snapshot(8, NOW.minusMinutes(31));

        assertThat(stale.toStatus(30, NOW)).isEqualTo(HospitalStatus.UNKNOWN);
    }

    @Test
    void 갱신시각이_없거나_병상수가_음수면_UNKNOWN으로_분류한다() {
        assertThat(snapshot(8, null).toStatus(30, NOW)).isEqualTo(HospitalStatus.UNKNOWN);
        assertThat(snapshot(-1, NOW.minusMinutes(5)).toStatus(30, NOW)).isEqualTo(HospitalStatus.UNKNOWN);
    }

    private static BedSnapshot snapshot(Integer availableBeds, LocalDateTime updatedAt) {
        return new BedSnapshot(
                availableBeds,
                1,
                2,
                3,
                4,
                5,
                true,
                true,
                true,
                updatedAt
        );
    }
}
