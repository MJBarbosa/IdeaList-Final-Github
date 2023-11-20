package com.inventory.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.inventory.idealist.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetStoreTimeActivity extends AppCompatActivity {

    private TimePicker timePickerOpenTime;
    private TimePicker timePickerCloseTime;
    private FirebaseAuth authProfileSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_store_time);

        getSupportActionBar().setTitle("Set Store Time");

        timePickerOpenTime = findViewById(R.id.timePickerOpenTime);
        timePickerCloseTime = findViewById(R.id.timePickerCloseTime);
        Button buttonSaveTimes = findViewById(R.id.buttonSaveTimes);

        // Load and display stored open and close times, if available
        loadAndDisplayStoreTimes();

        buttonSaveTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int openHour = timePickerOpenTime.getHour();
                int openMinute = timePickerOpenTime.getMinute();
                int closeHour = timePickerCloseTime.getHour();
                int closeMinute = timePickerCloseTime.getMinute();

                // Format the open and close times as strings
                String openTime = String.format("%02d:%02d", openHour, openMinute);
                String closeTime = String.format("%02d:%02d", closeHour, closeMinute);

                // Save the times to Firebase
                saveStoreTimes(openTime, closeTime);
                Toast.makeText(SetStoreTimeActivity.this, "Set Store Time Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveStoreTimes(String openTime, String closeTime) {
        // Get the authenticated user's UID
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a reference to your Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StoreTimes");

        // Store the open and close times in the database under the user's UID as the main ID
        databaseReference.child(userUid).child("openTime").setValue(openTime);
        databaseReference.child(userUid).child("closeTime").setValue(closeTime);

        // You can also add success or error handling logic here
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
        if (!openTime.isEmpty()) {
            String[] openTimeParts = openTime.split(":");
            int openHour = Integer.parseInt(openTimeParts[0]);
            int openMinute = Integer.parseInt(openTimeParts[1]);
            timePickerOpenTime.setHour(openHour);
            timePickerOpenTime.setMinute(openMinute);
        }

        if (!closeTime.isEmpty()) {
            String[] closeTimeParts = closeTime.split(":");
            int closeHour = Integer.parseInt(closeTimeParts[0]);
            int closeMinute = Integer.parseInt(closeTimeParts[1]);
            timePickerCloseTime.setHour(closeHour);
            timePickerCloseTime.setMinute(closeMinute);
        }
    }

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
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(SetStoreTimeActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuHomeSO) {
            Intent intent = new Intent(SetStoreTimeActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(SetStoreTimeActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmailSO) {
            Intent intent = new Intent(SetStoreTimeActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettingsSO) {
            Toast.makeText(SetStoreTimeActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuSetStoreTime) {
            Intent intent = new Intent(SetStoreTimeActivity.this, SetStoreTimeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSetTransactionKey) {
            Intent intent = new Intent(SetStoreTimeActivity.this, SetTransactionKeyActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuChangePasswordSO) {
            Intent intent = new Intent(SetStoreTimeActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuDeleteProfileSO) {
            Intent intent = new Intent(SetStoreTimeActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(SetStoreTimeActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SetStoreTimeActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SetStoreTimeActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}