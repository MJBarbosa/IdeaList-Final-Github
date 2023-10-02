package com.example.idealist;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.idealist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddProductActivity extends AppCompatActivity {

    private EditText editTextAddProductId, editTextAddProductName, editTextAddSuppName, editTextAddProductDesc,
            editTextAddQuantity, editTextAddPrice;
    private ProgressBar progressBarAddProduct;
    private Spinner spinnerAddCategory;
    private static final String TAG= "AddProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        getSupportActionBar().setTitle("Add Product Inventory");

        Toast.makeText(AddProductActivity.this, "You can Add Product now", Toast.LENGTH_LONG).show();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true); // Enable offline persistence

        progressBarAddProduct = findViewById(R.id.progressBarAddProduct);
        editTextAddProductId = findViewById(R.id.editTextAddProductId);
        editTextAddProductName = findViewById(R.id.editTextAddProductName);
        editTextAddSuppName = findViewById(R.id.editTextAddSuppName);
        editTextAddProductDesc = findViewById(R.id.editTextAddProductDesc);
        editTextAddQuantity = findViewById(R.id.editTextAddQuantity);
        editTextAddPrice = findViewById(R.id.editTextAddPrice);

        // Initialize the product ID counter from SharedPreferences
        int productIDCounter = SharedPreferencesHelper.getProductIDCounter(AddProductActivity.this);

        // Set the auto-incremented product ID in the editText
        editTextAddProductId.setText(String.valueOf(productIDCounter));

        spinnerAddCategory = findViewById(R.id.spinnerAddCategory);

        // Define the list of categories
        String[] categories = {"T-Shirt", "Cap", "Vape", "Hoodie"};

// Create an ArrayAdapter to populate the Spinner with the categories
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

// Set the dropdown layout style for the Spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Set the ArrayAdapter on the Spinner
        spinnerAddCategory.setAdapter(adapter);


        Button buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Obtain the entered data
                String textProductId = editTextAddProductId.getText().toString();
                // Increment the product ID counter in SharedPreferences
                SharedPreferencesHelper.incrementProductIDCounter(AddProductActivity.this);
                String textProductName = editTextAddProductName.getText().toString();
                String textAddSuppName = editTextAddSuppName.getText().toString();
                String textAddProductDesc = editTextAddProductDesc.getText().toString();
                String textAddQuantity = editTextAddQuantity.getText().toString();
                String textAddPrice = editTextAddPrice.getText().toString();
                String selectedAddCategory = spinnerAddCategory.getSelectedItem().toString();

                TextInputLayout textInputLayoutSpinnerAddCategory = findViewById(R.id.textInputLayoutSpinnerAddCategory);

                // To clear any existing error:
                textInputLayoutSpinnerAddCategory.setError(null);

                if (TextUtils.isEmpty(textProductId)){
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

    public static class SharedPreferencesHelper {
        private static final String PREFS_NAME = "MyAppPrefs";
        private static final String PRODUCT_ID_COUNTER_KEY = "product_id_counter";

        public static int getProductIDCounter(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getInt(PRODUCT_ID_COUNTER_KEY, 0);
        }

        public static void incrementProductIDCounter(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int currentCounter = prefs.getInt(PRODUCT_ID_COUNTER_KEY, 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(PRODUCT_ID_COUNTER_KEY, currentCounter + 1);
            editor.apply();
        }
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
        int randomSuffix = (int) (Math.random() * 1000); // Generate a random number between 0 and 999

        return "PID_" + timestamp + "_" + randomSuffix;
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
    }

}
