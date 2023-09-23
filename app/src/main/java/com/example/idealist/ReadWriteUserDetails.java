package com.example.idealist;

public class ReadWriteUserDetails {
    public String lName, address, dob, gender, mobile;

    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String textLName, String textAddress, String textDoB, String textGender, String textMobile){
        this.lName = textLName;
        this.address = textAddress;
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
    }
}
