package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginAsStoreOwner extends AppCompatActivity {

    private EditText editTextLoginEmailSO, editTextLoginPasswordSO;
    private TextView textViewLoginSignUpSO, textViewLoginForPswSO, textViewLoginAsCustomerSO;
    private ProgressBar progressBarSO;
    private FirebaseAuth authProfileSO;
    private static final String TAG = "LoginAsStoreOwner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as_store_owner);

        getSupportActionBar().setTitle("Login");

        editTextLoginEmailSO = findViewById(R.id.editTextLoginEmailSO);
        editTextLoginPasswordSO = findViewById(R.id.editTextLoginPasswordSO);
        textViewLoginSignUpSO = findViewById(R.id.textViewLoginSignUpSO);
        textViewLoginForPswSO = findViewById(R.id.textViewLoginForPswSO);
        textViewLoginAsCustomerSO = findViewById(R.id.textViewLoginAsCustomerSO);
        progressBarSO = findViewById(R.id.progressBarLoginSO);

        authProfileSO = FirebaseAuth.getInstance();

        //Show Hide Password using Eye Icon
        ImageView imageViewShowHidePwdSO = findViewById(R.id.imageViewShowHidePwdSO);
        imageViewShowHidePwdSO.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwdSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextLoginPasswordSO.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If Password is Visible then Hide it
                    editTextLoginPasswordSO.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imageViewShowHidePwdSO.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextLoginPasswordSO.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwdSO.setImageResource(R.drawable.ic_show_psw);
                }
            }
        });

        textViewLoginSignUpSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterStoreOwnerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textViewLoginForPswSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordStoreOwner.class);
                startActivity(intent);
                finish();
            }
        });

        textViewLoginAsCustomerSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        //Login As StoreOwner
        Button buttonLoginSO = findViewById(R.id.buttonLoginSO);
        buttonLoginSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = editTextLoginEmailSO.getText().toString();
                String textPassword = editTextLoginPasswordSO.getText().toString();

                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(LoginAsStoreOwner.this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmailSO.setError("Email is Required");
                    editTextLoginEmailSO.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginAsStoreOwner.this, "Please Re-Enter Your Email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmailSO.setError("Valid Email is Required");
                    editTextLoginEmailSO.requestFocus();
                } else if (TextUtils.isEmpty(textPassword)) {
                    Toast.makeText(LoginAsStoreOwner.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                    editTextLoginPasswordSO.setError("Password is Required");
                    editTextLoginPasswordSO.requestFocus();
                } else {
                    progressBarSO.setVisibility(View.VISIBLE);
                    loginUserSO(textEmail, textPassword);
                }
            }
        });

    }

    private void loginUserSO(String email, String password) {
        authProfileSO.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginAsStoreOwner.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //Get instance of the current User
                    FirebaseUser firebaseUser = authProfileSO.getCurrentUser();

                    //Check if email is verified before user can access their profile
                    if (firebaseUser.isEmailVerified()) {
                        Toast.makeText(LoginAsStoreOwner.this, "You Are Logged In Now", Toast.LENGTH_SHORT).show();
                        //Open User Profile After Successful Registration
                    } else {
                        firebaseUser.sendEmailVerification();
                        authProfileSO.signOut(); //Sign Out user
                        showAlertDialog();
                    }
                    /*Toast.makeText(LoginAsStoreOwner.this, "You Are Logged In Now", Toast.LENGTH_SHORT).show();
                    //Open User Profile After Successful Registration
                    Intent intent = new Intent(LoginAsStoreOwner.this, MainActivity.class);
                    //To Prevent User Form Returning Back to Register Activity on Pressing back Button After Registration
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); //to Close Register Activity*/
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextLoginEmailSO.setError("User Does Not Exists or is No Longer Valid. Please Register Again.");
                        editTextLoginEmailSO.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextLoginEmailSO.setError("User Does Not Exists or is No Longer Valid. Please Register Again.");
                        editTextLoginEmailSO.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginAsStoreOwner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBarSO.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        //Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginAsStoreOwner.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please Verify Your Email Now. You Can Not Login Without Email Verification.");

        //Open Email Apps If User Clicks/Taps Continue Button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To Email App in Now Window and Not within our app
                startActivity(intent);
            }
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //show the AlertDialog
        alertDialog.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (authProfileSO.getCurrentUser() != null) {
            Toast.makeText(LoginAsStoreOwner.this, "Already Logged In!", Toast.LENGTH_SHORT).show();

            //Start the UserProfileActivity
            startActivity(new Intent(LoginAsStoreOwner.this, MainActivity.class));
            finish(); //Close Login
        }
        else {
            Toast.makeText(LoginAsStoreOwner.this, "You can Login Now!", Toast.LENGTH_SHORT).show();
        }
    }
}