package com.inventory.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetTransactionKeyActivity extends AppCompatActivity {

    private FirebaseAuth authProfileSO;
    private FirebaseUser firebaseUserSO;
    private EditText editTextUserPwdSO, editTextSetNewTransactionKey;
    private TextView textViewAuthenticatedSO, currentTransactionKeyTextView;
    private ImageView imageViewHidePass;
    private ProgressBar progressBarSO;
    private String userPwdSO;
    private Button buttonReAuthenticateSO, buttonSetNewTransactionKey;
    private static final String TAG = "SetTransactionKeyActivity";
    private boolean isKeyVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_transaction_key);

        getSupportActionBar().setTitle("Set Transaction Key");

        progressBarSO = findViewById(R.id.progressBarSetTransactionKey);
        editTextUserPwdSO = findViewById(R.id.passwordEditViewAuthentication);
        currentTransactionKeyTextView = findViewById(R.id.currentTransactionKey);
        imageViewHidePass = findViewById(R.id.imageViewShowHidePwd);
        buttonReAuthenticateSO = findViewById(R.id.buttonTransactionKeyAuthentication);
        buttonSetNewTransactionKey = findViewById(R.id.buttonSetNewTransactionKey);
        editTextSetNewTransactionKey = findViewById(R.id.setNewTransactionKeyEditText);

        buttonSetNewTransactionKey.setOnClickListener(view -> {
            String newTransactionKey = editTextSetNewTransactionKey.getText().toString().trim();
            setNewTransactionKey(newTransactionKey);
        });

        // Set an initial state (hidden)
        hideTransactionKey();
        // Set an onClickListener for the imageView to toggle visibility
        imageViewHidePass.setOnClickListener(view -> toggleKeyVisibility());

        // Call a function to display the transaction key
        displayTransactionKey();


        //Disable Set New Transaction Key User Button Until User is Authenticated
        buttonSetNewTransactionKey.setEnabled(false);
        editTextSetNewTransactionKey.setEnabled(false);
        imageViewHidePass.setEnabled(false);


        authProfileSO = FirebaseAuth.getInstance();
        firebaseUserSO = authProfileSO.getCurrentUser();

        if (firebaseUserSO.equals("")) {
            Toast.makeText(SetTransactionKeyActivity.this, "Something Went Wrong!" + "User Details Are Not Available At The Moment.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SetTransactionKeyActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUserSO);
        }
    }

    private void displayTransactionKey() {
        // Retrieve the transaction key from the database for the currently authenticated user
        String transactionKey = getTransactionKeyFromDatabase();

        // Set the transaction key to the TextView
        currentTransactionKeyTextView.setText(transactionKey);
    }

    private String getTransactionKeyFromDatabase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userUid = currentUser.getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("Registered Store Owner Users")
                    .child(userUid);

            userRef.child("transactionKey").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String transactionKey = dataSnapshot.getValue(String.class);
                        updateTransactionKeyUI(transactionKey);
                    } else {
                        // Handle the case where the transaction key is not found
                        updateTransactionKeyUI("Transaction key not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error case
                    updateTransactionKeyUI("Database error");
                }
            });
        }

        return "Transaction key not found";
    }

    private void updateTransactionKeyUI(String transactionKey) {
        // Update the UI with the retrieved transaction key
        currentTransactionKeyTextView.setText(transactionKey);
    }

    private void toggleKeyVisibility() {
        if (isKeyVisible) {
            hideTransactionKey();
        } else {
            showTransactionKey();
        }
        isKeyVisible = !isKeyVisible; // Toggle the state
    }

    private void hideTransactionKey() {
        currentTransactionKeyTextView.setTransformationMethod(new PasswordTransformationMethod());
        imageViewHidePass.setImageResource(R.drawable.ic_hide_pwd); // Replace with your icon
    }

    private void showTransactionKey() {
        currentTransactionKeyTextView.setTransformationMethod(null);
        imageViewHidePass.setImageResource(R.drawable.ic_show_psw); // Replace with your icon
    }

    private void reAuthenticateUser(FirebaseUser firebaseUserSO) {
        buttonReAuthenticateSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdSO = editTextUserPwdSO.getText().toString();

                if (TextUtils.isEmpty(userPwdSO)) {
                    Toast.makeText(SetTransactionKeyActivity.this, "Password is Needed", Toast.LENGTH_SHORT).show();
                    editTextUserPwdSO.setError("Please Enter Your Current Password to Authenticate");
                    editTextUserPwdSO.requestFocus();
                } else {
                    progressBarSO.setVisibility(View.VISIBLE);

                    //ReAuthenticate User Now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUserSO.getEmail(), userPwdSO);

                    firebaseUserSO.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBarSO.setVisibility(View.GONE);

                                //Disable editText for Password.
                                editTextUserPwdSO.setEnabled(false);

                                //Enable Change Password Button. Disable Authenticate Button
                                buttonSetNewTransactionKey.setEnabled(true);
                                editTextSetNewTransactionKey.setEnabled(true);
                                imageViewHidePass.setEnabled(true);
                                buttonReAuthenticateSO.setEnabled(false);

                                //Set TextView to Show User is authenticated/verified
                                currentTransactionKeyTextView.setTextColor(Color.BLACK);
                                Toast.makeText(SetTransactionKeyActivity.this, "Password has Been Verified." + "Change Transaction Key Now", Toast.LENGTH_SHORT).show();

                                //Update Color of Change Password Button
                                //buttonDeleteUserSO.setBackgroundTintList(ContextCompat.getColorStateList(SetTransactionKeyActivity.this, R.color.dark_green));

                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(SetTransactionKeyActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBarSO.setVisibility(View.GONE);

                        }
                    });
                }
            }
        });
    }

    private void setNewTransactionKey(String newTransactionKey) {
        // Get the current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Reference to the user node in the "Registered Store Owner Users" in the database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("Registered Store Owner Users")
                    .child(currentUserId);

            // Set the new transaction key in the database
            userRef.child("transactionKey").setValue(newTransactionKey)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully set the new transaction key
                        Toast.makeText(this, "New transaction key set successfully", Toast.LENGTH_SHORT).show();
                        // Clear the EditText field
                        clearEditText();
                    })
                    .addOnFailureListener(e -> {
                        // Handle the case where setting the new transaction key failed
                        Toast.makeText(this, "Failed to set new transaction key", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearEditText() {
        // Clear the EditText field
        editTextSetNewTransactionKey.setText("");
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
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuHomeSO) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmailSO) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettingsSO) {
            Toast.makeText(SetTransactionKeyActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuSetStoreTime) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, SetStoreTimeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSetTransactionKey) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, SetTransactionKeyActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuChangePasswordSO) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuDeleteProfileSO) {
            Intent intent = new Intent(SetTransactionKeyActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(SetTransactionKeyActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(SetTransactionKeyActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SetTransactionKeyActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}