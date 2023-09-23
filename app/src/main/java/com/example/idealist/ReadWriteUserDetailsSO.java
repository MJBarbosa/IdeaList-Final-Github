package com.example.idealist;

public class ReadWriteUserDetailsSO {

    public String dob, gender, mobile, storeName, storeLocation;

    public ReadWriteUserDetailsSO(){};

    public ReadWriteUserDetailsSO(String textDoB, String textGender, String textMobile, String textStoreName, String textStoreLocation){
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
        this.storeName = textStoreName;
        this.storeLocation = textStoreLocation;
    }
}
