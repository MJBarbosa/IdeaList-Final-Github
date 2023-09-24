package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UsersProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome, textViewFirstName, textViewLastName, textViewAddress, textViewEmail, textViewDoB, textViewGender, textViewMobile;
    private ProgressBar progressBar;
    private String firstName, lastName, address, email, doB, gender, mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        getSupportActionBar().setTitle("User Profile");
        textViewWelcome = findViewById(R.id.textViewShowWelcome);
        textViewFirstName = findViewById(R.id.textViewShowFirstName);
        textViewLastName = findViewById(R.id.textViewShowLastName);
        textViewAddress = findViewById(R.id.textViewShowAddress);
        textViewEmail = findViewById(R.id.textViewShowEmail);
        textViewDoB = findViewById(R.id.textViewShowDoB);
        textViewGender = findViewById(R.id.textViewShowGender);
        textViewMobile = findViewById(R.id.textViewShowMobile);
        progressBar = findViewById(R.id.progressBarShow);

        //Set OnClickListener on ImageView to Open UploadProfilePicActivity
        imageView = findViewById(R.id.imageViewProfileDp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(UsersProfileActivity.this, "Something Went Wrong! User's Details Are Not Available at The Moment", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUsersProfile(firebaseUser);
        }
    }

    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UsersProfileActivity.this);
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

    private void showUsersProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting User Reference from Database for "Registered Store Owner Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    firstName = firebaseUser.getDisplayName();
                    lastName = readUserDetails.lName;
                    address = readUserDetails.address;
                    email = firebaseUser.getEmail();
                    doB = readUserDetails.dob;
                    gender = readUserDetails.gender;
                    mobile = readUserDetails.mobile;

                    textViewWelcome.setText("Welcome, " + firstName + "!");
                    textViewFirstName.setText(firstName);
                    textViewLastName.setText(lastName);
                    textViewAddress.setText(address);
                    textViewEmail.setText(email);
                    textViewDoB.setText(doB);
                    textViewGender.setText(gender);
                    textViewMobile.setText(mobile);

                    //Set User DP (After User Had Uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    //ImageViewer setImageURI() should not be Used with regular URIs. So we are using Picasso
                    Picasso.with(UsersProfileActivity.this).load(uri).into(imageView);
                } else {
                    Toast.makeText(UsersProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsersProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
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
        } /*else if (id == R.id.menuUpdateProfile) {
            Intent intent = new Intent(UsersProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(UsersProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuSettings) {
            Toast.makeText(UsersProfileActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(UsersProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(UsersProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } */else if (id == R.id.menuLogout) {
            authProfile.signOut();
            Toast.makeText(UsersProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UsersProfileActivity.this, Login.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UsersProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}