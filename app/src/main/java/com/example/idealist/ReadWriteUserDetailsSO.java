package com.example.idealist;

public class ReadWriteUserDetailsSO {

    public String fullName, dob, gender, mobile, storeName, storeLocation;

    public ReadWriteUserDetailsSO(String textFullName, String textDoB, String textGender, String textMobile, String textStoreName, String textStoreLocation){
        this.fullName = textFullName;
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
        this.storeName = textStoreName;
        this.storeLocation = textStoreLocation;
    }
}
