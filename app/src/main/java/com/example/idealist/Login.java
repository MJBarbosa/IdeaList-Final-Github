package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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

public class Login extends AppCompatActivity {
    private EditText editTextLoginEmail, editTextLoginPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference userRolesRef; // Reference to the UserRoles node
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        progressBar = findViewById(R.id.progressBarLogin);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewLoginSignUp = findViewById(R.id.textViewLoginSignUp);
        TextView textViewLoginAsSO = findViewById(R.id.textViewLoginAsSO);

        auth = FirebaseAuth.getInstance();
        userRolesRef = FirebaseDatabase.getInstance().getReference("UserRolesCus"); // Initialize the reference

        //Show Hide Password using Eye Icon
        ImageView imageViewShowHidePwd = findViewById(R.id.imageViewShowHidePwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextLoginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If Password is Visible then Hide it
                    editTextLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_psw);
                }
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextLoginEmail.getText().toString().trim();
                String password = editTextLoginPassword.getText().toString().trim();

                if (validateInput(email, password)) {
                    loginUser(email, password);
                }
            }
        });

        textViewLoginSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textViewLoginAsSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginAsStoreOwner.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateInput(String textEmail, String textPassword) {
        if (TextUtils.isEmpty(textEmail)) {
            showToast("Please enter your email.");
            editTextLoginEmail.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(textPassword)) {
            showToast("Please enter your password.");
            editTextLoginPassword.requestFocus();
            return false;
        } else {
            progressBar.setVisibility(View.VISIBLE);
            return true;
        }
    }

    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
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
        userRolesRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                if (role != null) {
                    if ("customer".equals(role)) {
                        // User is a Customer, navigate to CustomerMainActivity
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish(); // Close Login Activity
                    } else if ("storeOwner".equals(role)) {
                        // User is a Store Owner, show a message that they cannot login
                        showToast("Cannot login as a Store Owner.");
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
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            showToast("Already logged in!");
            // Check the user's role and navigate accordingly
            fetchUserRole(currentUser.getUid());
        } else {
            showToast("You can log in now.");
        }
    }
}
