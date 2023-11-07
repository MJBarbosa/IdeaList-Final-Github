package com.inventory.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.inventory.idealist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterStoreOwnerActivity extends AppCompatActivity {

    private EditText editTextRegisterFullNameSO, editTextRegisterEmailSO, editTextRegisterDoBSO, editTextRegisterMobileSO,
            editTextRegisterPasswordSO, editTextRegisterConfirmPasswordSO, editTextRegisterStoreNameSO, editTextRegisterStoreLocationSO;
    private ProgressBar progressBarRegisterSO;
    private RadioGroup radioGroupRegisterGenderSO;
    private RadioButton radioButtonRegisterGenderSelectedSO;
    private DatePickerDialog picker;
    private static final String TAG= "RegisterStoreOwnerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_store_owner);

        getSupportActionBar().setTitle("RegisterAsStoreOwner");

        Toast.makeText(RegisterStoreOwnerActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        progressBarRegisterSO = findViewById(R.id.progressBarRegSO);
        editTextRegisterFullNameSO = findViewById(R.id.editTextRegFNameSO);
        editTextRegisterEmailSO = findViewById(R.id.editTextRegEmailSO);
        editTextRegisterDoBSO = findViewById(R.id.editTextRegDobSO);
        editTextRegisterMobileSO = findViewById(R.id.editTextRegMobileSO);
        editTextRegisterPasswordSO = findViewById(R.id.editTextRegPasswordSO);
        editTextRegisterConfirmPasswordSO = findViewById(R.id.editTextRegConfirmPasswordSO);
        editTextRegisterStoreNameSO = findViewById(R.id.editTextRegStoreNameSO);
        editTextRegisterStoreLocationSO = findViewById(R.id.editTextRegStoreLocationSO);

        //RadioButton for Gender
        radioGroupRegisterGenderSO = findViewById(R.id.radioGroupRegGenderSO);
        radioGroupRegisterGenderSO.clearCheck();

        //Setting up DatePicker on EditText
        editTextRegisterDoBSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker = new DatePickerDialog(RegisterStoreOwnerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoBSO.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });


        Button buttonRegisterSO = findViewById(R.id.buttonRegisterSO);
        buttonRegisterSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedGenderId = radioGroupRegisterGenderSO.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelectedSO = findViewById(selectedGenderId);

                //Obtain the entered data
                String textFullName = editTextRegisterFullNameSO.getText().toString();
                String textEmail = editTextRegisterEmailSO.getText().toString();
                String textDoB = editTextRegisterDoBSO.getText().toString();
                String textMobile = editTextRegisterMobileSO.getText().toString();
                String textPwd = editTextRegisterPasswordSO.getText().toString();
                String textConfirmPwd = editTextRegisterConfirmPasswordSO.getText().toString();
                String textGender; //Can't obtain the value before verifying if any button was selected or not
                String textStoreName = editTextRegisterStoreNameSO.getText().toString();
                String textStoreLocation = editTextRegisterStoreLocationSO.getText().toString();

                //Valid mobile number using matcher and pattern (Regular Expression)
                String mobileRegex = "0[0-9]{10}"; //First no. should be 0 and rest 10 no. can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);

                if (TextUtils.isEmpty(textFullName)){
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Full Name", Toast.LENGTH_LONG).show();
                    editTextRegisterFullNameSO.setError("Full Name is Required");
                    editTextRegisterFullNameSO.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmailSO.setError("Email is Required");
                    editTextRegisterEmailSO.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Re-enter Your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmailSO.setError("Valid Email is Required");
                    editTextRegisterEmailSO.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Date of Birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDoBSO.setError("Date of Birth is Required");
                    editTextRegisterDoBSO.requestFocus();
                } else if (radioGroupRegisterGenderSO.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Select Your Gender", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelectedSO.setError("Gender is Required");
                    radioButtonRegisterGenderSelectedSO.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Mobile No.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobileSO.setError("Mobile No. is Required");
                    editTextRegisterMobileSO.requestFocus();
                } else if (textMobile.length() != 11) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobileSO.setError("Mobile No. Should be 11 Digits");
                    editTextRegisterMobileSO.requestFocus();
                } else if (!mobileMatcher.find()) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
                    editTextRegisterMobileSO.setError("Mobile No. is not Valid");
                    editTextRegisterMobileSO.requestFocus();
                } else if (TextUtils.isEmpty(textStoreName)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Store Name", Toast.LENGTH_LONG).show();
                    editTextRegisterStoreNameSO.setError("Store Name is Required");
                    editTextRegisterStoreNameSO.requestFocus();
                } else if (TextUtils.isEmpty(textStoreLocation)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Store Location", Toast.LENGTH_LONG).show();
                    editTextRegisterStoreLocationSO.setError("Store Location is Required");
                    editTextRegisterStoreLocationSO.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPasswordSO.setError("Password is Required");
                    editTextRegisterPasswordSO.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Password Should be at Least 6 Digits", Toast.LENGTH_LONG).show();
                    editTextRegisterPasswordSO.setError("Password Too Weak");
                    editTextRegisterPasswordSO.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPwd)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Please Confirm Your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPasswordSO.setError("Password Confirmation is Required");
                    editTextRegisterConfirmPasswordSO.requestFocus();
                } else if (!textPwd.equals(textConfirmPwd)) {
                    Toast.makeText(RegisterStoreOwnerActivity.this, "Password Same Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPasswordSO.setError("Password Confirmation is Required");
                    editTextRegisterConfirmPasswordSO.requestFocus();
                    //Clear the entered passwords
                    editTextRegisterPasswordSO.clearComposingText();
                    editTextRegisterConfirmPasswordSO.clearComposingText();
                } else {
                    textGender = radioButtonRegisterGenderSelectedSO.getText().toString();
                    progressBarRegisterSO.setVisibility(View.VISIBLE);
                    registerUserSO(textFullName, textEmail, textDoB, textGender, textMobile, textStoreName, textStoreLocation, textPwd);
                }
            }
        });


    }

    private void registerUserSO(String textFullName, String textEmail, String textDoB, String textGender, String textMobile, String textStoreName, String textStoreLocation, String textPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterStoreOwnerActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // Set user role as "storeOwner"
                    setUserRole(firebaseUser.getUid(), "storeOwner");

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    //Enter User Data Into the Firebase Realtime Database.
                    ReadWriteUserDetailsSO writeUserDetails = new ReadWriteUserDetailsSO(textFullName, textDoB, textGender, textMobile, textStoreName, textStoreLocation);

                    //Extracting user reference from Database for "Register Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                //Send Verification Email
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(RegisterStoreOwnerActivity.this, "User Registered Successfully. Please Verify Your Email", Toast.LENGTH_LONG).show();

                                //Open User Profile After Successful Registration
                                Intent intent = new Intent(RegisterStoreOwnerActivity.this, MainSOActivity.class);
                                //To Prevent User Form Returning Back to Register Activity on Pressing back Button After Registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); //to Close Register Store Owner Activity

                            } else {
                                Toast.makeText(RegisterStoreOwnerActivity.this, "User Registered Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                            }
                            progressBarRegisterSO.setVisibility(View.GONE);
                        }
                    });

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        editTextRegisterPasswordSO.setError("Your Password is too Weak");
                        editTextRegisterPasswordSO.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextRegisterPasswordSO.setError("Your Email is Invalid or Already Use.");
                        editTextRegisterPasswordSO.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        editTextRegisterPasswordSO.setError("User is Already Register with this Email");
                        editTextRegisterPasswordSO.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterStoreOwnerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressBarRegisterSO.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setUserRole(String uid, String role) {
        DatabaseReference userRolesRef = FirebaseDatabase.getInstance().getReference("UserRoles");
        userRolesRef.child(uid).setValue(role);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(RegisterStoreOwnerActivity.this, LoginAsStoreOwner.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        }

        // Handle other menu items if needed
        // ...

        return super.onOptionsItemSelected(item);
    }
}