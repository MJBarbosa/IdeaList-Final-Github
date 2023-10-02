package com.example.idealist;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UpdateProductActivity extends AppCompatActivity {

    private EditText editTextUpdateProductId, editTextUpdateProductName, editTextUpdateSuppName, editTextUpdateProductDesc,
            editTextUpdateQuantity, editTextUpdatePrice;
    private AutoCompleteTextView autoCompleteTextViewSearch;
    private ArrayAdapter<String> searchAdapter, adapter;
    private List<ReadWriteProductDetailsSO> productList;
    private ProgressBar progressBarUpdateProduct;
    private Spinner spinnerUpdateCategory;
    private static final String TAG = "UpdateProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        getSupportActionBar().setTitle("Update Product");

        // Initialize UI elements
        progressBarUpdateProduct = findViewById(R.id.progressBarUpdateProduct);
        editTextUpdateProductId = findViewById(R.id.editTextUpdateProductId);
        editTextUpdateProductName = findViewById(R.id.editTextUpdateProductName);
        editTextUpdateSuppName = findViewById(R.id.editTextUpdateSuppName);
        editTextUpdateProductDesc = findViewById(R.id.editTextUpdateProductDesc);
        editTextUpdateQuantity = findViewById(R.id.editTextUpdateQuantity);
        editTextUpdatePrice = findViewById(R.id.editTextUpdatePrice);
        autoCompleteTextViewSearch = findViewById(R.id.autoCompleteTextViewUpdateSearch);

        spinnerUpdateCategory = findViewById(R.id.spinnerUpdateCategory);
        String[] categories = {"T-Shirt", "Cap", "Vape", "Hoodie"};
        // Initialize the adapter here
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdateCategory.setAdapter(adapter);

        // Initialize the productList and set up the search functionality
        productList = new ArrayList<>();
        setupSearch();

        Button buttonUpdateProduct = findViewById(R.id.buttonUpdateProduct);
        buttonUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the update product logic here
                updateProduct();
            }
        });
    }

    private void setupSearch() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductInventory");

        // Attach a listener to populate the productList when data changes in the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    ReadWriteProductDetailsSO product = productSnapshot.getValue(ReadWriteProductDetailsSO.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }

                // Create an array of product names for the search suggestions
                List<String> productNames = new ArrayList<>();
                for (ReadWriteProductDetailsSO product : productList) {
                    productNames.add(product.getProductName());
                }

                // Create the adapter for search suggestions
                searchAdapter = new ArrayAdapter<>(UpdateProductActivity.this, android.R.layout.simple_dropdown_item_1line, productNames);
                autoCompleteTextViewSearch.setAdapter(searchAdapter);

                // Handle item selection from the search suggestions
                autoCompleteTextViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Get the selected product name
                        String selectedProductName = parent.getItemAtPosition(position).toString();

                        // Find the corresponding product in the productList
                        ReadWriteProductDetailsSO selectedProduct = null;
                        for (ReadWriteProductDetailsSO product : productList) {
                            if (product.getProductName().equals(selectedProductName)) {
                                selectedProduct = product;
                                break;
                            }
                        }

                        if (selectedProduct != null) {
                            // Populate the EditText fields with the selected product's data
                            editTextUpdateProductId.setText(selectedProduct.getProductId());
                            editTextUpdateProductName.setText(selectedProduct.getProductName());
                            editTextUpdateSuppName.setText(selectedProduct.getSupplierName());
                            editTextUpdateProductDesc.setText(selectedProduct.getProductDescription());
                            editTextUpdateQuantity.setText(selectedProduct.getQuantity());
                            editTextUpdatePrice.setText(selectedProduct.getPrice());
                            // Set the selected category in the spinner
                            spinnerUpdateCategory.setSelection(adapter.getPosition(selectedProduct.getCategory()));
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(UpdateProductActivity.this, "Error loading product data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product data: " + databaseError.getMessage());
            }
        });
    }

    private void updateProduct() {
        // Retrieve and validate the entered data
        String productId = editTextUpdateProductId.getText().toString();
        String productName = editTextUpdateProductName.getText().toString();
        String supplierName = editTextUpdateSuppName.getText().toString();
        String productDesc = editTextUpdateProductDesc.getText().toString();
        String quantity = editTextUpdateQuantity.getText().toString();
        String price = editTextUpdatePrice.getText().toString();
        String selectedCategory = spinnerUpdateCategory.getSelectedItem().toString();

        TextInputLayout textInputLayoutUpdateCategory = findViewById(R.id.textInputLayoutSpinnerUpdateCategory);
        textInputLayoutUpdateCategory.setError(null);

        if (TextUtils.isEmpty(productId)) {
            Toast.makeText(this, "Please enter Product ID", Toast.LENGTH_LONG).show();
            editTextUpdateProductId.setError("Product ID is required");
            editTextUpdateProductId.requestFocus();
        } else if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, "Please enter Product Name", Toast.LENGTH_LONG).show();
            editTextUpdateProductName.setError("Product Name is required");
            editTextUpdateProductName.requestFocus();
        } else if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, "Please enter Supplier Name", Toast.LENGTH_LONG).show();
            editTextUpdateSuppName.setError("Supplier Name is required");
            editTextUpdateSuppName.requestFocus();
        } else if (TextUtils.isEmpty(selectedCategory)) {
            textInputLayoutUpdateCategory.setError("Category is required");
        } else if (TextUtils.isEmpty(productDesc)) {
            Toast.makeText(this, "Please enter Product Description", Toast.LENGTH_LONG).show();
            editTextUpdateProductDesc.setError("Description is required");
            editTextUpdateProductDesc.requestFocus();
        } else if (TextUtils.isEmpty(quantity)) {
            Toast.makeText(this, "Please enter Quantity", Toast.LENGTH_LONG).show();
            editTextUpdateQuantity.setError("Quantity is required");
            editTextUpdateQuantity.requestFocus();
        } else if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Please enter Price", Toast.LENGTH_LONG).show();
            editTextUpdatePrice.setError("Price is required");
            editTextUpdatePrice.requestFocus();
        } else {
            textInputLayoutUpdateCategory.setError(null);
            progressBarUpdateProduct.setVisibility(View.VISIBLE);

            // Perform the update operation in the Firebase database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductInventory");
            // Use the existing product key (productId) for the update
            databaseReference.child(productId).setValue(new ReadWriteProductDetailsSO(productId, productName, supplierName, selectedCategory, productDesc, quantity, price, selectedCategory))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Product updated successfully
                                Toast.makeText(UpdateProductActivity.this, "Product Updated Successfully", Toast.LENGTH_LONG).show();
                                clearFields();
                                progressBarUpdateProduct.setVisibility(View.GONE);
                            } else {
                                // Handle the error here
                                Toast.makeText(UpdateProductActivity.this, "Failed to update product. Please try again.", Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error updating product: " + task.getException());
                                progressBarUpdateProduct.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void clearFields() {
        // Clear all the input fields after successfully updating the product
        editTextUpdateProductId.getText().clear();
        editTextUpdateProductName.getText().clear();
        editTextUpdateSuppName.getText().clear();
        editTextUpdateProductDesc.getText().clear();
        editTextUpdateQuantity.getText().clear();
        editTextUpdatePrice.getText().clear();
        spinnerUpdateCategory.setSelection(0); // Reset the spinner to the first category
    }
}
