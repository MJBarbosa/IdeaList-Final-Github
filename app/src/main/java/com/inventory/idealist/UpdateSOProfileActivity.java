package com.inventory.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.inventory.idealist.R;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateSOProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateFullNameSO, editTextUpdateDoBSO, editTextUpdateMobileSO, editTextUpdateSNameSO, editTextUpdateSAddressSO;
    private RadioGroup radioGroupUpdateGenderSO;
    private RadioButton radioButtonUpdateGenderSelectedSO;
    private String textFullNameSO, textDoBSO, textGenderSO, textMobileSO, textSNameSO, textSAddressSO;
    private FirebaseAuth authProfileSO;
    private ProgressBar progressBarSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_soprofile);

        getSupportActionBar().setTitle("Update Profile Details");

        progressBarSO = findViewById(R.id.progressBarUpdateProfileSO);
        editTextUpdateFullNameSO = findViewById(R.id.editTextUpdateProfileFullNameSO);
        editTextUpdateDoBSO = findViewById(R.id.editTextUpdateProfileDoBSO);
        editTextUpdateMobileSO = findViewById(R.id.editTextUpdateProfileMobileSO);
        editTextUpdateSNameSO = findViewById(R.id.editTextUpdateProfileSNameSO);
        editTextUpdateSAddressSO = findViewById(R.id.editTextUpdateProfileSAddressSO);

        radioGroupUpdateGenderSO = findViewById(R.id.radioGroupUpdateProfileGenderSO);

        authProfileSO = FirebaseAuth.getInstance();
        FirebaseUser firebaseUserSO = authProfileSO.getCurrentUser();

        //Show Profile Data
        showProfile(firebaseUserSO);

        //Upload Profile Pic
        Button buttonUploadProfilePic = findViewById(R.id.buttonUploadProfilePicSO);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateSOProfileActivity.this, UploadSOProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonUpdateEmailSO = findViewById(R.id.buttonUpdateProfileEmailSO);
        buttonUpdateEmailSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateSOProfileActivity.this, UpdateSOEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Setting up DatePicker on EditText
        editTextUpdateDoBSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Extracting saved dd, mm, yyyy into different variables by creating an array delimited by "/"
                String textSADoBSO[] = textDoBSO.split("/");

                int day = Integer.parseInt(textSADoBSO[0]);
                int month = Integer.parseInt(textSADoBSO[1]) - 1; // To take care of month index starting from 0 to 2
                int year = Integer.parseInt(textSADoBSO[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateSOProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoBSO.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        Button buttonUpdateProfile = findViewById(R.id.buttonProfileUpdateSO);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUserSO);
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUserSO) {
        int selectedGenderIO = radioGroupUpdateGenderSO.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelectedSO = findViewById(selectedGenderIO);

        //Valid mobile number using matcher and pattern (Regular Expression)
        String mobileRegex = "0[0-9]{10}"; //First no. should be 0 and rest 10 no. can be any no.
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobileSO);

        if (TextUtils.isEmpty(textFullNameSO)){
            Toast.makeText(UpdateSOProfileActivity.this, "Please Enter Your Full Name", Toast.LENGTH_LONG).show();
            editTextUpdateFullNameSO.setError("Full Name is Required");
            editTextUpdateFullNameSO.requestFocus();
        } else if (TextUtils.isEmpty(textDoBSO)) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Enter Your Date of Birth", Toast.LENGTH_LONG).show();
            editTextUpdateDoBSO.setError("Date of Birth is Required");
            editTextUpdateDoBSO.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelectedSO.getText())) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Select Your Gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelectedSO.setError("Gender is Required");
            radioButtonUpdateGenderSelectedSO.requestFocus();
        } else if (TextUtils.isEmpty(textMobileSO)) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Enter Your Mobile No.", Toast.LENGTH_LONG).show();
            editTextUpdateMobileSO.setError("Mobile No. is Required");
            editTextUpdateMobileSO.requestFocus();
        } else if (textMobileSO.length() != 11) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
            editTextUpdateMobileSO.setError("Mobile No. Should be 11 Digits");
            editTextUpdateMobileSO.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Re-enter Your Mobile No.", Toast.LENGTH_LONG).show();
            editTextUpdateMobileSO.setError("Mobile No. is not Valid");
            editTextUpdateMobileSO.requestFocus();
        } else if (TextUtils.isEmpty(textSNameSO)) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Enter Your Store Name", Toast.LENGTH_LONG).show();
            editTextUpdateSNameSO.setError("Store Name is Required");
            editTextUpdateSNameSO.requestFocus();
        } else if (TextUtils.isEmpty(textSAddressSO)) {
            Toast.makeText(UpdateSOProfileActivity.this, "Please Enter Your Store Location", Toast.LENGTH_LONG).show();
            editTextUpdateSAddressSO.setError("Store Location is Required");
            editTextUpdateSAddressSO.requestFocus();
        } else {
            textGenderSO = radioButtonUpdateGenderSelectedSO.getText().toString();

            //Obtain the data entered by user
            textGenderSO = radioButtonUpdateGenderSelectedSO.getText().toString();
            textFullNameSO = editTextUpdateFullNameSO.getText().toString();
            textDoBSO = editTextUpdateDoBSO.getText().toString();
            textMobileSO = editTextUpdateMobileSO.getText().toString();
            textSNameSO = editTextUpdateSNameSO.getText().toString();
            textSAddressSO = editTextUpdateSAddressSO.getText().toString();

            //Enter User Data into the Firebase Realtime Database. Set up dependencies
            ReadWriteUserDetailsSO writeUserDetailsSO = new ReadWriteUserDetailsSO(textFullNameSO,textDoBSO, textGenderSO, textMobileSO, textSNameSO, textSAddressSO);

            //Extract User Reference from Database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");

            String userID = firebaseUserSO.getUid();

            progressBarSO.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetailsSO).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        //Setting new display Name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullNameSO).build();
                        firebaseUserSO.updateProfile(profileUpdates);

                        Toast.makeText(UpdateSOProfileActivity.this, "Update Successful!", Toast.LENGTH_LONG).show();

                        //Stop user from returning to UpdateSOProfileActivity on pressing back button and close activity
                        Intent intent = new Intent(UpdateSOProfileActivity.this, StoreOwnerProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(UpdateSOProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBarSO.setVisibility(View.GONE);
                }
            });
        }
    }

    //fetch data from Firebase and Display
    private void showProfile(FirebaseUser firebaseUserSO) {
        String userIDofRegistered = firebaseUserSO.getUid();

        //Extracting User Reference from Database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");

        progressBarSO.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetailsSO readUserDetailsSO = snapshot.getValue(ReadWriteUserDetailsSO.class);
                if (readUserDetailsSO != null) {
                    textFullNameSO = firebaseUserSO.getDisplayName();
                    textFullNameSO = readUserDetailsSO.fullName;
                    textDoBSO = readUserDetailsSO.dob;
                    textGenderSO = readUserDetailsSO.gender;
                    textMobileSO = readUserDetailsSO.mobile;
                    textSNameSO = readUserDetailsSO.storeName;
                    textSAddressSO = readUserDetailsSO.storeLocation;

                    editTextUpdateFullNameSO.setText(textFullNameSO);
                    editTextUpdateDoBSO.setText(textDoBSO);
                    editTextUpdateMobileSO.setText(textMobileSO);
                    editTextUpdateSNameSO.setText(textSNameSO);
                    editTextUpdateSAddressSO.setText(textSAddressSO);

                    //Show Gender through Radio Button
                    if (textGenderSO.equals("Male")) {
                        radioButtonUpdateGenderSelectedSO = findViewById(R.id.radioButtonUpdateProfileMaleSO);
                    } else {
                        radioButtonUpdateGenderSelectedSO = findViewById(R.id.radioButtonUpdateProfileFemaleSO);
                    }
                    radioButtonUpdateGenderSelectedSO.setChecked(true);
                } else {
                    Toast.makeText(UpdateSOProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }
                progressBarSO.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateSOProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                progressBarSO.setVisibility(View.GONE);
            }
        });
    }

    //Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu_so, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuHomeSO) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmailSO) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettingsSO) {
            Toast.makeText(UpdateSOProfileActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuSetStoreTime) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, SetStoreTimeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuChangePasswordSO) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuDeleteProfileSO) {
            Intent intent = new Intent(UpdateSOProfileActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(UpdateSOProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateSOProfileActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateSOProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}