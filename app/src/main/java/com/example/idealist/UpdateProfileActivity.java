package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateFName, editTextUpdateLName, editTextUpdateAddress, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFName, textLName, textAddress, textDoB, textGender, textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile Details");

        progressBar = findViewById(R.id.progressBarUpdateProfile);
        editTextUpdateFName = findViewById(R.id.editTextUpdateProfileFName);
        editTextUpdateLName = findViewById(R.id.editTextUpdateProfileLName);
        editTextUpdateAddress = findViewById(R.id.editTextUpdateProfileAddress);
        editTextUpdateDoB = findViewById(R.id.editTextUpdateProfileDoB);
        editTextUpdateMobile = findViewById(R.id.editTextUpdateProfileMobile);

        radioGroupUpdateGender = findViewById(R.id.radioGroupUpdateProfileGender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //Show Profile Data
        showProfile(firebaseUser);

        //Upload Profile Pic
        Button buttonUploadProfilePic = findViewById(R.id.buttonUploadProfilePic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonUpdateEmail = findViewById(R.id.buttonUpdateProfileEmail);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Setting up DatePicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Extracting saved dd, mm, yyyy into different variables by creating an array delimited by "/"
                String textSADoB[] = textDoB.split("/");

                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1; // To take care of month index starting from 0 to 2
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonUpdateProfile = findViewById(R.id.buttonProfileUpdate);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    //Update Profile
    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderIO = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderIO);

        //Valid mobile number using matcher and pattern (Regular Expression)
        String mobileRegex = "0[0-9]{10}"; //First no. should be 0 and rest 10 no. can be any no.
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);

        if (TextUtils.isEmpty(textFName)){
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your First Name", Toast.LENGTH_LONG).show();
            editTextUpdateFName.setError("First Name is Required");
            editTextUpdateFName.requestFocus();
        } else if (TextUtils.isEmpty(textLName)){
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Last Name", Toast.LENGTH_LONG).show();
            editTextUpdateLName.setError("Last Name is Required");
            editTextUpdateLName.requestFocus();
        } else if (TextUtils.isEmpty(textAddress)){
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Address", Toast.LENGTH_LONG).show();
            editTextUpdateAddress.setError("Address is Required");
            editTextUpdateAddress.requestFocus();
        } else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Date of Birth", Toast.LENGTH_LONG).show();
            editTextUpdateDoB.setError("Date of Birth is Required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please Select Your Gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is Required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfileActivity.this, "Please Enter Your Mobile No.", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile No. is Required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 11) {
            Toast.makeText(UpdateProfileActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile No. Should be 11 Digits");
            editTextUpdateMobile.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateProfileActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile No. is not Valid");
            editTextUpdateMobile.requestFocus();
        }  else {
            textGender = radioButtonUpdateGenderSelected.getText().toString();

            //Obtain the data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFName = editTextUpdateFName.getText().toString();
            textLName = editTextUpdateLName.getText().toString();
            textAddress = editTextUpdateAddress.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            //Enter User Data into the Firebase Realtime Database. Set up dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textLName, textAddress, textDoB, textGender, textMobile);

            //Extract User Reference from Database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        //Setting new display Name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Update Successful!", Toast.LENGTH_LONG).show();

                        //Stop user from returning to UpdateProfileActivity on pressing back button and close activity
                        Intent intent = new Intent(UpdateProfileActivity.this, UsersProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    //fetch data from Firebase and Display
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        //Extracting User Reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    textFName = firebaseUser.getDisplayName();
                    textLName = readUserDetails.lName;
                    textAddress = readUserDetails.address;
                    textDoB = readUserDetails.dob;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;

                    editTextUpdateFName.setText(textFName);
                    editTextUpdateLName.setText(textLName);
                    editTextUpdateAddress.setText(textAddress);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    //Show Gender through Radio Button
                    if (textGender.equals("Male")) {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radioButtonUpdateProfileMale);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radioButtonUpdateProfileFemale);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuRefresh) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuUpdateProfile) {
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menuSettings) {
            Toast.makeText(UsersProfileActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(UsersProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(UsersProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menuLogout) {
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this, Login.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}