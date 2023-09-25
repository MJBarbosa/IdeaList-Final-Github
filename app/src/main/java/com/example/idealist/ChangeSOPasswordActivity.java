package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeSOPasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfileSO;
    private EditText editTextPwdCurrSO, editTextPwdNewSO, editTextPwdConfirmNewSO;
    private TextView textViewAuthenticatedSO;
    private Button buttonChangePwdSO, buttonReAuthenticateSO;
    private ProgressBar progressBarSO;
    private String userPwdCurrSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_sopassword);

        getSupportActionBar().setTitle("Change Password");

        editTextPwdNewSO = findViewById(R.id.editTextChangePwdNewSO);
        editTextPwdCurrSO = findViewById(R.id.editTextChangePwdOldSO);
        editTextPwdConfirmNewSO = findViewById(R.id.editTextConChangePasswordSO);
        textViewAuthenticatedSO = findViewById(R.id.textViewChangePwdAuthenticatedSO);
        progressBarSO = findViewById(R.id.progressBarChangePwdSO);
        buttonChangePwdSO = findViewById(R.id.buttonChangePwdSO);
        buttonReAuthenticateSO = findViewById(R.id.buttonChangePwdAuthenticateSO);

        //Disable ediText for New Password, Confirm New Password and Make Change Pwd Button unclickable until user is authenticated
        editTextPwdNewSO.setEnabled(false);
        editTextPwdConfirmNewSO.setEnabled(false);
        buttonChangePwdSO.setEnabled(false);

        authProfileSO = FirebaseAuth.getInstance();
        FirebaseUser firebaseUserSO = authProfileSO.getCurrentUser();

        if (firebaseUserSO.equals("")) {
            Toast.makeText(ChangeSOPasswordActivity.this, "Something Went Wrong! User's details not Available.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangeSOPasswordActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUserSO);
        }
    }

    //ReAuthenticate User Before Changing Password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticateSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurrSO = editTextPwdCurrSO.getText().toString();

                if (TextUtils.isEmpty(userPwdCurrSO)) {
                    Toast.makeText(ChangeSOPasswordActivity.this, "Password is Needed", Toast.LENGTH_SHORT).show();
                    editTextPwdCurrSO.setError("Please Enter Your Current Password to Authenticate");
                    editTextPwdCurrSO.requestFocus();
                } else {
                    progressBarSO.setVisibility(View.VISIBLE);

                    //ReAuthenticate User Now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurrSO);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBarSO.setVisibility(View.GONE);

                                //Disable editText for Current Password. Enable EditText for New Password and Confirm New Password
                                editTextPwdCurrSO.setEnabled(false);
                                editTextPwdNewSO.setEnabled(true);
                                editTextPwdConfirmNewSO.setEnabled(true);

                                //Enable Change Password Button. Disable Authenticate Button
                                buttonChangePwdSO.setEnabled(true);
                                buttonReAuthenticateSO.setEnabled(false);

                                //Set TextView to Show User is authenticated/verified
                                textViewAuthenticatedSO.setText("You are authenticated/verified." + "You can change password now!");
                                Toast.makeText(ChangeSOPasswordActivity.this, "Password has Been Verified." + "Change Password Now", Toast.LENGTH_SHORT).show();

                                //Update Color of Change Password Button
                                buttonChangePwdSO.setBackgroundTintList(ContextCompat.getColorStateList(ChangeSOPasswordActivity.this, R.color.dark_green));

                                buttonChangePwdSO.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(ChangeSOPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBarSO.setVisibility(View.GONE);

                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUserSO) {
        String userPwdNewSO = editTextPwdNewSO.getText().toString();
        String userPwdConfirmNewSO = editTextPwdConfirmNewSO.getText().toString();

        if (TextUtils.isEmpty(userPwdNewSO)) {
            Toast.makeText(ChangeSOPasswordActivity.this, "New Password is Needed", Toast.LENGTH_SHORT).show();
            editTextPwdNewSO.setError("Please Enter Your New Password");
            editTextPwdNewSO.requestFocus();
        } else if (TextUtils.isEmpty(userPwdConfirmNewSO)) {
            Toast.makeText(ChangeSOPasswordActivity.this, "Please Confirm Your Password", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNewSO.setError("Please Re-Enter Your New Password");
            editTextPwdConfirmNewSO.requestFocus();
        } else if (!userPwdNewSO.matches(userPwdConfirmNewSO)) {
            Toast.makeText(ChangeSOPasswordActivity.this, "Password Did Not Match", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNewSO.setError("Please Re-Enter Same Password");
            editTextPwdConfirmNewSO.requestFocus();
        } else if (userPwdCurrSO.matches(userPwdNewSO)) {
            Toast.makeText(ChangeSOPasswordActivity.this, "New Password Cannot Be Same As Old Password", Toast.LENGTH_SHORT).show();
            editTextPwdNewSO.setError("Please Enter A New Password");
            editTextPwdNewSO.requestFocus();
        } else {
            progressBarSO.setVisibility(View.VISIBLE);

            firebaseUserSO.updatePassword(userPwdNewSO).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangeSOPasswordActivity.this, "Password Has Been Changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangeSOPasswordActivity.this, StoreOwnerProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(ChangeSOPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBarSO.setVisibility(View.GONE);
                }
            });
        }
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

        if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuHomeSO) {
            Intent intent = new Intent(ChangeSOPasswordActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(ChangeSOPasswordActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(ChangeSOPasswordActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettings) {
            Toast.makeText(ChangeSOPasswordActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(ChangeSOPasswordActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(ChangeSOPasswordActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(ChangeSOPasswordActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangeSOPasswordActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(ChangeSOPasswordActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}