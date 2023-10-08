package com.example.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeleteProductActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextViewDeleteSearch;
    private ProgressBar progressBarDeleteProduct;
    private List<Product> productList;
    private TextView textViewDeleteSuppName, textViewDeleteProductDesc, textViewDeleteQuantity, textViewDeletePrice, textViewDeleteProductId, textViewDeleteProductName, textViewDeleteCategory;
    private ArrayAdapter<String> searchAdapter, adapter;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_product);

        getSupportActionBar().setTitle("Delete Product");

        // Initialize UI elements
        progressBarDeleteProduct = findViewById(R.id.progressBarDeleteProduct);
        textViewDeleteProductId = findViewById(R.id.textViewDeleteProductIdValue);
        textViewDeleteProductName = findViewById(R.id.textViewDeleteProductNameValue);
        textViewDeleteSuppName = findViewById(R.id.textViewDeleteSuppNameValue);
        textViewDeleteProductDesc = findViewById(R.id.textViewDeleteProductDescValue);
        textViewDeleteQuantity = findViewById(R.id.textViewDeleteQuantityValue);
        textViewDeletePrice = findViewById(R.id.textViewDeletePriceValue);
        autoCompleteTextViewDeleteSearch = findViewById(R.id.autoCompleteTextViewDeleteSearch);

        textViewDeleteCategory = findViewById(R.id.textViewDeleteCategoryValue);

        ImageView buttonSelectImage = findViewById(R.id.imageViewDeleteProductImage);

        // Initialize the productList and set up the search functionality
        productList = new ArrayList<>();
        // Initialize the searchAdapter (if needed)
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewDeleteSearch.setAdapter(searchAdapter);
        setupSearch();
        fetchDataFromIntentAndFirebase();
        // Implement the setupSearch() method for delete functionality

        Button buttonDeleteProduct = findViewById(R.id.buttonDeleteProduct);
        buttonDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the delete product logic here
                deleteProduct();

                clearFields();
            }
        });

        // Check the user's role before allowing access to this activity (if needed)
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
                DeleteProductActivity.Product product = parseScannedContent(scannedContent);
                if (product != null) {
                    // Initialize your UI elements outside the callback
                    textViewDeleteSuppName = findViewById(R.id.textViewDeleteSuppNameValue);
                    textViewDeleteQuantity = findViewById(R.id.textViewDeleteQuantityValue);
                    textViewDeleteProductId = findViewById(R.id.textViewDeleteProductIdValue);
                    autoCompleteTextViewDeleteSearch = findViewById(R.id.autoCompleteTextViewDeleteSearch);

                    // Set the product ID in the appropriate EditText
                    textViewDeleteProductId.setText(product.getProductId());

                    // Auto-fill the fields with product information
                    textViewDeleteProductDesc.setText(product.getProductDescription());
                    textViewDeletePrice.setText(product.getPrice());
                    textViewDeleteCategory.setText(product.getCategory());
                    textViewDeleteProductName.setText(product.getProductName());
                    autoCompleteTextViewDeleteSearch.setText(product.getProductName());

                    onQRCodeScanned(product.getProductName());

                    // Display product image after fetching data
                    ImageView imageViewProductImage = findViewById(R.id.imageViewDeleteProductImage);
                    displayProductImage(userUid, product.getProductId(), imageViewProductImage);

                    // Check user role for access
                    checkUserRoleForAccess(userUid);
                }
            }
        }
    }

    private DeleteProductActivity.Product parseScannedContent(String scannedContent) {
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
        DeleteProductActivity.Product product = new DeleteProductActivity.Product();
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
        DeleteProductActivity.Product selectedProduct = findProductByName(productName);

        if (selectedProduct != null) {
            // Populate the editText fields with the selected product's data
            textViewDeleteSuppName.setText(selectedProduct.getSupplierName());
            textViewDeleteProductDesc.setText(selectedProduct.getProductDescription());
            textViewDeleteQuantity.setText(selectedProduct.getQuantity());
            textViewDeletePrice.setText(selectedProduct.getPrice());
            textViewDeleteCategory.setText(selectedProduct.getCategory());

            // Update the TextViews for productId and productName
            TextView textViewProductIdValue = findViewById(R.id.textViewDeleteProductIdValue);
            TextView textViewProductNameValue = findViewById(R.id.textViewDeleteProductNameValue);

            textViewProductIdValue.setText(selectedProduct.getProductId());
            textViewProductNameValue.setText(selectedProduct.getProductName());

            // Display the product image
            ImageView imageViewProductImage = findViewById(R.id.imageViewDeleteProductImage);
            displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProductImage);
        } else {
            Log.e(TAG, "Selected product is null.");
        }
    }

    // Implement setupSearch() method for delete functionality
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
                        DeleteProductActivity.Product product = new DeleteProductActivity.Product();
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
                    } else if (productSnapshot.getValue() instanceof DeleteProductActivity.Product) {
                        // Handle data stored as Product object
                        DeleteProductActivity.Product product = productSnapshot.getValue(DeleteProductActivity.Product.class);
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
                for (DeleteProductActivity.Product product : productList) {
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
                Toast.makeText(DeleteProductActivity.this, "Error loading product data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product data: " + databaseError.getMessage());
            }
        });

        // Handle item selection from the search suggestions
        autoCompleteTextViewDeleteSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name
                String selectedProductName = parent.getItemAtPosition(position).toString();

                // Find the corresponding product in the productList
                DeleteProductActivity.Product selectedProduct = findProductByName(selectedProductName);

                if (selectedProduct != null) {
                    // Populate the editText fields with the selected product's data
                    textViewDeleteSuppName.setText(selectedProduct.getSupplierName());
                    textViewDeleteProductDesc.setText(selectedProduct.getProductDescription());
                    textViewDeleteQuantity.setText(selectedProduct.getQuantity());
                    textViewDeletePrice.setText(selectedProduct.getPrice());
                    textViewDeleteCategory.setText(selectedProduct.getCategory());

                    // Update the TextViews for productId and productName
                    TextView textViewProductIdValue = findViewById(R.id.textViewDeleteProductIdValue);
                    TextView textViewProductNameValue = findViewById(R.id.textViewDeleteProductNameValue);

                    textViewProductIdValue.setText(selectedProduct.getProductId());
                    textViewProductNameValue.setText(selectedProduct.getProductName());

                    // Display the product image
                    ImageView imageViewProductImage = findViewById(R.id.imageViewDeleteProductImage);
                    displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProductImage);
                } else {
                    Log.e(TAG, "Selected product is null.");
                }
            }
        });

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
                        Toast.makeText(DeleteProductActivity.this, "Only Store Owners can access this feature.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // User role data does not exist, handle it as needed
                    Toast.makeText(DeleteProductActivity.this, "User role not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(DeleteProductActivity.this, "Error checking user role.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error checking user role: " + databaseError.getMessage());
            }
        });
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
    private DeleteProductActivity.Product findProductByName(String productName) {
        for (DeleteProductActivity.Product product : productList) {
            if (product.getProductName().equals(productName)) {
                return product;
            }
        }
        return null;
    }

    private void deleteProduct() {
        // Retrieve and validate the entered data
        String productName = autoCompleteTextViewDeleteSearch.getText().toString();

        if (TextUtils.isEmpty(productName)) {
            // Display an error message if the search field is empty
            Toast.makeText(this, "Please enter a product name to delete.", Toast.LENGTH_LONG).show();
            return;
        }

        // Find the selected product in the productList (assuming it's stored there)
        Product selectedProduct = findProductByName(productName);

        if (selectedProduct != null) {
            String userUid = selectedProduct.getUserUid();
            String productId = selectedProduct.getProductId();

            if (userUid != null && productId != null) {
                // Query the database to fetch the productId for the selected product
                DatabaseReference productsReference = FirebaseDatabase.getInstance().getReference("ProductInventory").child(userUid);

                deleteImageInStorage(selectedProduct);


                productsReference.orderByChild("productId").equalTo(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Get the reference to the selected product
                            DataSnapshot productSnapshot = dataSnapshot.getChildren().iterator().next();
                            DatabaseReference deleteReference = productSnapshot.getRef();

                            // Delete the product from the database
                            deleteReference.removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBarDeleteProduct.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                // Product deleted successfully
                                                Toast.makeText(DeleteProductActivity.this, "Product Deleted Successfully", Toast.LENGTH_LONG).show();
                                            } else {
                                                // Handle the error here
                                                Toast.makeText(DeleteProductActivity.this, "Failed to delete product. Please try again.", Toast.LENGTH_LONG).show();
                                                Log.e(TAG, "Error deleting product: " + task.getException());
                                            }
                                        }
                                    });
                        } else {
                            // Handle the case where the selected product is not found in the database
                            Toast.makeText(DeleteProductActivity.this, "Selected product not found in the database.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(DeleteProductActivity.this, "Error querying the database.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error querying the database: " + databaseError.getMessage());
                    }
                });
            } else {
                // Handle the case where userUid or productId is null
                Toast.makeText(DeleteProductActivity.this, "Invalid product data. userUid or productId is null.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "userUid or productId is null.");
            }
        } else {
            Toast.makeText(DeleteProductActivity.this, "Selected product not found.", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteImageInStorage(Product product) {
        if (product == null || product.getProductId() == null || product.getUserUid() == null) {
            // Handle the case where product or its properties are null
            Toast.makeText(DeleteProductActivity.this, "Invalid product data. Cannot delete image.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a reference to the image in Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("ProductImages")
                .child(product.getUserUid())
                .child(product.getProductId());

        // Delete the image
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted successfully
                Toast.makeText(DeleteProductActivity.this, "Image Deleted Successfully", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to delete the image
                Toast.makeText(DeleteProductActivity.this, "Failed to delete image. Please try again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error deleting image: " + e.getMessage());
            }
        });
    }

    private Uri displayedImageUri; // Add this variable

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

                // Store the displayed image URI
                displayedImageUri = uri;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure to download and display the image
                imageView.setImageResource(R.drawable.default_product_image);
                displayedImageUri = null; // Clear the URI
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
            Intent intent = new Intent(DeleteProductActivity.this, ManageInventoryActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (itemId == R.id.menu_item_scan_qr_code) {
            // Launch the QR code scanner activity
            Intent intent = new Intent(this, QRCodeDeleteScannerActivity.class);
            startActivity(intent);
            return true;
        }

        // Handle other menu items if needed
        // ...

        return super.onOptionsItemSelected(item);
    }


    private void clearFields() {
        textViewDeleteSuppName.setText("");
        textViewDeleteProductDesc.setText("");
        textViewDeleteQuantity.setText("");
        textViewDeletePrice.setText("");
        autoCompleteTextViewDeleteSearch.setText(""); // Clear the search field
        textViewDeleteCategory.setText(""); // Set the spinner to the first item (or default selection)

        // Clear the productId and productName TextViews
        textViewDeleteProductId.setText("");
        textViewDeleteProductName.setText("");

        // Reset the image to the default image
        ImageView imageViewProductImage = findViewById(R.id.imageViewDeleteProductImage);
        imageViewProductImage.setImageResource(R.drawable.default_product_image);

        // Clear the selectedImageUri
        selectedImageUri = null;
    }
    // Define a Product class to manage product data (if needed)
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