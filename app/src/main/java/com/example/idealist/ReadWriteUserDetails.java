package com.example.idealist;

public class ReadWriteUserDetails {
    public String fullName, dob, gender, mobile;

    public ReadWriteUserDetails(String textFullName, String textDoB, String textGender, String textMobile){
        this.fullName = textFullName;
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
    }
}
