package com.example.idealist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewProductActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextViewSearch;
    private ProgressBar progressBarViewProduct;
    private List<Product> productList;
    private ListView listViewProducts;
    private ArrayAdapter<String> searchAdapter;
    private ArrayAdapter<String> adapter;
    private FirebaseAuth authProfile;
    private Product selectedProduct;
    private ImageView imageViewProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        getSupportActionBar().setTitle("View Products");

        // Initialize UI elements
        progressBarViewProduct = findViewById(R.id.progressBarViewProduct);
        autoCompleteTextViewSearch = findViewById(R.id.autoCompleteTextViewSearch);
        listViewProducts = findViewById(R.id.listViewProducts);
        imageViewProduct = findViewById(R.id.imageViewProductImage);

        // Initialize the productList and set up the search functionality
        productList = new ArrayList<>();
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewSearch.setAdapter(searchAdapter);
        setupSearch();

        Button buttonSearch = findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the search logic here
                String searchQuery = autoCompleteTextViewSearch.getText().toString();
                searchProduct(searchQuery);
            }
        });
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
                        product.setUserUid(getCurrentUserUid());
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
                Toast.makeText(ViewProductActivity.this, "Error loading product data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product data: " + databaseError.getMessage());
            }
        });

        // Handle item selection from the search suggestions
        autoCompleteTextViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name
                String selectedProductName = parent.getItemAtPosition(position).toString();

                // Find the corresponding product in the productList
                selectedProduct = findProductByName(selectedProductName);

                if (selectedProduct != null) {
                    // Populate TextViews with the selected product's data
                    updateTextViewsWithProductData(selectedProduct);

                    // Display the product image
                    displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProduct);
                } else {
                    Log.e(TAG, "Selected product is null.");
                }
            }
        });

        // Add an OnItemClickListener to the ListView to handle item clicks
        listViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name from the adapter
                String selectedProductName = adapter.getItem(position);

                // Find the corresponding product in the productList
                Product selectedProduct = findProductByName(selectedProductName);

                if (selectedProduct != null) {
                    // Populate TextViews with the selected product's data
                    updateTextViewsWithProductData(selectedProduct);

                    // Display the product image
                    displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProduct);
                } else {
                    Log.e(TAG, "Selected product is null.");
                }
            }
        });
    }

    private void loadProductImage(String imageUrl, String userUid, String productId) {
        if (!TextUtils.isEmpty(imageUrl)) {
            // If imageUrl is provided, load and display the image using the displayProductImage function
            displayProductImage(userUid, productId, imageViewProduct);
        } else {
            // If imageUrl is empty, you can display a placeholder image or handle it as needed
            imageViewProduct.setImageResource(R.drawable.default_product_image);
            Log.d(TAG, "Empty imageUrl, using placeholder image.");
        }
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

    private Product findProductByName(String productName) {
        for (Product product : productList) {
            if (product.getProductName().equalsIgnoreCase(productName)) {
                return product;
            }
        }
        return null; // Return null if the product is not found
    }


    private void updateTextViewsWithProductData(Product selectedProduct) {
        TextView textViewProductName = findViewById(R.id.textViewProductName);
        TextView textViewProductDescription = findViewById(R.id.textViewProductDescription);
        TextView textViewQuantity = findViewById(R.id.textViewQuantity);
        TextView textViewPrice = findViewById(R.id.textViewPrice);

        textViewProductName.setText("Product Name: " + selectedProduct.getProductName());
        textViewProductDescription.setText("Product Description: " + selectedProduct.getProductDescription());
        textViewQuantity.setText("Quantity: " + selectedProduct.getQuantity());
        textViewPrice.setText("Price: " + selectedProduct.getPrice());
    }

    private void searchProduct(String query) {
        // Perform a search based on the entered query
        // You can use the productList to filter and display the search results
        // Example: Filter the productList to find products matching the query
        List<Product> searchResults = new ArrayList<>();
        for (Product product : productList) {
            if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(product);
            }
        }

        // Create an adapter to display the search results in the listViewProducts
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewProducts.setAdapter(adapter);

        // Clear the previous search results
        adapter.clear();

        // Add the search results to the adapter
        for (Product product : searchResults) {
            adapter.add(product.getProductName());
        }

        // Notify the adapter that the data has changed
        adapter.notifyDataSetChanged();
    }

    private String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "";
        }
    }

    private void checkUserRoleForAccess() {
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
                    } else {
                        // User does not have the "storeOwner" role, show an error message
                        Toast.makeText(ViewProductActivity.this, "Only Store Owners can access this feature.", Toast.LENGTH_LONG).show();
                        finish(); // Close the activity for non-store owners
                    }
                } else {
                    // User role data does not exist for this user or is not set to "storeOwner"
                    // Handle it as needed, for example, by displaying an error message or taking appropriate action.
                    Toast.makeText(ViewProductActivity.this, "User role not set to 'storeOwner'.", Toast.LENGTH_LONG).show();
                    finish(); // Close the activity if the user role is not set to "storeOwner"
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(ViewProductActivity.this, "Error checking user role.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error checking user role: " + databaseError.getMessage());
                finish(); // Close the activity on database error
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Handle the back button press
            Intent intent = new Intent(ViewProductActivity.this, ManageInventoryActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        }

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                    .getReference("Registered Users")
                    .child(firebaseUser.getUid());

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.child("UserRoles").getValue(String.class);
                        if (role != null) {
                            if (role.equals("storeOwner")) {
                                // Handle actions for Admin role
                                Intent adminIntent = new Intent(ViewProductActivity.this, ManageInventoryActivity.class);
                                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(adminIntent);
                            } else {
                                // Handle actions for other roles or roles not defined
                                Toast.makeText(ViewProductActivity.this, "Unauthorized action for this role", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        } else {
            Toast.makeText(ViewProductActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Define a Product class to manage product data (if needed)
    private static class Product {
        private String productId;
        private String productName;
        private String supplierName;
        private String category;
        private String productDescription;
        private String quantity;
        private String price;
        private String imageUrl;
        private String userUid;

        // Constructors, getters, setters, and other methods go here
        public Product() {
            this.productId = ""; // Initialize productId to an empty string
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

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }
    }
}
