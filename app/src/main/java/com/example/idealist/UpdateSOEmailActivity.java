package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class UpdateSOEmailActivity extends AppCompatActivity {

    private FirebaseAuth authProfileSO;
    private FirebaseUser firebaseUserSO;
    private ProgressBar progressBarSO;
    private TextView textViewAuthenticatedSO;
    private String userOldEmailSO, userNewEmailSO, userPwdSO;
    private Button buttonUpdatedEmailSO;
    private EditText editTextNewEmailSO, editTextPwdSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_soemail);

        getSupportActionBar().setTitle("Update Email");

        progressBarSO = findViewById(R.id.progressBarUpdateEmailSO);
        editTextPwdSO = findViewById(R.id.editTextUpdateEmailVerifyPasswordSO);
        editTextNewEmailSO = findViewById(R.id.editTextUpdateEmailNewSO);
        textViewAuthenticatedSO = findViewById(R.id.textViewUpdateEmailAuthenticatedSO);
        buttonUpdatedEmailSO = findViewById(R.id.buttonUpdateEmailSO);

        buttonUpdatedEmailSO.setEnabled(false); //Make button disabled in the beginning until the user is authenticated
        editTextNewEmailSO.setEnabled(false);

        authProfileSO = FirebaseAuth.getInstance();
        firebaseUserSO = authProfileSO.getCurrentUser();

        //Set old email ID on TextView
        userOldEmailSO = firebaseUserSO.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textViewUpdateEmailOldSO);
        textViewOldEmail.setText(userOldEmailSO);

        if (firebaseUserSO.equals("")) {
            Toast.makeText(UpdateSOEmailActivity.this, "Something Went Wrong! User's Details Not Available", Toast.LENGTH_LONG).show();
        } else {
            reAuthenticate(firebaseUserSO);
        }
    }

    //ReAuthenticate/Verify User before Updating Email
    private void reAuthenticate(FirebaseUser firebaseUserSO) {
        Button buttonVerifyUser = findViewById(R.id.buttonAuthenticateUserSO);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Obtain Password for Authentication
                userPwdSO = editTextPwdSO.getText().toString();

                if (TextUtils.isEmpty(userPwdSO)) {
                    Toast.makeText(UpdateSOEmailActivity.this, "Password is Needed to Continue", Toast.LENGTH_SHORT).show();
                    editTextPwdSO.setError("Please Enter Your Password for Authentication");
                    editTextPwdSO.requestFocus();
                } else {
                    progressBarSO.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmailSO, userPwdSO);

                    firebaseUserSO.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBarSO.setVisibility(View.GONE);

                                Toast.makeText(UpdateSOEmailActivity.this, "Password has Been Verified." + "You Can Update Email Now.", Toast.LENGTH_LONG).show();

                                //Set TextView to show the user is authenticated
                                textViewAuthenticatedSO.setText("You are Authenticated, You Can Update Your Email Now.");

                                //Disable EditText for Password, Button to verify user and enable EditText for now Email and Update Email Button
                                editTextNewEmailSO.setEnabled(true);
                                editTextPwdSO.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdatedEmailSO.setEnabled(true);

                                //Change Color of Update Email Button
                                buttonUpdatedEmailSO.setBackgroundTintList(ContextCompat.getColorStateList(UpdateSOEmailActivity.this, R.color.dark_green));

                                buttonUpdatedEmailSO.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmailSO = editTextNewEmailSO.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmailSO)) {
                                            Toast.makeText(UpdateSOEmailActivity.this, "New Email is Required", Toast.LENGTH_SHORT).show();
                                            editTextNewEmailSO.setError("Please Enter New Email");
                                            editTextNewEmailSO.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmailSO).matches()) {
                                            Toast.makeText(UpdateSOEmailActivity.this, "Please Enter Valid Email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmailSO.setError("Please Provide Valid Email");
                                            editTextNewEmailSO.requestFocus();
                                        } else if (userOldEmailSO.matches(userNewEmailSO)) {
                                            Toast.makeText(UpdateSOEmailActivity.this, "New Email Cannot Be Same as Old Email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmailSO.setError("Please Enter New Email");
                                            editTextNewEmailSO.requestFocus();
                                        } else {
                                            progressBarSO.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUserSO);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(UpdateSOEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUserSO) {
        firebaseUserSO.updateEmail(userNewEmailSO).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {

                    //Verify Email
                    firebaseUserSO.sendEmailVerification();

                    Toast.makeText(UpdateSOEmailActivity.this, "Email has Been Updated, PLease Verify Your New Email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateSOEmailActivity.this, StoreOwnerProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(UpdateSOEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
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

        if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(UpdateSOEmailActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(UpdateSOEmailActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menuSettings) {
            Toast.makeText(StoreOwnerProfileActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(UpdateSOEmailActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateSOEmailActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateSOEmailActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}