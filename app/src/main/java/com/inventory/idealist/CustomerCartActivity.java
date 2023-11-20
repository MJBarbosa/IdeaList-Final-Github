package com.inventory.idealist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CustomerCartActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String CART_PREFERENCES = "cart_preferences_customer";
    private static final String CART_ITEMS_KEY = "cart_items_customer";
    private DatabaseReference databaseReference;
    private List<Product> cartProducts = new ArrayList<>();
    private CartAdapter cartAdapter;
    private TextView totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);

        sharedPreferences = getSharedPreferences(CART_PREFERENCES, MODE_PRIVATE);

        totalTextView = findViewById(R.id.textViewTotalCus);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("ProductInventory");

        // Initialize the RecyclerView and the adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerViewCartCus);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set the layout manager
        cartAdapter = new CartAdapter(this, cartProducts, this);
        recyclerView.setAdapter(cartAdapter);

        // Load cart items from SharedPreferences
        loadCartItemsFromSharedPreferences();

        Button clearCartButton = findViewById(R.id.buttonClearCartCus);
        clearCartButton.setOnClickListener(v -> {
            clearCart();
            // Implement checkout logic here
            // You can proceed with the payment process or any other actions
            // you want to perform when the user checks out
            // For this example, let's just display a message
            Toast.makeText(CustomerCartActivity.this, "Cart Has Been Cleared", Toast.LENGTH_SHORT).show();
        });

        Button purchaseButton = findViewById(R.id.buttonPurchaseCus);
        purchaseButton.setOnClickListener(v -> {
            purchase();

            Toast.makeText(CustomerCartActivity.this, "Purchased Successfully!", Toast.LENGTH_SHORT).show();
        });

        // Calculate and display the total when the activity is created
        calculateAndDisplayTotal();
    }

    // Add this method to calculate and display the total
    private void calculateAndDisplayTotal() {
        double total = 0.0;

        for (Product cartProduct : cartProducts) {
            // Parse the price from the product and add it to the total
            try {
                double price = Double.parseDouble(cartProduct.getPrice());
                total += price;
            } catch (NumberFormatException e) {
                // Handle the case where the price is not a valid number
                e.printStackTrace();
            }
        }

        // Format the total as a currency string (e.g., ₱123.45)
        String formattedTotal = "₱" + String.format(Locale.US, "%.2f", total);

        // Display the total in the TextView
        totalTextView.setText("Total: " + formattedTotal);
    }

    // Load cart items from SharedPreferences
    private void loadCartItemsFromSharedPreferences() {
        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());

        // Convert the cart item strings to Product objects
        cartProducts.clear();
        for (String cartItemString : cartItemsSet) {
            Product cartProduct = convertCartItemStringToProduct(cartItemString);
            if (cartProduct != null) {
                cartProducts.add(cartProduct);
            }
        }

        // Notify the adapter that the data has changed
        cartAdapter.notifyDataSetChanged();
    }

    private String convertProductToCartItemString(Product product) {
        // Convert a Product object to a JSON string
        Gson gson = new Gson();
        return gson.toJson(product);
    }


    private Product convertCartItemStringToProduct(String cartItemString) {
        // Convert a JSON string back to a Product object
        Gson gson = new Gson();
        return gson.fromJson(cartItemString, Product.class);
    }


    private void saveCartItemsToSharedPreferences() {
        // Convert the cart items to strings
        Set<String> cartItemsSet = new HashSet<>();
        for (Product cartProduct : cartProducts) {
            String cartItemString = convertProductToCartItemString(cartProduct);
            cartItemsSet.add(cartItemString);
        }

        // Save the cart items to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CART_ITEMS_KEY, cartItemsSet);
        editor.apply();
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false); // Allow both portrait and landscape
        integrator.initiateScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qr_code_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(CustomerCartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();// Close the current activity and return to the previous one
            return true;
        } else if (id == R.id.menu_item_scan_qr_code) {
            startQRScanner();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Override onActivityResult to handle QR code scanning result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Get the scanned QR code data
                String scannedData = result.getContents();

                // Parse the scanned content and create a Product object
                Product product = parseScannedContent(scannedData);

                if (product != null) {
                    // Use the extracted product data to fetch details from the Realtime Database
                    fetchProductDetails(product);
                } else {
                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Scanning canceled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Method to fetch product details from the Realtime Database using a Product object
    private void fetchProductDetails(Product product) {
        String productId = product.getProductId();
        String productName = product.getProductName();
        Log.d(TAG, "Fetching productId: " + productId);
        Log.d(TAG, "Fetching productName: " + productName);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Registered Store Owner Users"); // Replace with the appropriate path to your user data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                for (DataSnapshot userUidSnapshot : userSnapshot.getChildren()) {
                    String userUid = userUidSnapshot.getKey();

                    DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("ProductInventory").child(userUid);

                    productRef.orderByChild("productId").equalTo(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean productFound = false;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.child("productName").exists()) {
                                    String dbProductName = snapshot.child("productName").getValue(String.class);
                                    Log.d("ProductDetails", "DBProduct Name: " + dbProductName);

                                    if (dbProductName != null && dbProductName.equals(productName)) {
                                        // Product found, log and show details
                                        productFound = true;
                                        String DBProductId = snapshot.child("productId").getValue(String.class);
                                        String DBUserUid = snapshot.child("userUid").getValue(String.class);
                                        String productDescription = snapshot.child("productDescription").getValue(String.class);
                                        String category = snapshot.child("category").getValue(String.class);
                                        String price = snapshot.child("price").getValue(String.class);

                                        // Log product details
                                        Log.d("ProductDetails", "DBProduct Description: " + productDescription);
                                        Log.d("ProductDetails", "DBCategory: " + category);
                                        Log.d("ProductDetails", "DBPrice: " + price);
                                        Log.d("ProductDetails", "DBProductId: " + DBProductId);
                                        Log.d("ProductDetails", "DBUserUid: " + DBUserUid);

                                        // Display an alert message to add the product to the cart
                                        showAddToCartAlert(dbProductName, productDescription, category, price, DBProductId, DBUserUid);

                                        // Exit the loop as the product has been found
                                        break;
                                    }
                                }
                            }

                            if (!productFound) {
                                Toast.makeText(CustomerCartActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                            }

                            // Log that all data has been fetched
                            Log.d("ProductDetails", "All data has been fetched.");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(CustomerCartActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerCartActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Display an alert to add the product to the cart
    private void showAddToCartAlert(String productName, String productDescription, String category, String price, String productId, String userUid) {
        //Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        dialogBuilder.setView(dialogView);

        // Find views within the custom dialog layout
        TextView productNameTextView = dialogView.findViewById(R.id.itemNameTextView);
        TextView productDescTextView = dialogView.findViewById(R.id.itemDescriptionTextView);
        TextView productCategoryTextView = dialogView.findViewById(R.id.itemCategoryTextView);
        TextView productQuantityTextView = dialogView.findViewById(R.id.itemQuantityTextView);
        TextView productPriceTextView = dialogView.findViewById(R.id.itemPriceTextView);
        TextView productTotalTextView = dialogView.findViewById(R.id.itemTotalTextView);
        Button addToCartButton = dialogView.findViewById(R.id.addToCartButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        ImageView imageViewProductImage = dialogView.findViewById(R.id.itemDefaultImageView);

        Log.d("ProductDetails", "ProductId: " + productId);
        Log.d("ProductDetails", "UserUid: " + userUid);

        // Set the product name in the TextView
        productNameTextView.setText("Product Name: " + productName);
        productDescTextView.setText("Description: " + productDescription);
        productCategoryTextView.setText("Category: " + category);
        productQuantityTextView.setText("Quantity: " + 1);
        productPriceTextView.setText("₱" + price);
        productTotalTextView.setText("₱" + price);

        displayProductImage(productId, userUid, imageViewProductImage);


        // Create the dialog
        AlertDialog dialog = dialogBuilder.create();

        // Add an onClickListener for the "Add" button
        addToCartButton.setOnClickListener(v -> {
            // Create a Product object for the selected item
            Product cartProduct = new Product();
            cartProduct.setUserUid(userUid);
            cartProduct.setProductId(productId);
            cartProduct.setProductName(productName);
            cartProduct.setProductDescription(productDescription);
            cartProduct.setPrice(price);
            cartProduct.setCategory(category);

            // Add the product to the cart list
            //cartProducts.add(cartProduct);

            // Notify the adapter that the data has changed
            cartAdapter.notifyDataSetChanged();
            addProductToCart(cartProduct);

            calculateAndDisplayTotal();

            // Close the dialog
            dialog.dismiss();
        });

        // Add an onClickListener for the "Cancel" button
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    private void displayProductImage(String productId, String userUid, ImageView imageViewProductImage) {

        // Create a reference to the specific image in the "ProductImages" folder
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child("ProductImages/" + userUid + "/" + productId);

        Log.d(TAG, "UserUid Image: " + userUid);
        Log.d(TAG, "Product ID Image: " + productId);
        // Get the download URL for the image
        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Load the image into the ImageView using Picasso
                    Picasso.get().load(uri).into(imageViewProductImage);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        // Handle the case where the file does not exist
                        Log.e(TAG, "Image does not exist at location.");
                    } else {
                        // Handle other errors
                        Log.e(TAG, "Error getting image URL: " + e.getMessage());
                    }
                });
    }

    // Add the product to the cart
    private void addProductToCart(Product cartProduct) {
        cartProducts.add(cartProduct);
        cartAdapter.notifyDataSetChanged();

        // Save the updated cart items to SharedPreferences
        saveCartItemsToSharedPreferences();
    }

    private void purchase() {
        if (cartProducts.size() == 1) {
            // If there is only one product, directly proceed with the purchase
            Product singleProduct = cartProducts.get(0);

            // Get the current user ID
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                String commonUserUid = cartProducts.get(0).getUserUid();

                // Save the transaction to the database with the current user ID
                Transaction transaction = createTransactionObject();
                showPasswordPrompt(transaction, commonUserUid);
            } else {
                // Handle the case where the user is not authenticated
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Check if all products have different user UIDs
            if (checkAllProductsHaveDifferentUserUid()) {
                showUserUidConflictAlert();
            } else {
                // All products have different user UIDs or the same user UID, proceed with the purchase

                // Create a Transaction object
                Transaction transaction = createTransactionObject();

                // Check if all products have the same user UID
                if (checkAllProductsHaveSameUserUid()) {
                    String commonUserUid = cartProducts.get(0).getUserUid(); // Assuming there is at least one product
                    showPasswordPrompt(transaction, commonUserUid);
                } else {
                    // Get the current user ID
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String currentUserId = currentUser.getUid();
                        // Save the transaction to the database with the current user ID
                        saveTransactionToDatabase(transaction, currentUserId);

                        // Display the receipt
                        //showReceipt(transaction);
                    } else {
                        // Handle the case where the user is not authenticated
                        Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private Transaction createTransactionObject() {
        Transaction transaction = new Transaction();
        transaction.setPurchasedProducts(cartProducts);
        transaction.setTotalAmount(calculateTotal());
        transaction.setTransactionDate(getCurrentDate());
        return transaction;
    }

    private boolean checkAllProductsHaveSameUserUid() {
        Set<String> uniqueUserUids = new HashSet<>();

        for (Product cartProduct : cartProducts) {
            String userUid = cartProduct.getUserUid();

            // Check if the user UID is not in the set
            if (uniqueUserUids.isEmpty()) {
                uniqueUserUids.add(userUid);
            } else {
                // Products have different user UIDs
                if (!uniqueUserUids.contains(userUid)) {
                    Log.d(TAG, "Different user UID found: " + userUid);
                    return false;
                }
            }
        }

        return true; // All products have the same user UID
    }


    private void showPasswordPrompt(Transaction transaction, String userUid) {
        // Create an alert dialog with an input field for the password
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Verification");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserUid = currentUser.getUid();
            // Verify the password using Firebase Authentication
            verifyPasswordAndProceed(transaction, userUid, password, currentUserUid);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }

    private void verifyPasswordAndProceed(Transaction transaction, String userUid, String password, String currentUserUid) {
        // Get the reference to the user node in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Registered Store Owner Users").child(userUid);

        // Retrieve the stored password hash and other necessary information from the database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedTransactionKey = dataSnapshot.child("transactionKey").getValue(String.class);

                    // You can retrieve other user information as needed
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String storeName = dataSnapshot.child("storeName").getValue(String.class);

                    // Verify the entered password with the stored hash on the server
                    if (verifyPassword(password, storedTransactionKey)) {
                        // Password verification successful, save the transaction
                        saveTransactionToDatabase(transaction, currentUserUid);

                        savePurchasedProductToSalesDatabase(transaction);

                        updateProductInventoryQuantity(transaction, userUid);
                        // Display the receipt
                        //showReceipt(transaction);
                    } else {
                        // Password verification failed, show an error message
                        Toast.makeText(CustomerCartActivity.this, "Password verification failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where the user's data is not found
                    Toast.makeText(CustomerCartActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error case
                Toast.makeText(CustomerCartActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean verifyPassword(String enteredTransactionKey, String storedTransactionKey) {
        // Implement password verification logic, you might use a library or your own implementation
        // For example, you can use Firebase Authentication's signInWithEmailAndPassword
        // or a secure password hashing library
        // ...

        // For simplicity, let's assume a simple string comparison here
        return enteredTransactionKey.equals(storedTransactionKey);
    }

    private boolean checkAllProductsHaveDifferentUserUid() {
        Set<String> uniqueUserUids = new HashSet<>();

        for (Product cartProduct : cartProducts) {
            String userUid = cartProduct.getUserUid();

            // Check if the user UID is already in the set
            if (uniqueUserUids.contains(userUid)) {
                return false; // Products have the same user UID
            }

            // Add the user UID to the set
            uniqueUserUids.add(userUid);
        }

        return true; // All products have different user UIDs
    }

    private void showUserUidConflictAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("User UID Conflict");
        dialogBuilder.setMessage("All the products in the cart are not from the same store. Please remove conflicting products before purchasing.");
        dialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void saveTransactionToDatabase(Transaction transaction, String userId) {
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference().child("TransactionHistory").child(userId);
        String transactionId = transactionRef.push().getKey();

        Log.d(TAG, "TransactionId: " + transactionId);
        Log.d(TAG, "TransactionRef: " + transactionRef);

        DatabaseReference currentTransactionRef = transactionRef.child(transactionId);

        currentTransactionRef.setValue(transaction)
                .addOnSuccessListener(aVoid -> onTransactionSavedSuccessfully(transaction))
                .addOnFailureListener(e -> onTransactionSaveFailure(e, transaction));

        Log.d(TAG, "Current UserId: " + userId);
        Log.d(TAG, "Current Transaction: " + transaction);
    }

    private void onTransactionSavedSuccessfully(Transaction transaction) {
        Log.d(TAG, "Transaction saved successfully.");
        // Display the receipt only after the transaction is saved successfully
        showReceipt(transaction);
    }

    private void onTransactionSaveFailure(Exception e, Transaction transaction) {
        Log.e(TAG, "Error saving transaction: " + e.getMessage());
        // Handle the case where the transaction fails to save (e.g., show an error message)
        Toast.makeText(CustomerCartActivity.this, "Error saving transaction", Toast.LENGTH_SHORT).show();
    }

    private void savePurchasedProductToSalesDatabase(Transaction transaction) {
        // Get the reference to the "Sales" node in the database
        DatabaseReference salesRef = FirebaseDatabase.getInstance().getReference().child("Sales");

        // Iterate through purchased products and save each product to the Sales database
        for (Product product : transaction.getPurchasedProducts()) {
            // Generate a unique key for the sales entry
            String salesEntryKey = salesRef.push().getKey();

            // Create a Map to represent the sales data
            Map<String, Object> salesData = new HashMap<>();
            salesData.put("userUid", product.getUserUid());
            salesData.put("productId", product.getProductId());
            salesData.put("productName", product.getProductName());
            salesData.put("quantity", 1); // Assuming quantity is always 1 in this context
            salesData.put("price", Double.parseDouble(product.getPrice())); // Assuming price is a string, convert it to double
            salesData.put("timestamp", transaction.getTransactionDate()); // Use the timestamp from the transaction object

            // Set the data in the Sales node under the generated key
            salesRef.child(salesEntryKey).setValue(salesData)
                    .addOnSuccessListener(aVoid -> {
                        // Handle the case where the data is saved successfully
                        Log.d(TAG, "Sales entry saved successfully.");
                    })
                    .addOnFailureListener(e -> {
                        // Handle the case where there is an error saving the data
                        Log.e(TAG, "Error saving sales entry: " + e.getMessage());
                    });
        }
    }

    private void updateProductInventoryQuantity(Transaction transaction, String storeOwnerUid) {
        DatabaseReference productInventoryRef = FirebaseDatabase.getInstance().getReference().child("ProductInventory").child(storeOwnerUid);

        for (Product product : transaction.getPurchasedProducts()) {
            String productId = product.getProductId();

            // Find the product reference using the product ID
            productInventoryRef.orderByChild("productId").equalTo(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Loop through the matching products (though there should be only one)
                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                            // Get the reference to the selected product
                            DatabaseReference updateReference = productSnapshot.getRef();

                            // Update the product's information
                            int currentQuantity = Integer.parseInt(productSnapshot.child("quantity").getValue(String.class));
                            int purchasedQuantity = 1;
                            int updatedQuantity = currentQuantity - purchasedQuantity;

                            // Ensure the quantity doesn't go below zero
                            updatedQuantity = Math.max(updatedQuantity, 0);

                            // Convert updatedQuantity back to String
                            String updatedQuantityString = String.valueOf(updatedQuantity);

                            Log.d(TAG, "Product found. Updating quantity. ProductId: " + product.getProductId());
                            Log.d(TAG, "Current Quantity: " + currentQuantity);
                            Log.d(TAG, "Purchased Quantity: " + purchasedQuantity);
                            Log.d(TAG, "Updated Quantity: " + updatedQuantity);

                            // Update the quantity only if the user has the required permissions
                            String productUserUid = productSnapshot.child("userUid").getValue(String.class);
                            if (productUserUid != null && productUserUid.equals(storeOwnerUid)) {
                                // Update the quantity with proper validation
                                updateReference.child("quantity").setValue(updatedQuantityString, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error == null) {
                                            Log.d(TAG, "Quantity updated successfully for ProductId: " + product.getProductId());
                                            clearCart();
                                        } else {
                                            Log.e(TAG, "Error updating quantity: " + error.getMessage());
                                            // Handle the update failure
                                            // Example: callback.onFailure();
                                        }
                                    }
                                });
                                Log.d(TAG, "Update Reference: " + updateReference);
                            } else {
                                Log.e(TAG, "User does not have permission to update quantity for ProductId: " + product.getProductId());
                                // Here you can invoke a callback to notify the failure
                                // Example: callback.onFailure();
                            }
                        }
                    } else {
                        Log.e(TAG, "Product not found in the inventory. ProductId: " + product.getProductId());
                        // Here you can invoke a callback to notify the failure
                        // Example: callback.onFailure();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    // Here you can invoke a callback to notify the failure
                    // Example: callback.onFailure();
                }
            });
        }
    }

    private void showReceipt(Transaction transaction) {
        // Build the receipt string
        StringBuilder receiptText = new StringBuilder();
        receiptText.append("Receipt\n\n");
        for (Product product : transaction.getPurchasedProducts()) {
            receiptText.append(product.getProductName()).append(" - ₱").append(product.getPrice()).append("\n");
        }
        receiptText.append("\nTotal Amount: ₱").append(transaction.getTotalAmount()).append("\n\n")
                .append("Transaction Date: ").append(transaction.getTransactionDate());

        // Create the AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Receipt");
        builder.setMessage(receiptText.toString());
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Share", (dialog, which) -> shareReceipt(receiptText.toString()));
        builder.setNeutralButton("Print", (dialog, which) -> printReceipt(receiptText.toString()));

        // Show the AlertDialog
        builder.create().show();
    }


    private void shareReceipt(String receiptText) {
        // Implement logic to share the receipt text (e.g., via Intent)
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, receiptText);
        startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
    }

    private void printReceipt(String receiptText) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        String jobName = "Receipt";

        // Create a WebView to render the receipt text as an HTML page
        WebView webView = new WebView(this);
        String htmlData = "<html>" +
                "<head>" +
                "<style>" +
                "body {" +
                "   font-family: Arial, sans-serif;" +
                "}" +
                "h2 {" +
                "   color: #333333;" +
                "}" +
                "p {" +
                "   margin: 5px 0;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h2>Receipt</h2>" +
                "<p>" + receiptText + "</p>" +
                "</body>" +
                "</html>";

        webView.loadDataWithBaseURL(null, htmlData, "text/HTML", "UTF-8", null);

        // Create a print document adapter
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Start the print job
        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

        // You can monitor the print job status if needed
        if (printJob.isCompleted()) {
            // Printing is complete
        } else if (printJob.isFailed()) {
            // Printing failed
        }
    }

    private double calculateTotal() {
        // Implement your logic to calculate the total amount
        double total = 0.0;
        for (Product product : cartProducts) {
            try {
                double price = Double.parseDouble(product.getPrice());
                total += price;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    private String getCurrentDate() {
        // Implement logic to get the current date (e.g., using SimpleDateFormat)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }


    private void clearCart() {
        // Clear the list of cart items in your adapter
        cartAdapter.clearCart();

        // Clear the cartProducts list
        cartProducts.clear();

        // Update the total display to show $0.00
        totalTextView.setText("Total: ₱0.00");

        // Clear the cart items from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(CART_ITEMS_KEY);
        editor.apply();
    }

    // Implement your logic to parse the scanned content and create a Product object
    private Product parseScannedContent(String scannedContent) {
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

        Log.d(TAG, "Product ID: " + productId);
        Log.d(TAG, "Product Name: " + productName);

        return product;
    }

    // Implement your logic to extract values from the scanned content
    private String extractValueFromScannedContent(String scannedContent, String key) {
        String[] lines = scannedContent.split("\n");
        for (String line : lines) {
            if (line.startsWith(key)) {
                return line.substring(key.length()).trim();
            }
        }
        return "";
    }

    public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        private List<Product> cartProducts;
        private Context context;
        private CustomerCartActivity activity;

        public CartAdapter(Context context, List<Product> cartProducts, CustomerCartActivity activity) {
            this.context = context;
            this.cartProducts = cartProducts;
            this.activity = activity;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_cus_item_layout, parent, false);
            return new CartViewHolder(view);
        }

        public void clearCart() {
            cartProducts.clear();
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            Product cartProduct = cartProducts.get(position); // Get the cartProduct

            // Bind the cartProduct data to the views in the cart item layout
            holder.productNameTextView.setText("Product Name: " + cartProduct.getProductName());
            holder.productDescriptionTextView.setText("Description: " + cartProduct.getProductDescription());
            holder.priceTextView.setText("Price: " + cartProduct.getPrice());
            holder.categoryTextView.setText("Category: " + cartProduct.getCategory());
            holder.quantityTextView.setText("Quantity: " + 1);
            holder.totalTextView.setText("Total: " + cartProduct.getPrice());

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle delete button click here
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        removeItem(adapterPosition);
                    }
                }
            });

            displayProductImage(cartProduct, holder.productImage);
        }

        public void removeItem(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Remove Item");
            builder.setMessage("Are you sure you want to remove this item?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked Yes, remove the item
                    String productIdToRemove = cartProducts.get(position).getProductId();
                    removeItemFromSharedPreferences(productIdToRemove);

                    cartProducts.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartProducts.size());

                    activity.calculateAndDisplayTotal();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked No, do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        private void removeItemFromSharedPreferences(String productId) {
            // Retrieve existing cart items from SharedPreferences
            Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());

            // Convert the set to a list for manipulation
            List<String> cartItemsList = new ArrayList<>(cartItemsSet);

            // Identify the index of the item to remove
            int indexToRemove = -1;
            for (int i = 0; i < cartItemsList.size(); i++) {
                Product product = deserializeProduct(cartItemsList.get(i));
                if (product != null && product.getProductId().equals(productId)) {
                    indexToRemove = i;
                    break;  // Assuming productId is unique, exit loop once found
                }
            }

            // Print the removed product ID for debugging
            if (indexToRemove != -1) {
                Log.d(TAG, "Removing product with ID: " + productId);
            } else {
                Log.d(TAG, "Product with ID " + productId + " not found in the cart.");
            }

            // Remove the item from the list
            if (indexToRemove != -1) {
                cartItemsList.remove(indexToRemove);
            }

            // Save the updated cart items back to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(CART_ITEMS_KEY, new HashSet<>(cartItemsList));
            editor.apply();

            activity.calculateAndDisplayTotal();
        }

        // Helper method to deserialize JSON string to Product
        private Product deserializeProduct(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                Product product = new Product();
                product.setProductId(jsonObject.getString("productId"));
                product.setProductName(jsonObject.getString("productName"));
                // Add other fields as needed
                return product;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public int getItemCount() {
            return cartProducts.size();
        }

        public class CartViewHolder extends RecyclerView.ViewHolder {
            public TextView productNameTextView;
            public TextView productDescriptionTextView;
            public TextView priceTextView;
            public TextView categoryTextView;
            public TextView quantityTextView;
            public TextView totalTextView;
            public ImageView productImage;
            public ImageButton btnDelete;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.itemNameTextView);
                productDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                priceTextView = itemView.findViewById(R.id.itemPriceTextView);
                categoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
                quantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
                totalTextView = itemView.findViewById(R.id.itemTotalTextView);
                productImage = itemView.findViewById(R.id.itemDefaultImageView);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

        private void displayProductImage(Product cartProduct, ImageView imageView) {
            // Retrieve userUid and productId from the cartProduct
            String userUid = cartProduct.getUserUid();
            String productId = cartProduct.getProductId();

            Log.d(TAG, "Product ID: " + productId);
            Log.d(TAG, "Product UserUid: " + userUid);

            // Create a reference to the specific image in the "ProductImages" folder
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference().child("ProductImages/" + userUid + "/" + productId);

            // Get the download URL for the image
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        // Load the image into the ImageView using Picasso
                        Picasso.get().load(uri).into(imageView);
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            // Handle the case where the file does not exist
                            Log.e(TAG, "Image does not exist at location.");
                        } else {
                            // Handle other errors
                            Log.e(TAG, "Error getting image URL: " + e.getMessage());
                        }
                    });
        }
    }

    // Define the Product class to hold product information
    public static class Product {
        private String productId;
        private String productName;
        private String productDescription;
        private String price;
        private String category;
        private String userUid;

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

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        // Inside your Product class
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Product otherProduct = (Product) obj;
            return productId.equals(otherProduct.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId);
        }

    }

    public class Transaction {
        private List<Product> purchasedProducts;
        private double totalAmount;
        private String transactionDate;

        // constructor, getters, and setters

        public String getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(String transactionDate) {
            this.transactionDate = transactionDate;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public List<Product> getPurchasedProducts() {
            return purchasedProducts;
        }

        public void setPurchasedProducts(List<Product> purchasedProducts) {
            this.purchasedProducts = purchasedProducts;
        }
    }

}