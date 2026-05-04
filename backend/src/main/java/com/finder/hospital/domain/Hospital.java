package com.finder.hospital.domain;

import com.finder.hospital.client.EGenItem;

/** E-Gen API 응답으로 구성되는 병원 도메인 모델. DB에 저장하지 않는다. */
public class Hospital {

    private final String id;
    private final String name;
    private final String address;
    private final String phone;
    private final double lat;
    private final double lng;
    private final boolean surgeryAvailable;
    private final boolean ctAvailable;
    private final boolean mriAvailable;

    public Hospital(EGenItem item) {
        this.id      = item.getHpid();
        this.name    = item.getDutyName();
        this.address = item.getDutyAddr();
        this.phone   = item.getContactPhone();
        this.lat     = parseDouble(item.getLatCoord());
        this.lng     = parseDouble(item.getLngCoord());
        // 수술실·CT·MRI 가용 여부는 기본정보 API(getEgytBassInfoInqire)에서만 제공
        this.surgeryAvailable = parsePositiveInt(item.getHpopyn()) > 0;
        this.ctAvailable      = "Y".equals(item.getHvctayn());
        this.mriAvailable     = "Y".equals(item.getHvmriayn());
    }

    /** 두 지점 간 거리를 킬로미터 단위로 계산한다 (Haversine 공식). */
    public double distanceTo(double targetLat, double targetLng) {
        double R    = 6371;
        double dLat = Math.toRadians(targetLat - this.lat);
        double dLng = Math.toRadians(targetLng - this.lng);
        double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2)
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

    public String getId()               { return id; }
    public String getName()             { return name; }
    public String getAddress()          { return address; }
    public String getPhone()            { return phone; }
    public double getLat()              { return lat; }
    public double getLng()              { return lng; }
    public boolean isSurgeryAvailable() { return surgeryAvailable; }
    public boolean isCtAvailable()      { return ctAvailable; }
    public boolean isMriAvailable()     { return mriAvailable; }
}
