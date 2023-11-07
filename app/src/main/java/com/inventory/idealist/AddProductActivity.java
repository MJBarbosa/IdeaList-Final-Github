package com.inventory.idealist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inventory.idealist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private EditText editTextAddProductId, editTextAddProductName, editTextAddSuppName, editTextAddProductDesc,
            editTextAddQuantity, editTextAddPrice;
    private ProgressBar progressBarAddProduct;
    private Spinner spinnerAddCategory;
    private static final String TAG = "AddProductActivity";
    private static final int IMAGE_PICK_REQUEST = 1;
    private Uri selectedImageUri;
    private int generatedProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        getSupportActionBar().setTitle("Add Product Inventory");

        Toast.makeText(AddProductActivity.this, "You can Add Product now", Toast.LENGTH_LONG).show();

        progressBarAddProduct = findViewById(R.id.progressBarAddProduct);
        editTextAddProductId = findViewById(R.id.editTextAddProductId);
        editTextAddProductName = findViewById(R.id.editTextAddProductName);
        editTextAddSuppName = findViewById(R.id.editTextAddSuppName);
        editTextAddProductDesc = findViewById(R.id.editTextAddProductDesc);
        editTextAddQuantity = findViewById(R.id.editTextAddQuantity);
        editTextAddPrice = findViewById(R.id.editTextAddPrice);

        spinnerAddCategory = findViewById(R.id.spinnerAddCategory);

        Button buttonGenerateProductId = findViewById(R.id.buttonGenerateProductId); // Assuming you have a button to trigger this
        buttonGenerateProductId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatedProductId = generateTextRandomProductId();
                String productIdString = String.valueOf(generatedProductId);
                editTextAddProductId.setText(productIdString);
            }
        });

        // Define the list of categories
        String[] categories = {"T-Shirt", "Cap", "Vape", "Hoodie"};

        // Create an ArrayAdapter to populate the Spinner with the categories
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Set the dropdown layout style for the Spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter on the Spinner
        spinnerAddCategory.setAdapter(adapter);

        Button buttonAddProduct = findViewById(R.id.buttonAddProduct);

        // Check the user's role before allowing product addition
        checkUserRoleForProductAddition();

        // Add this inside your onCreate method
        ImageButton buttonSelectImage = findViewById(R.id.imageButtonAddProductImage);

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the device's file picker to select an image
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK_REQUEST);
            }
        });

        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain the entered data
                String textProductId = editTextAddProductId.getText().toString();
                String textProductName = editTextAddProductName.getText().toString();
                String textAddSuppName = editTextAddSuppName.getText().toString();
                String textAddProductDesc = editTextAddProductDesc.getText().toString();
                String textAddQuantity = editTextAddQuantity.getText().toString();
                String textAddPrice = editTextAddPrice.getText().toString();
                String selectedAddCategory = spinnerAddCategory.getSelectedItem().toString();

                TextInputLayout textInputLayoutSpinnerAddCategory = findViewById(R.id.textInputLayoutSpinnerAddCategory);

                // To clear any existing error:
                textInputLayoutSpinnerAddCategory.setError(null);

                if (TextUtils.isEmpty(textProductId)) {
                    Toast.makeText(AddProductActivity.this, "Please Enter Product ID", Toast.LENGTH_LONG).show();
                    editTextAddProductId.setError("Product ID is Required");
                    editTextAddProductId.requestFocus();
                } else if (TextUtils.isEmpty(textProductName)) {
                    Toast.makeText(AddProductActivity.this, "Please Enter Product Name", Toast.LENGTH_LONG).show();
                    editTextAddProductName.setError("Product Name is Required");
                    editTextAddProductName.requestFocus();
                } else if (TextUtils.isEmpty(textAddSuppName)) {
                    Toast.makeText(AddProductActivity.this, "Please Enter Supplier Name", Toast.LENGTH_LONG).show();
                    editTextAddSuppName.setError("Supplier Name is Required");
                    editTextAddSuppName.requestFocus();
                } else if (TextUtils.isEmpty(selectedAddCategory)) {
                    textInputLayoutSpinnerAddCategory.setError("Category is Required");
                } else if (TextUtils.isEmpty(textAddProductDesc)) {
                    Toast.makeText(AddProductActivity.this, "Please Enter Product Description", Toast.LENGTH_LONG).show();
                    editTextAddProductDesc.setError("Description is Required");
                    editTextAddProductDesc.requestFocus();
                } else if (TextUtils.isEmpty(textAddQuantity)) {
                    Toast.makeText(AddProductActivity.this, "Please Enter Quantity", Toast.LENGTH_LONG).show();
                    editTextAddQuantity.setError("Quantity is Required");
                    editTextAddQuantity.requestFocus();
                } else if (TextUtils.isEmpty(textAddPrice)) {
                    Toast.makeText(AddProductActivity.this, "Please Enter Your Password", Toast.LENGTH_LONG).show();
                    editTextAddPrice.setError("Password is Required");
                    editTextAddPrice.requestFocus();
                } else {
                    textInputLayoutSpinnerAddCategory.setError(null);
                    progressBarAddProduct.setVisibility(View.VISIBLE);
                    addProduct(textProductId, textProductName, textAddSuppName, selectedAddCategory, textAddProductDesc, textAddQuantity, textAddPrice);
                }
            }
        });
    }

    private int generateTextRandomProductId() {
        // You can generate a random integer within a specified range
        // For example, to generate an integer between 1000 and 9999 (inclusive):
        int min = 1000;
        int max = 9999;
        return new Random().nextInt(max - min + 1) + min;
    }

    // Add this method to handle the image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView imageViewProductImage = findViewById(R.id.imageButtonAddProductImage);
            imageViewProductImage.setImageURI(selectedImageUri);
        }
    }

    private void checkUserRoleForProductAddition() {
        String userRole = getUserRoleFromSharedPreferences(); // Replace this with your logic to get the user's role
        if (!"storeOwner".equals(userRole)) {
            // User is not a Store Owner, show an error message but keep the button enabled
            Toast.makeText(AddProductActivity.this, "Only Store Owners can add products.", Toast.LENGTH_LONG).show();
        }
    }

    // Replace this with your logic to get the user's role from SharedPreferences
    private String getUserRoleFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return prefs.getString("userRole", "");
    }

    private void addProduct(String textProductId, String textProductName, String textAddSuppName, String selectedAddCategory, String textAddProductDesc, String textAddQuantity, String textAddPrice) {
        // Create a reference to your Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductInventory");

        // Generate a random product ID
        String randomProductId = generateRandomProductId();

        // Get the authenticated user's UID
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a Product object with the provided information, including the userUid
        ReadWriteProductDetailsSO product = new ReadWriteProductDetailsSO(userUid, textProductId, textProductName, textAddSuppName, selectedAddCategory, textAddProductDesc, textAddQuantity, textAddPrice);

        // Store the product in the database under the user's UID as the main ID
        databaseReference.child(userUid).child(randomProductId).setValue(product)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Check if an image is selected
                            if (selectedImageUri != null) {
                                // Upload the image to Firebase Storage
                                uploadImageToStorage(selectedImageUri, textProductId, textProductName, textAddSuppName, selectedAddCategory, textAddProductDesc, textAddQuantity, textAddPrice);
                            }
                            // Product added successfully
                            Toast.makeText(AddProductActivity.this, "Product Added Successfully", Toast.LENGTH_LONG).show();
                            clearFields();
                            progressBarAddProduct.setVisibility(View.GONE);
                        } else {
                            // Handle the error here
                            Toast.makeText(AddProductActivity.this, "Failed to add product. Please try again.", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error adding product: " + task.getException());
                            progressBarAddProduct.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void uploadImageToStorage(Uri imageUri, final String textProductId, final String textProductName, final String textAddSuppName, final String selectedAddCategory, final String textAddProductDesc, final String textAddQuantity, final String textAddPrice) {
        // Create a storage reference
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("ProductImages"); // Updated path to "ProductImages"

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Ensure that productId is not null before creating the child reference
        if (textProductId != null) {
            // Create a reference to the image with the product ID as the name
            final StorageReference imageRef = storageRef.child(userUid).child(textProductId); // Include userUid in the path

            // Upload the image
            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        // Image uploaded successfully
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Get the download URL of the uploaded image
                                String imageUrl = uri.toString();

                                // Clear the selectedImageUri
                                selectedImageUri = null;

                                // Now you can handle the image URL as needed, for example, you can display it or use it in other ways
                                Toast.makeText(AddProductActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        // Handle the error
                        Toast.makeText(AddProductActivity.this, "Failed to upload image. Please try again.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error uploading image: " + task.getException());
                    }
                }
            });
        } else {
            // Handle the case where productId is null
            Toast.makeText(AddProductActivity.this, "Failed to generate product ID. Please try again.", Toast.LENGTH_LONG).show();
        }
    }



    private void addProductToDatabase(String productId, ReadWriteProductDetailsSO product) {
        // Create a reference to your Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductInventory");

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Store the product in the database under the user's UID as the main ID
        databaseReference.child(userUid).child(productId).setValue(product)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Product added successfully
                            Toast.makeText(AddProductActivity.this, "Product Added Successfully", Toast.LENGTH_LONG).show();
                            clearFields();
                            progressBarAddProduct.setVisibility(View.GONE);
                        } else {
                            // Handle the error here
                            Toast.makeText(AddProductActivity.this, "Failed to add product. Please try again.", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error adding product: " + task.getException());
                            progressBarAddProduct.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private String generateRandomProductId() {
        // You can generate a random ID using a combination of timestamp and a random number.
        // For example, you can use System.currentTimeMillis() to get the current timestamp
        // and append a random number to it. Here's a sample implementation:

        long timestamp = System.currentTimeMillis();
        UUID uuid = UUID.randomUUID(); // Generate a random UUID

        return "PID_" + timestamp + "_" + uuid.toString();
    }

    private void clearFields() {
        // Clear all the input fields after successfully adding the product
        editTextAddProductId.getText().clear();
        editTextAddProductName.getText().clear();
        editTextAddSuppName.getText().clear();
        editTextAddProductDesc.getText().clear();
        editTextAddQuantity.getText().clear();
        editTextAddPrice.getText().clear();
        spinnerAddCategory.setSelection(0); // Reset the spinner to the first category

        // Clear the selected image in the ImageButton
        ImageView imageViewProductImage = findViewById(R.id.imageButtonAddProductImage);
        imageViewProductImage.setImageResource(0); // Set to an empty image resource or transparent image as needed
        selectedImageUri = null; // Clear the selectedImageUri
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(AddProductActivity.this, ManageInventoryActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        }

        // Handle other menu items if needed
        // ...

        return super.onOptionsItemSelected(item);
    }

}
