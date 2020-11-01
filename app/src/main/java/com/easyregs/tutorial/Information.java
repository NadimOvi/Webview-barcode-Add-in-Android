package com.easyregs.tutorial;

public class Information {
    String UserPhoneNo;
    String macAddress;
    String Latitude;
    String longitude;
    String LocationName;

    public Information() {
    }

    public Information(String userPhoneNo, String macAddress, String latitude, String longitude, String locationName) {
        UserPhoneNo = userPhoneNo;
        this.macAddress = macAddress;
        Latitude = latitude;
        this.longitude = longitude;
        LocationName = locationName;
    }

    public String getUserPhoneNo() {
        return UserPhoneNo;
    }

    public void setUserPhoneNo(String userPhoneNo) {
        UserPhoneNo = userPhoneNo;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }
}
