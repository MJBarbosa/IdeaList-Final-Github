package com.example.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.idealist.model.Product;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PointOfSaleActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextViewPOSSearch;
    private ProgressBar progressBarViewPOSProduct;
    private List<PointOfSaleActivity.Product> productList;
    private TextView invisibleProductIdTextView, textViewPOSSuppName, textViewPOSProductDescription, textViewPOSQuantity, textViewPOSPrice, textViewPOSProductId, textViewPOSProductName, textViewPOSCategory;
    private ArrayAdapter<String> searchAdapter, adapter;
    private ImageView imageViewProduct;
    private Uri selectedImageUri;
    private PointOfSaleActivity.Product selectedProduct;
    private Cart carts;
    private int currentQuantity = 0;
    private SharedPreferences sharedPreferences;
    private static final String CART_PREFERENCES = "cart_preferences";
    private static final String CART_ITEMS_KEY = "cart_items";
    private static final String TAG = "PointOfSaleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_sale);

        getSupportActionBar().setTitle("Point Of Sales");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(CART_PREFERENCES, MODE_PRIVATE);

        // Initialize UI elements
        progressBarViewPOSProduct = findViewById(R.id.progressBarViewPOSProduct);
        autoCompleteTextViewPOSSearch = findViewById(R.id.autoCompleteTextViewPOSSearch);
        textViewPOSProductName = findViewById(R.id.textViewPOSProductName);
        textViewPOSProductDescription = findViewById(R.id.textViewPOSProductDescription);
        textViewPOSPrice = findViewById(R.id.textViewPOSPrice);
        textViewPOSCategory = findViewById(R.id.textViewPOSCategory);
        imageViewProduct = findViewById(R.id.imageViewPOSProductImage);
        textViewPOSQuantity = findViewById(R.id.textViewPOSQuantity);
        invisibleProductIdTextView = findViewById(R.id.invisibleProductIdTextView);

        // Initialize the productList and set up the search functionality
        productList = new ArrayList<>();
        // Initialize the searchAdapter (if needed)
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextViewPOSSearch.setAdapter(searchAdapter);
        setupSearch();
        fetchDataFromIntentAndFirebase();

        // Initialize the cart
        carts = new Cart();

        Button buttonAddToCart = findViewById(R.id.buttonAddToCart);
        buttonAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToCart();
                    // You can provide feedback to the user that the product has been added to the cart, e.g., with a Toast message.
                    Toast.makeText(PointOfSaleActivity.this, "Product added to cart", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Log the exception for debugging purposes
                    Log.e(TAG, "Error when adding to cart: " + e.getMessage(), e);
                }
            }
        });

        Button buttonClearFields = findViewById(R.id.buttonClearFields);

        buttonClearFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });

        Button buttonQRScanner = findViewById(R.id.buttonQRScanner);
        buttonQRScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PointOfSaleActivity.this, QRCodePOSScannerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_pos);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home_so) {
                // Handle the Home menu item click here
                Intent intent = new Intent(PointOfSaleActivity.this, MainSOActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_inventory) {
                // You are already in the Inventory tab, no action needed
                Intent intent = new Intent(PointOfSaleActivity.this, ManageInventoryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_pos) {
                // You are already in the POS tab, no action needed
                return true;
            } else if (itemId == R.id.menu_qr_gene) {
                Intent intent = new Intent(PointOfSaleActivity.this, QrGeneratorActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.menu_sales_report) {
                Intent intent = new Intent(PointOfSaleActivity.this, SalesReportActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // Find the buttons
        Button buttonIncreaseQuantity = findViewById(R.id.buttonIncreaseQuantity);
        Button buttonDecreaseQuantity = findViewById(R.id.buttonDecreaseQuantity);

// Find the quantity TextView
        textViewPOSQuantity = findViewById(R.id.textViewPOSQuantity);

        updateQuantityTextView(); // Call this to initialize the quantity display

// Set a click listener for the "Increase Quantity" button
        buttonIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementQuantity();
            }
        });

// Set a click listener for the "Decrease Quantity" button
        buttonDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementQuantity();
            }
        });

        // Check the user's role before allowing access to this activity (if needed)
        checkUserRoleForAccess(getCurrentUserUid());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_cart) {
            Intent intent = new Intent(PointOfSaleActivity.this, AddToCartActivity.class);
            intent.putParcelableArrayListExtra("cart", carts.getCartItems());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void addToCart() {
        // Retrieve and validate the entered data
        String productName = textViewPOSProductName.getText().toString();
        String productDesc = textViewPOSProductDescription.getText().toString();
        String category = textViewPOSCategory.getText().toString();
        String quantity = textViewPOSQuantity.getText().toString();
        String price = textViewPOSPrice.getText().toString();
        String productId = invisibleProductIdTextView.getText().toString();

        // Check if the cart exists in SharedPreferences
        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());

        // Create a string representation of the product
        String cartItem = productName + "," + productDesc + "," + category + "," + quantity + "," + price + "," + productId;
        Log.d(TAG, "Value of addttocart Product: " + productId);

        // Add the cart item to the set
        cartItemsSet.add(cartItem);

        // Store the updated cart in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CART_ITEMS_KEY, cartItemsSet);
        editor.apply();

        // Debugging: Log to check if the cart item is added
        Log.d("Cart", "Added to cart: " + cartItem);

        // Optionally, you can also display a confirmation message
        Toast.makeText(this, "Product added to cart.", Toast.LENGTH_LONG).show();
        clearFields();
    }

    private void updateQuantityTextView() {
        // Just update the text of the existing textViewPOSQuantity
        textViewPOSQuantity.setText("Quantity: " + currentQuantity);
    }

    private void decrementQuantity() {
        if (currentQuantity > 0) {
            currentQuantity--;
            updateQuantityTextView();
        }
    }

    private void incrementQuantity() {
        if (currentQuantity >= 0) { // If you allow a quantity of 0, change this condition
            currentQuantity++;
            updateQuantityTextView();
        }
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
                PointOfSaleActivity.Product product = parseScannedContent(scannedContent);
                if (product != null) {
                    // Initialize your UI elements outside the callback
                    autoCompleteTextViewPOSSearch = findViewById(R.id.autoCompleteTextViewPOSSearch);

                    String labelProductName = "Product Name: ";
                    String labelProductDescription = "Product Description: ";
                    String labelCategory = "Category: ";
                    String labelPrice = "Price: ";

                    // Auto-fill the fields with product information
                    textViewPOSProductDescription.setText(labelProductDescription + product.getProductDescription());
                    textViewPOSPrice.setText(labelPrice + product.getPrice());
                    textViewPOSCategory.setText(labelCategory + product.getCategory());
                    textViewPOSProductName.setText(labelProductName + product.getProductName());
                    autoCompleteTextViewPOSSearch.setText(product.getProductName());
                    invisibleProductIdTextView.setText(product.getProductId());

                    onQRCodeScanned(product.getProductName(), product.getProductId());

                    // Display product image after fetching data
                    ImageView imageViewProductImage = findViewById(R.id.imageViewPOSProductImage);
                    displayProductImage(userUid, product.getProductId(), imageViewProductImage);

                    // Check user role for access
                    checkUserRoleForAccess(userUid);
                }
            }
        }
    }

    private PointOfSaleActivity.Product parseScannedContent(String scannedContent) {
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
        PointOfSaleActivity.Product product = new PointOfSaleActivity.Product();
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

    private AlertDialog alertDialog = null;

    // Call this method when you want to show the search dialog (e.g., after scanning a QR code)
    private void onQRCodeScanned(String scannedContent, String productId) {
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
                //Log.d(TAG, "Value of Product: " + productId);

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
        PointOfSaleActivity.Product selectedProduct = findProductByName(productName);

        if (selectedProduct != null) {
            String labelProductName = "Product Name: ";
            String labelProductDescription = "Product Description: ";
            String labelCategory = "Category: ";
            String labelPrice = "Price: ";
            // Populate the editText fields with the selected product's data
            textViewPOSProductDescription.setText(labelProductDescription + selectedProduct.getProductDescription());
            textViewPOSPrice.setText(labelPrice + selectedProduct.getPrice());
            textViewPOSCategory.setText(labelCategory + selectedProduct.getCategory());

            // Update the TextViews for productId and productName
            TextView textViewPOSProductName = findViewById(R.id.textViewPOSProductName);

            textViewPOSProductName.setText(labelProductName + selectedProduct.getProductName());

            // Display the product image
            ImageView imageViewProductImage = findViewById(R.id.imageViewPOSProductImage);
            displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProductImage);
        } else {
            Log.e(TAG, "Selected product is null.");
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
                        PointOfSaleActivity.Product product = new PointOfSaleActivity.Product();
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
                    } else if (productSnapshot.getValue() instanceof PointOfSaleActivity.Product) {
                        // Handle data stored as Product object
                        PointOfSaleActivity.Product product = productSnapshot.getValue(PointOfSaleActivity.Product.class);
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
                for (PointOfSaleActivity.Product product : productList) {
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
                Toast.makeText(PointOfSaleActivity.this, "Error loading product data.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading product data: " + databaseError.getMessage());
            }
        });

        // Handle item selection from the search suggestions
        autoCompleteTextViewPOSSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected product name
                String selectedProductName = parent.getItemAtPosition(position).toString();

                // Find the corresponding product in the productList
                PointOfSaleActivity.Product selectedProduct = findProductByName(selectedProductName);

                if (selectedProduct != null) {
                    String labelProductName = "Product Name: ";
                    String labelProductDescription = "Product Description: ";
                    String labelCategory = "Category: ";
                    String labelPrice = "Price: ";
                    // Populate the editText fields with the selected product's data
                    textViewPOSProductDescription.setText(labelProductDescription + selectedProduct.getProductDescription());
                    textViewPOSPrice.setText(labelPrice + selectedProduct.getPrice());
                    textViewPOSCategory.setText(labelCategory + selectedProduct.getCategory());
                    invisibleProductIdTextView.setText(selectedProduct.getProductId());

                    // Update the TextViews for productId and productName
                    TextView textViewPOSProductName = findViewById(R.id.textViewPOSProductName);

                    textViewPOSProductName.setText(labelProductName + selectedProduct.getProductName());

                    // Display the product image
                    ImageView imageViewProductImage = findViewById(R.id.imageViewPOSProductImage);
                    displayProductImage(selectedProduct.getUserUid(), selectedProduct.getProductId(), imageViewProductImage);
                } else {
                    Log.e(TAG, "Selected product is null.");
                }
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

    private void checkUserRoleForAccess(String userUid) {
        DatabaseReference userRolesRef = FirebaseDatabase.getInstance().getReference("UserRoles")
                .child(userUid)
                .child("storeOwner");

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
                        Toast.makeText(PointOfSaleActivity.this, "Only Store Owners can access this feature.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // User role data does not exist, handle it as needed
                    //Toast.makeText(PointOfSaleActivity.this, "User role not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(PointOfSaleActivity.this, "Error checking user role.", Toast.LENGTH_LONG).show();
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
    private PointOfSaleActivity.Product findProductByName(String productName) {
        for (PointOfSaleActivity.Product product : productList) {
            if (product.getProductName().equals(productName)) {
                return product;
            }
        }
        return null;
    }

    private void clearFields() {
        textViewPOSProductDescription.setText("Product Desription: ");
        textViewPOSPrice.setText("Price: ");
        autoCompleteTextViewPOSSearch.setText(""); // Clear the search field
        textViewPOSCategory.setText("Category: "); // Set the spinner to the first item (or default selection)
        textViewPOSQuantity.setText("Quantity: 0");
        invisibleProductIdTextView.setText("This is an invisible TextView");

        // Clear the productId and productName TextViews
        textViewPOSProductName.setText("Product Name: ");

        // Reset the image to the default image
        ImageView imageViewProductImage = findViewById(R.id.imageViewPOSProductImage);
        imageViewProductImage.setImageResource(R.drawable.default_product_image);

        // Clear the selectedImageUri
        selectedImageUri = null;
    }

    public class Cart {
        private ArrayList<Product> cartItems;

        public Cart() {
            // Initialize the cartItems list when a new Cart is created
            cartItems = new ArrayList<>();
        }

        // Add a product to the cart
        public void addProduct(Product product) {
            cartItems.add(product);
        }

        // Remove a product from the cart
        public void removeProduct(Product product) {
            cartItems.remove(product);
        }

        // Get the list of cart items
        public ArrayList<Product> getCartItems() {
            return cartItems;
        }

        // Clear the cart
        public void clearCart() {
            cartItems.clear();
        }

        // Calculate the total price of items in the cart
        public double calculateTotal() {
            double total = 0.0;
            try {
                for (Product product : cartItems) {
                    // Calculate the total price by summing up the prices of all items in the cart
                    total += Double.parseDouble(product.getPrice());
                }
            } catch (NumberFormatException e) {
                // Handle the case where the price cannot be parsed as a double
                e.printStackTrace();
            }
            return total;
        }
    }


    // Define a Product class to manage product data (if needed)
    public static class Product implements Parcelable {
        private String productId;
        private String userUid;
        private String productName;
        private String supplierName;
        private String category;
        private String productDescription;
        private String quantity;
        private String price;
        // Additional fields to store calculated numeric values
        private int intQuantity;
        private double doublePrice;

        // Constructors, getters, setters, and other methods go here
        // Constructor with five string parameters
        public Product(String productName, String productDescription, String category, String quantity, String price, String productId) {
            this.productName = productName;
            this.productDescription = productDescription;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.productId = productId;

            // Initialize the additional fields
            try {
                intQuantity = Integer.parseInt(quantity);
            } catch (NumberFormatException e) {
                intQuantity = 0; // Handle parsing errors
            }

            try {
                doublePrice = Double.parseDouble(price);
            } catch (NumberFormatException e) {
                doublePrice = 0.0; // Handle parsing errors
            }
        }

        // Factory method to create Product objects
        public static Product createProduct(String productName, String productDescription, String category, String quantity, String price) {
            Product product = new Product();
            product.setProductName(productName);
            product.setProductDescription(productDescription);
            product.setCategory(category);
            product.setQuantity(quantity);
            product.setPrice(price);
            return product;
        }

        public int getIntQuantity() {
            return intQuantity;
        }

        public void setIntQuantity(int intQuantity) {
            this.intQuantity = intQuantity;
            // Update the original string value
            this.quantity = String.valueOf(intQuantity);
        }

        public double getDoublePrice() {
            return doublePrice;
        }

        public void setDoublePrice(double doublePrice) {
            this.doublePrice = doublePrice;
            // Update the original string value
            this.price = String.valueOf(doublePrice);
        }
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

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(productId);
            dest.writeString(userUid);
            dest.writeString(productName);
            dest.writeString(supplierName);
            dest.writeString(category);
            dest.writeString(productDescription);
            dest.writeString(quantity);
            dest.writeString(price);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        // Add a Parcelable.Creator field
        public static final Creator<Product> CREATOR = new Creator<Product>() {
            @Override
            public Product createFromParcel(Parcel in) {
                return new Product(in);
            }

            @Override
            public Product[] newArray(int size) {
                return new Product[size];
            }
        };

        // Add a constructor that reads from a Parcel
        protected Product(Parcel in) {
            productId = in.readString();
            userUid = in.readString();
            productName = in.readString();
            supplierName = in.readString();
            category = in.readString();
            productDescription = in.readString();
            quantity = in.readString();
            price = in.readString();
        }
    }
}