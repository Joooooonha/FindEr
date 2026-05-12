package com.finder.hospital.domain;

import com.finder.hospital.client.EGenItem;

/** 응급의료기관 기본정보 캐시 단위. 좌표·연락처 등 거의 변하지 않는 메타데이터. */
public record HospitalInfo(
        String id,
        String name,
        String address,
        String phone,
        double lat,
        double lng,
        boolean surgeryAvailable,
        boolean ctAvailable,
        boolean mriAvailable,
        boolean ventilatorAvailable
) {
    public static HospitalInfo from(EGenItem item) {
        return new HospitalInfo(
                item.getHpid(),
                item.getDutyName(),
                item.getDutyAddr(),
                item.getContactPhone(),
                parseDouble(item.getLatCoord()),
                parseDouble(item.getLngCoord()),
                parsePositiveInt(item.getHpopyn()) > 0,
                "Y".equals(item.getHvctayn()),
                "Y".equals(item.getHvmriayn()),
                "Y".equals(item.getHvventiayn())
        );
    }

    /** 두 지점 간 거리를 킬로미터 단위로 계산한다 (Haversine 공식). */
    public double distanceTo(double targetLat, double targetLng) {
        double R = 6371;
        double dLat = Math.toRadians(targetLat - this.lat);
        double dLng = Math.toRadians(targetLng - this.lng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(targetLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int parsePositiveInt(String value) {
        try {
            int v = value != null ? Integer.parseInt(value.trim()) : 0;
            return Math.max(v, 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
