package com.example.idealist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.ContentHandler;
import java.net.URI;

public class UploadProfilePicActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        getSupportActionBar().setTitle("Upload Profile Picture");

        Button buttonUploadPicChoose = findViewById(R.id.uploadPicChooseButton);
        Button buttonUploadPic = findViewById(R.id.uploadPicButton);
        progressBar = findViewById(R.id.progressBarUploadPic);
        imageViewUploadPic = findViewById(R.id.imageViewProfileDp);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        //Set User's current DP in ImageView (if upload already). We will Picasso since ImageViewer setImage
        //Regular URIs.
        Picasso.with(UploadProfilePicActivity.this).load(uri).into(imageViewUploadPic);

        //Choose Image to Upload
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Upload Image
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPic();
            }
        });
    }




    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }
    }

    private void UploadPic() {
        if (uriImage != null) {
            //Save the Image with uid of the Currently logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "," + getFileExtension(uriImage));

            //Upload image to Storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            //Finally set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UploadProfilePicActivity.this, UsersProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this, "No File Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //Obtain File Extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
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
        } else if (id == R.id.menuUpdateProfile) {
            Intent intent = new Intent(UploadProfilePicActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menuUpdateEmail) {
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
            Toast.makeText(UploadProfilePicActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadProfilePicActivity.this, Login.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UploadProfilePicActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}