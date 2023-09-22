package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

public class StoreOwnerProfileActivity extends AppCompatActivity {

    private TextView textViewWelcomeSO, textViewFullNameSO, textViewEmailSO, textViewDoBSO, textViewGenderSO, textViewMobileSO, textViewStoreNameSO, textViewStoreLocSO;
    private ProgressBar progressBarSO;
    private String fullName, email, doB, gender, mobile, storeName, storeLoc;
    private ImageView imageView;
    private FirebaseAuth authProfileSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_owner_profile);

        getSupportActionBar().setTitle("User Profile");

        textViewWelcomeSO = findViewById(R.id.textViewShowWelcomeSO);
        textViewFullNameSO = findViewById(R.id.textViewShowFullNameSO);
        textViewEmailSO = findViewById(R.id.textViewShowEmailSO);
        textViewDoBSO = findViewById(R.id.textViewShowDoBSO);
        textViewGenderSO = findViewById(R.id.textViewShowGenderSO);
        textViewMobileSO = findViewById(R.id.textViewShowMobileSO);
        textViewStoreNameSO = findViewById(R.id.textViewShowStoreNameSO);
        textViewStoreLocSO = findViewById(R.id.textViewShowStoreLocSO);
        progressBarSO = findViewById(R.id.progressBarShowSO);

        authProfileSO = FirebaseAuth.getInstance();
        FirebaseUser firebaseUserSO = authProfileSO.getCurrentUser();

        if (firebaseUserSO == null) {
            Toast.makeText(StoreOwnerProfileActivity.this, "Something Went Wrong! User's Details Are Not Available at The Moment", Toast.LENGTH_LONG).show();
        } else {
            progressBarSO.setVisibility(View.VISIBLE);
            showStoreOwnerProfile(firebaseUserSO);
        }

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
}