package com.example.idealist;

public class ReadWriteUserDetailsSO {

    public String fullName, dob, gender, mobile, storeName, storeLocation;

    public ReadWriteUserDetailsSO() {}

    public ReadWriteUserDetailsSO(String textFullName,String textDoB, String textGender, String textMobile, String textStoreName, String textStoreLocation) {
        this.fullName = textFullName;
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
        this.storeName = textStoreName;
        this.storeLocation = textStoreLocation;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(String storeLocation) {
        this.storeLocation = storeLocation;
    }
}
