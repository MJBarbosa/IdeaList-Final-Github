package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginAsStoreOwner extends AppCompatActivity {
    private EditText editTextLoginEmailSO, editTextLoginPasswordSO;
    private ProgressBar progressBarSO;
    private FirebaseAuth authSO;
    private DatabaseReference userRolesRefSO; // Reference to the UserRoles node
    private static final String TAG = "LoginAsStoreOwner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as_store_owner);

        editTextLoginEmailSO = findViewById(R.id.editTextLoginEmailSO);
        editTextLoginPasswordSO = findViewById(R.id.editTextLoginPasswordSO);
        progressBarSO = findViewById(R.id.progressBarLoginSO);
        Button buttonLoginSO = findViewById(R.id.buttonLoginSO);
        TextView textViewLoginSignUpSO = findViewById(R.id.textViewLoginSignUpSO);
        TextView textViewLoginAsCustomer = findViewById(R.id.textViewLoginAsCustomerSO);
        TextView textViewLoginForgotPassSO = findViewById(R.id.textViewLoginForPswSO);

        authSO = FirebaseAuth.getInstance();
        userRolesRefSO = FirebaseDatabase.getInstance().getReference("UserRoles"); // Initialize the reference

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

        buttonLoginSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextLoginEmailSO.getText().toString().trim();
                String password = editTextLoginPasswordSO.getText().toString().trim();

                if (validateInput(email, password)) {
                    loginUser(email, password);
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

        textViewLoginAsCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        textViewLoginForgotPassSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordStoreOwner.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateInput(String textEmail, String textPassword) {
        if (TextUtils.isEmpty(textEmail)) {
            showToast("Please enter your email.");
            editTextLoginEmailSO.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(textPassword)) {
            showToast("Please enter your password.");
            editTextLoginPasswordSO.requestFocus();
            return false;
        } else {
            progressBarSO.setVisibility(View.VISIBLE);
            return true;
        }
    }

    private void loginUser(String email, String password) {
        progressBarSO.setVisibility(View.VISIBLE);

        authSO.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarSO.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = authSO.getCurrentUser();
                            if (firebaseUser != null) {
                                // Fetch the user's role from the database
                                fetchUserRole(firebaseUser.getUid());
                            }
                        } else {
                            showToast("Login failed. Please check your credentials.");
                        }
                    }
                });
    }

    private void fetchUserRole(String uid) {
        userRolesRefSO.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                if (role != null) {
                    if ("customer".equals(role)) {
                        // User is a Customer, navigate to CustomerMainActivity
                        showToast("Cannot login as a Customer.");
                    } else if ("storeOwner".equals(role)) {
                        // User is a Store Owner, show a message that they cannot login
                        startActivity(new Intent(LoginAsStoreOwner.this, MainSOActivity.class));
                        finish(); // Close Login Activity
                    } else {
                        showToast("Login failed. Invalid user role.");
                        finish(); // Close Login Activity
                    }
                } else {
                    showToast("Login failed. User role not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Error fetching user role: " + databaseError.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(LoginAsStoreOwner.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = authSO.getCurrentUser();
        if (currentUser != null) {
            showToast("Already logged in!");
            // Check the user's role and navigate accordingly
            fetchUserRole(currentUser.getUid());
        } else {
            showToast("You can log in now.");
        }
    }
}
