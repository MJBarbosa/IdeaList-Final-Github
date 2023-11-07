package com.inventory.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import com.inventory.idealist.R;
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
            loadAndDisplayStoreTimes();
        }
    }

    private void swipeToRefreshSO() {
        swipeContainerSO = findViewById(R.id.swipeContainerUploadSO);

        swipeContainerSO.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Code to refresh goes here.
                // You don't need to restart the activity, just fetch the data.
                showStoreOwnerProfile(authProfileSO.getCurrentUser());
                swipeContainerSO.setRefreshing(false);
            }
        });

        swipeContainerSO.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    private void showStoreOwnerProfile(FirebaseUser firebaseUserSO) {
        String userID = firebaseUserSO.getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users").child(userID);
        referenceProfile.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    // Set User DP (After User Had Uploaded)
                    Uri uri = firebaseUserSO.getPhotoUrl();
                    Picasso.get().load(uri).into(imageView);
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

    private void loadAndDisplayStoreTimes() {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StoreTimes").child(userUid);

        // Read the stored open and close times from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String openTime = dataSnapshot.child("openTime").getValue(String.class);
                    String closeTime = dataSnapshot.child("closeTime").getValue(String.class);

                    // Display the stored times in TimePicker
                    displayStoreTimes(openTime, closeTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void displayStoreTimes(String openTime, String closeTime) {
        if (!openTime.isEmpty() && !closeTime.isEmpty()) {
            // Split open and close times
            String[] openTimeParts = openTime.split(":");
            int openHour = Integer.parseInt(openTimeParts[0]);
            int openMinute = Integer.parseInt(openTimeParts[1]);

            String[] closeTimeParts = closeTime.split(":");
            int closeHour = Integer.parseInt(closeTimeParts[0]);
            int closeMinute = Integer.parseInt(closeTimeParts[1]);

            // Format open and close times
            String formattedOpenTime = String.format("%02d:%02d %s", openHour % 12, openMinute, openHour >= 12 ? "PM" : "AM");
            String formattedCloseTime = String.format("%02d:%02d %s", closeHour % 12, closeMinute, closeHour >= 12 ? "PM" : "AM");

            // Combine open and close times and set it in the TextView
            String combinedTimes = "Open Time: " + formattedOpenTime + " - Close Time: " + formattedCloseTime;

            // Update the TextView to display the combined times
            TextView timesDisplay = findViewById(R.id.textViewShowSetStoreTimeSO);
            timesDisplay.setText(combinedTimes);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu_so, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (id == R.id.menuRefreshSO) {
            showStoreOwnerProfile(authProfileSO.getCurrentUser());
        } else if (id == R.id.menuHomeSO) {
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
        } else if (id == R.id.menuSetStoreTime) {
            Intent intent = new Intent(StoreOwnerProfileActivity.this, SetStoreTimeActivity.class);
            startActivity(intent);
            finish();
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(StoreOwnerProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
