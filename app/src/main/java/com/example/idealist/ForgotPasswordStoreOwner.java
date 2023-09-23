package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordStoreOwner extends AppCompatActivity {

    private Button buttonPwdResetSO;
    private EditText editTextPwdResetEmailSO;
    private ProgressBar progressBarSO;
    private FirebaseAuth authProfileSO;
    private final static String TAG = "ForgotPasswordStoreOwner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_store_owner);

        getSupportActionBar().setTitle("Forgot Password As Store Owner");

        editTextPwdResetEmailSO = findViewById(R.id.editTextPassResEmailSO);
        buttonPwdResetSO = findViewById(R.id.buttonPasswordResetSO);
        progressBarSO = findViewById(R.id.progressBarForgotPassSO);

        buttonPwdResetSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextPwdResetEmailSO.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(ForgotPasswordStoreOwner.this, "Please Enter Your Registered Email", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmailSO.setError("Email is Required");
                    editTextPwdResetEmailSO.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPasswordStoreOwner.this, "Please Enter Valid Email", Toast.LENGTH_SHORT).show();
                    editTextPwdResetEmailSO.setError("Valid Email is Required");
                    editTextPwdResetEmailSO.requestFocus();
                } else  {
                    progressBarSO.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        authProfileSO = FirebaseAuth.getInstance();
        authProfileSO.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordStoreOwner.this, "Please Check Your Inbox for Password Reset Link", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordStoreOwner.this, LoginAsStoreOwner.class);

                    //Clear stack to prevent user coming back to ForgotPassword
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        editTextPwdResetEmailSO.setError("User Does Not Exists or is No Longer Valid. PLease Register Again.");
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPasswordStoreOwner.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBarSO.setVisibility(View.GONE);
            }
        });
    }
}