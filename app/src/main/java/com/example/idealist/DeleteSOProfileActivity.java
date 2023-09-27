package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteSOProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfileSO;
    private FirebaseUser firebaseUserSO;
    private EditText editTextUserPwdSO;
    private TextView textViewAuthenticatedSO;
    private ProgressBar progressBarSO;
    private String userPwdSO;
    private Button buttonReAuthenticateSO, buttonDeleteUserSO;
    private static final String TAG = "DeleteSOProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_soprofile);

        getSupportActionBar().setTitle("Delete Your Profile");

        progressBarSO = findViewById(R.id.progressBarDeleteUserSO);
        editTextUserPwdSO = findViewById(R.id.editTextDeleteUserPwdSO);
        textViewAuthenticatedSO = findViewById(R.id.textViewDeleteUserPwdAuthenticatedSO);
        buttonReAuthenticateSO = findViewById(R.id.buttonDeleteUserPwdAuthenticateSO);
        buttonDeleteUserSO = findViewById(R.id.buttonDeleteUserSO);

        //Disable Delete User Button Until User is Authenticated
        buttonDeleteUserSO.setEnabled(false);

        authProfileSO = FirebaseAuth.getInstance();
        firebaseUserSO = authProfileSO.getCurrentUser();

        if (firebaseUserSO.equals("")) {
            Toast.makeText(DeleteSOProfileActivity.this, "Something Went Wrong!" + "User Details Are Not Available At The Moment.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteSOProfileActivity.this, StoreOwnerProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUserSO);
        }
    }

    //ReAuthenticate User Before Changing Password
    private void reAuthenticateUser(FirebaseUser firebaseUserSO) {
        buttonReAuthenticateSO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdSO = editTextUserPwdSO.getText().toString();

                if (TextUtils.isEmpty(userPwdSO)) {
                    Toast.makeText(DeleteSOProfileActivity.this, "Password is Needed", Toast.LENGTH_SHORT).show();
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
                                buttonDeleteUserSO.setEnabled(true);
                                buttonReAuthenticateSO.setEnabled(false);

                                //Set TextView to Show User is authenticated/verified
                                textViewAuthenticatedSO.setText("You are authenticated/verified." + "You can Delete Your Profile and Related Data Now!");
                                Toast.makeText(DeleteSOProfileActivity.this, "Password has Been Verified." + "Change Password Now", Toast.LENGTH_SHORT).show();

                                //Update Color of Change Password Button
                                buttonDeleteUserSO.setBackgroundTintList(ContextCompat.getColorStateList(DeleteSOProfileActivity.this, R.color.dark_green));

                                buttonDeleteUserSO.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(DeleteSOProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBarSO.setVisibility(View.GONE);

                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        //Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteSOProfileActivity.this);
        builder.setTitle("Delete User and Related Data?");
        builder.setMessage("Do You Really Want To Delete Your Profile and Related Data? This Action is Irreversible!");

        //Open Email Apps If User Clicks/Taps Continue Button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserDataSO(firebaseUserSO);
            }
        });

        //Return to User Profile Activity if User Presses Cancel Button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteSOProfileActivity.this, StoreOwnerProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Change the Color of Continue Button
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //show the AlertDialog
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUserSO.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    authProfileSO.signOut();
                    Toast.makeText(DeleteSOProfileActivity.this, "User Has Been Deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteSOProfileActivity.this, MainSOActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(DeleteSOProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBarSO.setVisibility(View.GONE);
            }
        });
    }

    //Delete all the Data of the User
    private void deleteUserDataSO(FirebaseUser firebaseUserSO) {

        if (firebaseUserSO.getPhotoUrl() != null) {
            //Delete Display Pic. Also check if the user has uploaded any pic before deleting
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUserSO.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteSOProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Delete Data From Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users");
        databaseReference.child(firebaseUserSO.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteSOProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (id == R.id.menuRefreshSO) {
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menuHomeSO) {
            Intent intent = new Intent(DeleteSOProfileActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(DeleteSOProfileActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(DeleteSOProfileActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettings) {
            Toast.makeText(DeleteSOProfileActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(DeleteSOProfileActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(DeleteSOProfileActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(DeleteSOProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteSOProfileActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(DeleteSOProfileActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}