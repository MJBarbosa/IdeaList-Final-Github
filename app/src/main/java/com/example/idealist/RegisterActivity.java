package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFirstName, editTextRegisterLastName, editTextRegisterAddress, editTextRegisterEmail, editTextRegisterDoB, editTextRegisterMobile,
            editTextRegisterPassword, editTextRegisterConfirmPassword;
    private ProgressBar progressBarRegister;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    private static final String TAG= "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register");

        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        progressBarRegister = findViewById(R.id.progressBarReg);
        editTextRegisterFirstName = findViewById(R.id.editTextRegFName);
        editTextRegisterLastName = findViewById(R.id.editTextRegLName);
        editTextRegisterAddress = findViewById(R.id.editTextRegAddress);
        editTextRegisterEmail = findViewById(R.id.editTextRegEmail);
        editTextRegisterDoB = findViewById(R.id.editTextRegDob);
        editTextRegisterMobile = findViewById(R.id.editTextRegMobile);
        editTextRegisterPassword = findViewById(R.id.editTextRegPassword);
        editTextRegisterConfirmPassword = findViewById(R.id.editTextRegConfirmPassword);

        //RadioButton for Gender
        radioGroupRegisterGender =findViewById(R.id.radioGroupRegGender);
        radioGroupRegisterGender.clearCheck();

        //Setting up DatePicker on EditText
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });


        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                //Obtain the entered data
                String textFirstName = editTextRegisterFirstName.getText().toString();
                String textLastName = editTextRegisterLastName.getText().toString();
                String textAddress = editTextRegisterAddress.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textMobile = editTextRegisterMobile.getText().toString();
                String textPwd = editTextRegisterPassword.getText().toString();
                String textConfirmPwd = editTextRegisterConfirmPassword.getText().toString();
                String textGender; //Can't obtain the value before verifying if any button was selected or not

                //Valid mobile number using matcher and pattern (Regular Expression)
                String mobileRegex = "0[0-9]{10}"; //First no. should be 0 and rest 10 no. can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);

                if (TextUtils.isEmpty(textFirstName)){
                    Toast.makeText(RegisterActivity.this, "Please Enter Your First Name", Toast.LENGTH_LONG).show();
                    editTextRegisterFirstName.setError("First Name is Required");
                    editTextRegisterFirstName.requestFocus();
                } else if (TextUtils.isEmpty(textLastName)){
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Last Name", Toast.LENGTH_LONG).show();
                    editTextRegisterLastName.setError("Last Name is Required");
                    editTextRegisterLastName.requestFocus();
                } else if (TextUtils.isEmpty(textAddress)){
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Address", Toast.LENGTH_LONG).show();
                    editTextRegisterAddress.setError("Address is Required");
                    editTextRegisterAddress.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email is Required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter Your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid Email is Required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Date of Birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDoB.setError("Date of Birth is Required");
                    editTextRegisterDoB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please Select Your Gender", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is Required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Mobile No.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile No. is Required");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() != 11) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile No. Should be 11 Digits");
                    editTextRegisterMobile.requestFocus();
                } else if (!mobileMatcher.find()) {
                    Toast.makeText(RegisterActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile No. is not Valid");
                    editTextRegisterMobile.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPassword.setError("Password is Required");
                    editTextRegisterPassword.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password Should be at Least 6 Digits", Toast.LENGTH_LONG).show();
                    editTextRegisterPassword.setError("Password Too Weak");
                    editTextRegisterPassword.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please Confirm Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPassword.setError("Password Confirmation is Required");
                    editTextRegisterConfirmPassword.requestFocus();
                } else if (!textPwd.equals(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Password Same Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPassword.setError("Password Confirmation is Required");
                    editTextRegisterConfirmPassword.requestFocus();
                    //Clear the entered passwords
                    editTextRegisterPassword.clearComposingText();
                    editTextRegisterConfirmPassword.clearComposingText();
                } else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBarRegister.setVisibility(View.VISIBLE);
                    registerUser(textFirstName, textLastName, textAddress, textEmail, textDoB, textGender, textMobile, textPwd);
                }
            }
        });


    }

    //Register User using the credentials given
    private void registerUser(String textFirstName, String textLastName, String textAddress, String textEmail, String textDoB, String textGender, String textMobile, String textPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //Display
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFirstName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    //Enter User Data Into the Firebase Realtime Database.
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textLastName, textAddress, textDoB, textGender, textMobile);

                    //Extracting user reference from Database for "Register Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                //Send Verification Email
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(RegisterActivity.this, "User Registered Successfully. Please Verify Your Email", Toast.LENGTH_LONG).show();

                                //Open User Profile After Successful Registration
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                //To Prevent User Form Returning Back to Register Activity on Pressing back Button After Registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); //to Close Register Activity

                            } else {
                                Toast.makeText(RegisterActivity.this, "User Registered Failed. Please Try Again", Toast.LENGTH_LONG).show();
                            }
                            progressBarRegister.setVisibility(View.GONE);
                        }
                    });

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        editTextRegisterPassword.setError("Your Password is too Weak");
                        editTextRegisterPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextRegisterPassword.setError("Your Email is Invalid or Already Use.");
                        editTextRegisterPassword.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        editTextRegisterPassword.setError("User is Already Register with this Email");
                        editTextRegisterPassword.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressBarRegister.setVisibility(View.GONE);
                }
            }
        });
    }
}