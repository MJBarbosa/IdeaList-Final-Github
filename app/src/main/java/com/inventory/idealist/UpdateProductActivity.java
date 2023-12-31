package com.inventory.idealist;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.inventory.idealist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateProductActivity extends AppCompatActivity {

    private EditText editTextUpdateSuppName, editTextUpdateProductDesc,
            editTextUpdateQuantity, editTextUpdatePrice;
    private TextView editTextUpdateProductId, editTextUpdateProductName;
    private AutoCompleteTextView autoCompleteTextViewSearch;
    private ArrayAdapter<String> searchAdapter, adapter;
    private List<Product> productList;
    private ProgressBar progressBarUpdateProduct;
    private Spinner spinnerUpdateCategory;
    private static final String TAG = "UpdateProductActivity";
    private static final int IMAGE_PICK_REQUEST = 1;
    private Uri selectedImageUri;
    private Product selectedProduct;
    private DatabaseReference productRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        getSupportActionBar().setTitle("Update Product");
        FirebaseApp.initializeApp(this);

        // Initialize UI elements
        progressBarUpdateProduct = findViewById(R.id.progressBarUpdateProduct);
        editTextUpdateProductId = findViewById(R.id.textViewProductIdValue);
        editTextUpdateProductName = findViewById(R.id.textViewProductNameValue);
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

        // Initialize the searchAdapter
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewSearch.setAdapter(searchAdapter);
        setupSearch();
        fetchDataFromIntentAndFirebase();


        ImageView buttonSelectImage = findViewById(R.id.imageViewUpdateProductImage);

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the device's file picker to select an image
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK_REQUEST);
            }
        });


        Button buttonUpdateProduct = findViewById(R.id.buttonUpdateProduct);
        buttonUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the update product logic here
                updateProduct();

                // Clear the fields after the update
                clearFields();
            }
        });
        checkUserRoleForAccess(getCurrentUserUid());
    }

    private void fetchDataFromIntentAndFirebase() {
        // Check if the intent contains the userUid and scannedContent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userUid") && intent.hasExtra("scannedContent")) {
            String userUid = intent.getStringExtra("userUid");
            String scannedContent = intent.getStringExtra("scannedContent");

            // Check if userUid and scannedContent are not null or empty
            if (userUid != null && !userUid.isEmpty() && scannedContent != null && !scannedContent.isEmpty()) {
                // Parse the scanned content to extract product information
                Product product = parseScannedContent(scannedContent);

                if (product != null) {
                    // Initialize your UI elements outside the callback
                    editTextUpdateSuppName = findViewById(R.id.editTextUpdateSuppName);
                    editTextUpdateQuantity = findViewById(R.id.editTextUpdateQuantity);
                    editTextUpdateProductId = findViewById(R.id.textViewProductIdValue);
                    autoCompleteTextViewSearch = findViewById(R.id.autoCompleteTextViewUpdateSearch);

                    // Set the product ID in the appropriate EditText
                    editTextUpdateProductId.setText(product.getProductId());

                    // Auto-fill the fields with product information
                    editTextUpdateProductDesc.setText(product.getProductDescription());
                    editTextUpdatePrice.setText(product.getPrice());
                    spinnerUpdateCategory.setSelection(adapter.getPosition(product.getCategory()));
                    editTextUpdateProductName.setText(product.getProductName());
                    autoCompleteTextViewSearch.setText(product.getProductName());

                    onQRCodeScanned(product.getProductName());

                    // Display product image after fetching data
                    ImageView imageViewProductImage = findViewById(R.id.imageViewUpdateProductImage);
                    displayProductImage(userUid, product.getProductId(), imageViewProductImage);

                    // Check user role for access
                    checkUserRoleForAccess(userUid);
                }
            }
        }
    }


    private Product parseScannedContent(String scannedContent) {
        // Implement your logic to parse the scanned content and create a Product object
        // Extract Product ID, Name, Description, Price, Category from the scanned content
        // Create a Product object with the extracted data

        // Example parsing logic:
        String productId = extractValueFromScannedContent(scannedContent, "Product ID:");
        String productName = extractValueFromScannedContent(scannedContent, "Product Name:");
        String productDesc = extractValueFromScannedContent(scannedContent, "Product Description:");
        String price = extractValueFromScannedContent(scannedContent, "Price:");
        String category = extractValueFromScannedContent(scannedContent, "Category:");

        // Create a Product object and populate it with the parsed data
        Product product = new Product();
        product.setProductId(productId);
        product.setProductName(productName);
        product.setProductDescription(productDesc);
        product.setPrice(price);
        product.setCategory(category);

        return product;
    }



    private String extractValueFromScannedContent(String scannedContent, String key) {
        // Implement your logic to extract the value associated with a key in the scanned content
        // Search for the key in the scanned content and return the associated value
        // Example logic:
        String[] lines = scannedContent.split("\n");
        for (String line : lines) {
            if (line.startsWith(key)) {
                return line.substring(key.length()).trim();
            }
        }
        return "";
    }


    private void checkUserRoleForAccess(String userUid) {
        DatabaseReference userRolesRef = FirebaseDatabase.getInstance().getReference("UserRoles")
                .child(getCurrentUserUid())
                .child("role");

        userRolesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.getValue(String.class);
                    if ("storeOwner".equals(userRole)) {
                        // User has the "storeOwner" role, allow access
                        setupSearch();
                    } else {
                        // User does not have the "storeOwner" role, show an error message
                        Toast.makeText(UpdateProductActivity.this, "Only Store Owners can access this feature.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // User role data does not exist, handle it as needed
                    //Toast.makeText(UpdateProductActivity.this, "User role not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(UpdateProductActivity.this, "Error checking user role.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error checking user role: " + databaseError.getMessage());
            }
        });
    }


    // Add this method to handle the image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView imageViewProductImage = findViewById(R.id.imageViewUpdateProductImage);
            imageViewProductImage.setImageURI(selectedImageUri);
        }
    }


    private void setupSearch() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProductInventory").child(getCurrentUserUid());

        // Attach a listener to populate the productList when data changes in the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Check if the data is a HashMap or a Product object
                    if (productSnapshot.getValue() instanceof HashMap) {
                        // Handle data stored as HashMap (if needed)
                        // You can extract data from the HashMap and create a Product object
                        // Example:
                        HashMap<String, Object> productData = (HashMap<String, Object>) productSnapshot.getValue();
                        Product product = new Product();
                        product.setProductId((String) productData.get("productId"));
                        product.setProductName((String) productData.get("productName"));
                        product.setSupplierName((String) productData.get("supplierName"));
                        product.setCategory((String) productData.get("category"));
                        product.setProductDescription((String) productData.get("productDescription"));
                        product.setPrice((String) productData.get("price"));
                        product.setQuantity((String) productData.get("quantity"));
                        product.setUserUid(getCurrentUserUid()); // Set the userUid
                        // Populate other fields similarly

                        // Add the product to the list
                        productList.add(product);
                    } else if (productSnapshot.getValue() instanceof Product) {
                        // Handle data stored as Product object
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            productList.add(product);
                        }
                    } else {
                        Log.e(TAG, "Unexpected data type: " + productSnapshot.getValue().getClass().getSimpleName());
                    }
                }

                // Clear the searchAdapter before adding new items
                searchAdapter.clear();

                // Create an array of product names for the search suggestions
                List<String> productNames = new ArrayList<>();
                for (Product product : productList) {
                    productNames.add(product.getProductName());
                }

                // Add product names to the searchAdapter
                searchAdapter.addAll(productNames);

                // Notify the adapter that the data has changed
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(UpdateProductActivity.this, "Error loading product data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product data: " + databaseError.getMessage());
            }
        });

        // Handle item selection from the search suggestions
        autoCompleteTextViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name
                String selectedProductName = parent.getItemAtPosition(position).toString();

                // Call the method to populate UI elements with the selected product's data
                populateUIWithProductData(selectedProductName);
            }
        });

        // Handle text change in autoCompleteTextViewSearch
        autoCompleteTextViewSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this functionality
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed for this functionality
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if the text matches a product name and call the method to populate UI elements
                String productName = s.toString();
                if (!productName.isEmpty()) {
                    populateUIWithProductData(productName);
                }
            }
        });
    }

    // Inside your activity
    private AlertDialog alertDialog;

    // Call this method when you want to show the search dialog (e.g., after scanning a QR code)
    private void onQRCodeScanned(String scannedContent) {
        showSearchDialog();
        AutoCompleteTextView autoCompleteTextViewSearch = alertDialog.findViewById(R.id.autoCompleteTextViewSearch);

        // Set the scanned content as the text in autoCompleteTextViewSearch
        if (autoCompleteTextViewSearch != null) {
            autoCompleteTextViewSearch.setText(scannedContent);
        }
    }


    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_search, null);
        builder.setView(dialogView);

        AutoCompleteTextView autoCompleteTextViewSearch = dialogView.findViewById(R.id.autoCompleteTextViewSearch);
        Button buttonPopulateDetails = dialogView.findViewById(R.id.buttonPopulateDetails);

        // Create an ArrayAdapter for the autoCompleteTextViewSearch using your searchAdapter
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewSearch.setAdapter(searchAdapter);

        // Set up the autoCompleteTextViewSearch and buttonPopulateDetails as you did in setupSearch
        // (add item click listener and text change listener to autoCompleteTextViewSearch)

        // Set up the button click listener to populate details
        buttonPopulateDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProductName = autoCompleteTextViewSearch.getText().toString();
                populateUIWithProductData(selectedProductName);

                // Dismiss the dialog after populating details
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }


    // Method to populate UI elements with product data based on the product name
    private void populateUIWithProductData(String productName) {
        // Find the corresponding product in the productList
        Product selectedProduct = findProductByName(productName);

        if (selectedProduct != null) {
            // Populate the editText fields with the selected product's data
            editTextUpdateSuppName.setText(selectedProduct.getSupplierName());
            editTextUpdateProductDesc.setText(selectedProduct.getProductDescription());
            editTextUpdateQuantity.setText(selectedProduct.getQuantity());
            editTextUpdatePrice.setText(selectedProduct.getPrice());
            spinnerUpdateCategory.setSelection(adapter.getPosition(selectedProduct.getCategory()));

            // Update the TextViews for productId and productName
            TextView textViewProductIdValue = findViewById(R.id.textViewProductIdValue);
            TextView textViewProductNameValue = findViewById(R.id.textViewProductNameValue);

            textViewProductIdValue.setText(selectedProduct.getProductId());
            textViewProductNameValue.setText(selectedProduct.getProductName());

            // Display the product image
            ImageView imageViewProductImage = findViewById(R.id.imageViewUpdateProductImage);
            displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProductImage);
        } else {
            Log.e(TAG, "Selected product is null.");
        }
    }




    private String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }

    // Find a product by its name
    private Product findProductByName(String productName) {
        for (Product product : productList) {
            if (product.getProductName().equals(productName)) {
                return product;
            }
        }
        return null;
    }


    private void updateProduct() {
        // Retrieve and validate the entered data
        String productName = editTextUpdateProductName.getText().toString();
        String supplierName = editTextUpdateSuppName.getText().toString();
        String productDesc = editTextUpdateProductDesc.getText().toString();
        String quantity = editTextUpdateQuantity.getText().toString();
        String price = editTextUpdatePrice.getText().toString();
        String selectedCategory = spinnerUpdateCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(supplierName) || TextUtils.isEmpty(selectedCategory) ||
                TextUtils.isEmpty(productDesc) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(price)) {
            // Display an error message if any of the fields are empty
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // All fields are valid, proceed with updating the product
        progressBarUpdateProduct.setVisibility(View.VISIBLE);

        // Find the selected product in the productList (assuming it's stored there)
        Product selectedProduct = findProductByName(productName);

        if (selectedProduct != null) {
            String userUid = selectedProduct.getUserUid();
            String productId = selectedProduct.getProductId();

            if (userUid != null && productId != null) {
                // Query the database to fetch the productId for the selected product
                DatabaseReference productsReference = FirebaseDatabase.getInstance().getReference("ProductInventory").child(userUid);

                if (selectedImageUri != null) {
                    // Update the product's image in Firebase Storage
                    updateImageInStorage(selectedImageUri, selectedProduct.getProductId());
                }

                productsReference.orderByChild("productId").equalTo(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Get the reference to the selected product
                            DataSnapshot productSnapshot = dataSnapshot.getChildren().iterator().next();
                            DatabaseReference updateReference = productSnapshot.getRef();

                            // Update the product's information
                            selectedProduct.setSupplierName(supplierName);
                            selectedProduct.setCategory(selectedCategory);
                            selectedProduct.setProductDescription(productDesc);
                            selectedProduct.setQuantity(quantity);
                            selectedProduct.setPrice(price);

                            // Update the product in the database
                            updateReference.setValue(selectedProduct)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBarUpdateProduct.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                // Product updated successfully
                                                Toast.makeText(UpdateProductActivity.this, "Product Updated Successfully", Toast.LENGTH_LONG).show();
                                            } else {
                                                // Handle the error here
                                                Toast.makeText(UpdateProductActivity.this, "Failed to update product. Please try again.", Toast.LENGTH_LONG).show();
                                                Log.e(TAG, "Error updating product: " + task.getException());
                                            }
                                        }
                                    });
                        } else {
                            // Handle the case where the selected product is not found in the database
                            Toast.makeText(UpdateProductActivity.this, "Selected product not found in the database.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(UpdateProductActivity.this, "Error querying the database.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error querying the database: " + databaseError.getMessage());
                    }
                });
            } else {
                // Handle the case where userUid or productId is null
                Toast.makeText(UpdateProductActivity.this, "Invalid product data. userUid or productId is null.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "userUid or productId is null.");
            }
        } else {
            Toast.makeText(UpdateProductActivity.this, "Selected product not found.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateImageInStorage(Uri imageUri, final String productId) {
        // Check if an image is selected
        if (imageUri == null) {
            // Handle the case where no image is selected
            Toast.makeText(UpdateProductActivity.this, "Please select an image for the product.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a storage reference
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("ProductImages"); // Updated path to "ProductImages"

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Ensure that productId is not null before creating the child reference
        if (productId != null) {
            // Create a reference to the image with the product ID as the name
            final StorageReference imageRef = storageRef.child(userUid).child(productId); // Include userUid in the path

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

                                Toast.makeText(UpdateProductActivity.this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        // Handle the error
                        Toast.makeText(UpdateProductActivity.this, "Failed to upload image. Please try again.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error uploading image: " + task.getException());
                    }
                }
            });
        } else {
            // Handle the case where productId is null
            Toast.makeText(UpdateProductActivity.this, "Failed to generate product ID. Please try again.", Toast.LENGTH_LONG).show();
        }
    }


    private void displayProductImage(String userUid, String productId, ImageView imageView) {
        // Reference to the product image in Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("ProductImages")
                .child(userUid)
                .child(productId); // Assuming images are in JPG format

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image using Picasso
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.default_product_image)
                        .error(R.drawable.default_product_image)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to download and display the image
                imageView.setImageResource(R.drawable.default_product_image);
                Log.e(TAG, "Error loading product image: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qr_code_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Intent intent = new Intent(UpdateProductActivity.this, ManageInventoryActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (itemId == R.id.menu_item_scan_qr_code) {
            // Launch the QR code scanner activity
            Intent intent = new Intent(this, QRCodeScannerActivity.class);
            startActivity(intent);
            return true;
        }
        // Handle other menu items if needed
        // ...

        return super.onOptionsItemSelected(item);
    }


    private void clearFields() {
        editTextUpdateSuppName.setText("");
        editTextUpdateProductDesc.setText("");
        editTextUpdateQuantity.setText("");
        editTextUpdatePrice.setText("");
        autoCompleteTextViewSearch.setText(""); // Clear the search field
        spinnerUpdateCategory.setSelection(0); // Set the spinner to the first item (or default selection)

        // Clear the productId and productName TextViews
        editTextUpdateProductId.setText("");
        editTextUpdateProductName.setText("");

        // Reset the image to the default image
        ImageView imageViewProductImage = findViewById(R.id.imageViewUpdateProductImage);
        imageViewProductImage.setImageResource(R.drawable.default_product_image);

        // Clear the selectedImageUri
        selectedImageUri = null;
    }




    // Define a Product class to manage product data
    private static class Product {
        private String productId;
        private String userUid;
        private String productName;
        private String supplierName;
        private String category;
        private String productDescription;
        private String quantity;
        private String price;
        private String imageUrl;

        // Constructors, getters, setters, and other methods go here
        public Product() {
            this.productId = ""; // Initialize productId to an empty string
        }


        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        // Add getters and setters for each field
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
