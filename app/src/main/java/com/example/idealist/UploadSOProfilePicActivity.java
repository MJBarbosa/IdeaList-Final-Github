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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadSOProfilePicActivity extends AppCompatActivity {

    private ProgressBar progressBarSO;
    private ImageView imageViewUploadPicSO;
    private FirebaseAuth authProfileSO;
    private StorageReference storageReferenceSO;
    private FirebaseUser firebaseUserSO;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImageSO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_soprofile_pic);

        getSupportActionBar().setTitle("Upload Store Owner Profile Picture");

        Button buttonUploadPicChoose = findViewById(R.id.uploadPicChooseButtonSO);
        Button buttonUploadPic = findViewById(R.id.uploadPicButtonSO);
        progressBarSO = findViewById(R.id.progressBarUploadPicSO);
        imageViewUploadPicSO = findViewById(R.id.imageViewProfileDpSO);

        authProfileSO = FirebaseAuth.getInstance();
        firebaseUserSO = authProfileSO.getCurrentUser();

        storageReferenceSO = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUserSO.getPhotoUrl();

        Picasso.get().load(uri).into(imageViewUploadPicSO);

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
                progressBarSO.setVisibility(View.VISIBLE);
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
            uriImageSO = data.getData();
            imageViewUploadPicSO.setImageURI(uriImageSO);
        }
    }

    private void UploadPic() {
        if (uriImageSO != null) {
            //Save the Image with uid of the Currently logged user
            StorageReference fileReference = storageReferenceSO.child(authProfileSO.getCurrentUser().getUid() + "/displaypic." + getFileExtension(uriImageSO));

            //Upload image to Storage
            fileReference.putFile(uriImageSO).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUserSO = authProfileSO.getCurrentUser();

                            //Finally set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUserSO.updateProfile(profileUpdates);
                        }
                    });

                    progressBarSO.setVisibility(View.GONE);
                    Toast.makeText(UploadSOProfilePicActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UploadSOProfilePicActivity.this, StoreOwnerProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadSOProfilePicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBarSO.setVisibility(View.GONE);
            Toast.makeText(UploadSOProfilePicActivity.this, "No File Selected!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(UploadSOProfilePicActivity.this, MainSOActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateProfileSO) {
            Intent intent = new Intent(UploadSOProfilePicActivity.this, UpdateSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuUpdateEmail) {
            Intent intent = new Intent(UploadSOProfilePicActivity.this, UpdateSOEmailActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuSettings) {
            Toast.makeText(UploadSOProfilePicActivity.this, "menuSettings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menuChangePassword) {
            Intent intent = new Intent(UploadSOProfilePicActivity.this, ChangeSOPasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuDeleteProfile) {
            Intent intent = new Intent(UploadSOProfilePicActivity.this, DeleteSOProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menuLogoutSO) {
            authProfileSO.signOut();
            Toast.makeText(UploadSOProfilePicActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadSOProfilePicActivity.this, LoginAsStoreOwner.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after Logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UploadSOProfilePicActivity.this, "Something Went Wrong!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}