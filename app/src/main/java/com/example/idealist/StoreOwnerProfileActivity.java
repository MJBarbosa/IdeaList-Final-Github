package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class StoreOwnerProfileActivity extends AppCompatActivity {

    private TextView textViewWelcomeSO, textViewFullNameSO, textViewEmailSO, textViewDoBSO, textViewGenderSO, textViewMobileSO, textViewStoreNameSO, textViewStoreLocSO;
    private ProgressBar progressBarSO;
    private String fullName, email, doB, gender, mobile, storeName, storeLoc;
    private ImageView imageView;
    private FirebaseAuth authProfileSO;
    private SwipeRefreshLayout swipeContainerSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_owner_profile);

        getSupportActionBar().setTitle("User Profile");

        swipeToRefreshSO();

        textViewWelcomeSO = findViewById(R.id.textViewShowWelcomeSO);
        textViewFullNameSO = findViewById(R.id.textViewShowFullNameSO);
        textViewEmailSO = findViewById(R.id.textViewShowEmailSO);
        textViewDoBSO = findViewById(R.id.textViewShowDoBSO);
        textViewGenderSO = findViewById(R.id.textViewShowGenderSO);
        textViewMobileSO = findViewById(R.id.textViewShowMobileSO);
        textViewStoreNameSO = findViewById(R.id.textViewShowStoreNameSO);
        textViewStoreLocSO = findViewById(R.id.textViewShowStoreLocSO);
        progressBarSO = findViewById(R.id.progressBarShowSO);

        //Set OnClickListener on ImageView to Open UploadProfilePicActivity
        imageView = findViewById(R.id.imageViewProfileDpSO);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreOwnerProfileActivity.this, UploadSOProfilePicActivity.class);
                startActivity(intent);
            }
        });

        authProfileSO = FirebaseAuth.getInstance();
        FirebaseUser firebaseUserSO = authProfileSO.getCurrentUser();

        if (firebaseUserSO == null) {
            Toast.makeText(StoreOwnerProfileActivity.this, "Something Went Wrong! User's Details Are Not Available at The Moment", Toast.LENGTH_LONG).show();
        } else {
            progressBarSO.setVisibility(View.VISIBLE);
            showStoreOwnerProfile(firebaseUserSO);
        }

    }

    private void swipeToRefreshSO() {//Look up for the swipe Container
        swipeContainerSO = findViewById(R.id.swipeContainerUploadSO);

        //Setup Refresh Listener which triggers new data loading
        swipeContainerSO.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Code to refresh goes here. Make sure to call swipeContainer.setRefreshing(false) once the refresh
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                swipeContainerSO.setRefreshing(false);
            }
        });

        //Configure refresh colors
        swipeContainerSO.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    private void showStoreOwnerProfile(FirebaseUser firebaseUserSO) {
        String userID = firebaseUserSO.getUid();

        //Extracting User Reference from Database for "Registered Store Owner Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetailsSO readUserDetailsSO = snapshot.getValue(ReadWriteUserDetailsSO.class);
                if (readUserDetailsSO != null) {
                    fullName = firebaseUserSO.getDisplayName();
                    email = firebaseUserSO.getEmail();
                    doB = readUserDetailsSO.dob;
                    gender = readUserDetailsSO.gender;
                    mobile = readUserDetailsSO.mobile;
                    storeName = readUserDetailsSO.storeName;
                    storeLoc = readUserDetailsSO.storeLocation;

                    textViewWelcomeSO.setText("Welcome, " + fullName + "!");
                    textViewFullNameSO.setText(fullName);
                    textViewEmailSO.setText(email);
                    textViewDoBSO.setText(doB);
                    textViewGenderSO.setText(gender);
                    textViewMobileSO.setText(mobile);
                    textViewStoreNameSO.setText(storeName);
                    textViewStoreLocSO.setText(storeLoc);

                    //Set User DP (After User Had Uploaded)
                    Uri uri = firebaseUserSO.getPhotoUrl();

                    //ImageViewer setImageURI() should not be Used with regular URIs. So we are using Picasso
                    Picasso.with(StoreOwnerProfileActivity.this).load(uri).into(imageView);
                } else {
                    Toast.makeText(StoreOwnerProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
                }
                progressBarSO.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoreOwnerProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
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

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(StoreOwnerProfileActivity.this);
        }else if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        }  else if (id == R.id.menuHomeSO) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmailSO) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettingsSO) {
            Toast.makeText(StoreOwnerProfileActivity.this, "menuSettingsSO", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePasswordSO) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuDeleteProfileSO) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(StoreOwnerProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(StoreOwnerProfileActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(StoreOwnerProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}