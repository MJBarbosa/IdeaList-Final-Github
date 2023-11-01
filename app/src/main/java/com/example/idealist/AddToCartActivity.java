package com.example.idealist;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddToCartActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<PointOfSaleActivity.Product> cartItems;
    private TextView totalTextView;
    private static final String CART_PREFERENCES = "cart_preferences";
    private static final String CART_ITEMS_KEY = "cart_items";
    private double total = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_cart);

        getSupportActionBar().setTitle("Shopping Cart");

        sharedPreferences = getSharedPreferences(CART_PREFERENCES, MODE_PRIVATE);


        // Initialize recyclerView and totalTextView
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalTextView = findViewById(R.id.textViewTotal);

        // Get the cart items passed from PointOfSaleActivity
        Intent intent = getIntent();
        cartItems = intent.getParcelableArrayListExtra("cart");

        // Initialize the adapter and set it to the RecyclerView
        cartAdapter = new CartAdapter(cartItems, totalTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);

        // Call the method to display cart items
        displayCartItems();

        // Call the method to calculate and display total from SharedPreferences
        calculateAndDisplayTotalFromSharedPreferences();

        Button clearCartButton = findViewById(R.id.buttonClearCart);
        clearCartButton.setOnClickListener(v -> {
            clearCart();
            // Implement checkout logic here
            // You can proceed with the payment process or any other actions
            // you want to perform when the user checks out
            // For this example, let's just display a message
            Toast.makeText(AddToCartActivity.this, "Cart Has Been Cleared", Toast.LENGTH_SHORT).show();
        });

        // Handle checkout button click
        Button checkoutButton = findViewById(R.id.buttonCheckout);
        checkoutButton.setOnClickListener(v -> {
            performCheckout();
            // Implement checkout logic here
            // You can proceed with the payment process or any other actions
            // you want to perform when the user checks out
            // For this example, let's just display a message
            Toast.makeText(AddToCartActivity.this, "Checkout clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayCartItems() {
        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());
        List<PointOfSaleActivity.Product> updatedCartItems = new ArrayList<>();

        // Iterate through cart items and create Product objects
        for (String cartItem : cartItemsSet) {
            String[] cartItemParts = cartItem.split(",");
            // Extract product details from cartItemParts and create a Product object
            PointOfSaleActivity.Product product = new PointOfSaleActivity.Product(
                    cartItemParts[0], cartItemParts[1], cartItemParts[2], cartItemParts[3], cartItemParts[4], cartItemParts[5]
            );
            updatedCartItems.add(product);
        }

        // Update the cart items in the adapter
        cartAdapter.updateCartItems(updatedCartItems);
    }

    private void calculateAndDisplayTotalFromSharedPreferences() {
        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());
        List<PointOfSaleActivity.Product> cartItems = new ArrayList<>();
        total = 0.0;

        for (String cartItem : cartItemsSet) {
            String[] cartItemParts = cartItem.split(",");

            // Extract product details from cartItemParts
            String productName = cartItemParts[0];
            String productDesc = cartItemParts[1];
            String category = cartItemParts[2];
            String productQuantity = cartItemParts[3];
            String productPrice = cartItemParts[4];
            String productId = cartItemParts[5];

            // Extract Quantity as int
            int quantity = 0;
            try {
                // Extract and convert productQuantity, assuming it's in the format "Quantity: 3"
                String[] quantityParts = productQuantity.split(":");
                if (quantityParts.length == 2) {
                    // Attempt to parse the second part as an integer
                    quantity = Integer.parseInt(quantityParts[1].trim());
                    // Log quantity value
                    Log.d(TAG, "Quantity: " + quantity);
                } else {
                    // Handle cases where the format is unexpected
                    Log.e(TAG, "Invalid quantity format for product: " + productQuantity);
                }
            } catch (NumberFormatException e) {
                // Handle parsing errors
                Log.e(TAG, "Invalid quantity format for product: " + productQuantity);
                e.printStackTrace();
            }
            Log.d(TAG, "Quantity after: " + quantity);
            // Extract Price as double
            double price = 0.00;
            try {
                // Extract and convert productPrice, assuming it's in the format "Price: 250.00"
                String[] priceParts = productPrice.split(":");
                if (priceParts.length == 2 && priceParts[1] != null) {
                    // Attempt to parse the second part as a double
                    String priceValue = priceParts[1].trim();
                    price = Double.parseDouble(priceValue);
                    // Log price value
                    Log.d(TAG, "Price: " + price);
                } else {
                    // Handle cases where the format is unexpected
                    Log.e(TAG, "Invalid price format for product: " + productPrice);
                }
            } catch (NumberFormatException e) {
                // Handle parsing errors
                Log.e(TAG, "Invalid price format for product: " + productPrice);
                e.printStackTrace();
            }
            Log.d(TAG, "Price after: " + price);


            // Calculate item total
            double itemTotal = price * quantity;
            total += itemTotal;

            Log.d(TAG, "Total after solving: " + total);

            // Create a Product object with the extracted values
            PointOfSaleActivity.Product product = new PointOfSaleActivity.Product(
                    productName, productDesc, category, productQuantity, productPrice, productId
            );
            cartItems.add(product);

            // Add a log statement to identify the cart item
            Log.d(TAG, "Cart Item: Product Name: " + productName + ", Product Description: " + productDesc + ", Category: " + category + ", Quantity: " + quantity + ", Price: " + price);
        }
        // Log the calculated total
        Log.d(TAG, "Total: " + total);

        totalTextView.setText("Total: ₱" + formatPrice(total));

    }

    private String formatPrice(double price) {
        // Format the price with two decimal places
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        String formattedPrice = decimalFormat.format(price);
        return formattedPrice;
    }

    private void performCheckout() {
        Log.d(TAG, "Checkout button clicked");

        // Ensure the user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle unauthenticated user
            Log.e(TAG, "User is not authenticated");
            return;
        }

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Sales");

        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());

        if (cartItemsSet != null && !cartItemsSet.isEmpty()) {
            // Create a StringBuilder to build the receipt message
            StringBuilder receiptMessage = new StringBuilder();
            receiptMessage.append("Receipt:\n\n");

            double totalAmount = 0.0;

            for (String cartItem : cartItemsSet) {

                String[] cartItemParts = cartItem.split(",");
                String productName = cartItemParts[0];
                String productDesc = cartItemParts[1];
                String category = cartItemParts[2];
                String productQuantity = cartItemParts[3];
                String productPrice = cartItemParts[4];
                String productId = cartItemParts[5];

                // Extract Quantity as int
                int quantity = 0;
                try {
                    String[] quantityParts = productQuantity.split(":");
                    if (quantityParts.length == 2) {
                        quantity = Integer.parseInt(quantityParts[1].trim());
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Invalid quantity format for product: " + productQuantity);
                }

                // Check if the quantityChanged flag is set for the corresponding CartViewHolder
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    CartAdapter.CartViewHolder cartViewHolder = (CartAdapter.CartViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    if (cartViewHolder.quantityChanged) {
                        // Use the updated quantity
                        cartViewHolder.quantityChanged = false; // Reset the flag
                        quantity = cartViewHolder.currentQuantity;
                        break;
                    }
                }

                // Extract Price as double
                double price = 0.00;
                try {
                    String[] priceParts = productPrice.split(":");
                    if (priceParts.length == 2 && priceParts[1] != null) {
                        String priceValue = priceParts[1].trim();
                        price = Double.parseDouble(priceValue);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Invalid price format for product: " + productPrice);
                }

                double itemTotal = quantity * price; // Calculate item total
                totalAmount += itemTotal;

                // Build the receipt message with item details
                receiptMessage.append("").append(productName).append("\n");
                receiptMessage.append("").append(productDesc).append("\n");
                receiptMessage.append("").append(category).append("\n");
                receiptMessage.append("Quantity: ").append(quantity).append("\n");
                receiptMessage.append("Price: ₱").append(price).append("\n\n");
                receiptMessage.append("Total: ₱").append(itemTotal).append("\n\n"); // Calculate and display item total

                // Create a transaction object (you can customize this based on your database structure)
                Transaction transaction = new Transaction(
                        productName,
                        price,
                        quantity,
                        getCurrentTimestamp(),
                        currentUser.getUid(), // Set the user's UID in the transaction
                        productId
                );

                //Log.d(TAG, "Product Transaction: " + product);

                // Push the transaction object to the "Sales" node in Firebase
                databaseReference.push().setValue(transaction);

                updateProductQuantity(productId, quantity);

                // Deduct the quantity of the product from its database (adjust this based on your actual database structure)
                // You may call this function here or in a loop
                //updateProductQuantity(product.getProductId(), quantity);
            }

            // Add the total amount to the receipt message
            receiptMessage.append("Total Amount: ₱").append(totalAmount).append("\n\n");

            // Clear the cart and display a success message
            clearCart();
            Log.d(TAG, "Checkout successful");
            // Show the receipt in a pop-up dialog
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddToCartActivity.this);
            alertDialogBuilder
                    .setTitle("Checkout Successful")
                    .setMessage(receiptMessage.toString())
                    .setPositiveButton("Print", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            printReceipt(receiptMessage.toString());
                        }
                    })
                    .setNeutralButton("Share", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            shareReceipt(receiptMessage.toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        } else {
            // Handle case where the cart is empty
            Log.e(TAG, "Cart is empty. Nothing to checkout.");
        }

        checkUserRoleForAccess(getCurrentUserUid());
    }

    // Function to print the receipt
    private void printReceipt(String receiptText) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        String jobName = "Receipt";

        // Create a WebView to render the receipt text as an HTML page
        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL(null, "<html><body>" + receiptText + "</body></html>", "text/HTML", "UTF-8", null);

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

    // Function to share the receipt
    private void shareReceipt(String receiptText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, receiptText);

        // Start an activity to share the receipt
        startActivity(Intent.createChooser(shareIntent, "Share Receipt via"));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    // This function updates the quantity of the product in your Firebase Database
    private void updateProductQuantity(String productId, int quantityToDeduct) {
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("ProductInventory")
                .child(getCurrentUserUid());

        productRef.orderByChild("productId").equalTo(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot productSnapshot = dataSnapshot.getChildren().iterator().next();
                    String quantityStr = productSnapshot.child("quantity").getValue(String.class);
                    Log.d(TAG, "Quantity Current Value: " + quantityStr);

                    if (quantityStr != null) {
                        int currentQuantity = Integer.parseInt(quantityStr);

                        // Now you have the quantity value as an integer
                        int updatedQuantity = currentQuantity - quantityToDeduct;

                        if (updatedQuantity >= 0) {
                            // Update the product quantity in the database as a string
                            String updatedQuantityStr = Integer.toString(updatedQuantity);
                            productSnapshot.child("quantity").getRef().setValue(updatedQuantityStr);

                            // After updating the quantity in the database, you can update the corresponding items in the cart
                            // Implement a function to update the cart item's quantity. You'll need to find the corresponding cart item
                            // and update its quantity based on the product ID and updated quantity.
                            updateCartItemQuantity(productId, updatedQuantity);
                        } else {
                            // Handle cases where the quantity goes negative (out of stock)
                            Toast.makeText(AddToCartActivity.this, "Product out of stock: " + productSnapshot.child("productName").getValue(String.class), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle cases where quantity is not set in the database
                        Log.e(TAG, "Quantity is not set in the database.");
                    }
                } else {
                    // Handle cases where the product with the given productId doesn't exist
                    Log.e(TAG, "Product with productId " + productId + " not found in the database.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur while retrieving the quantity
                Log.e(TAG, "Failed to retrieve product quantity: " + databaseError.getMessage());
            }
        });
    }

    private void updateCartItemQuantity(String productId, int updatedQuantity) {
        // Find the corresponding cart item in your cartItems list
        for (PointOfSaleActivity.Product cartItem : cartItems) {
            if (cartItem.getProductId().equals(productId)) {
                // Update the cart item's quantity
                cartItem.setQuantity("Quantity: " + updatedQuantity);
                // Update the RecyclerView or UI to reflect the change
                cartAdapter.notifyDataSetChanged();
                break;  // Exit the loop once you've found and updated the cart item
            }
        }
    }



    private void checkUserRoleForAccess(String userUid) {
        DatabaseReference userRolesRef = FirebaseDatabase.getInstance().getReference("UserRoles")
                .child(userUid)  // Use the provided userUid here
                .child("storeOwner");
        Log.d(TAG, "User: " + userUid);

        userRolesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRoles = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "User role retrieved: " + userRoles);
                    if ("storeOwner".equals(userRoles)) {
                        // User has the "storeOwner" role, allow access
                        performCheckout();
                        Log.d(TAG, "User is a store owner. Allowing access.");
                    } else {
                        // User does not have the "storeOwner" role, show an error message
                        Log.d(TAG, "User does not have the 'storeOwner' role.");
                        Toast.makeText(AddToCartActivity.this, "Only Store Owners can access this feature.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // User role data does not exist, handle it as needed
                    Log.d(TAG, "User role data not found.");
                    //Toast.makeText(AddToCartActivity.this, "User role not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(AddToCartActivity.this, "Error checking user role.", Toast.LENGTH_LONG).show();
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

    private void clearCart() {
        // Clear the cart items in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CART_ITEMS_KEY, new HashSet<>());
        editor.apply();

        // Clear the list of cart items in your adapter
        cartAdapter.clearCart();

        // Update the total display to show $0.00
        totalTextView.setText("Total: $0.00");
    }


    // Adapter for displaying cart items in RecyclerView
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        private List<PointOfSaleActivity.Product> cartItems;
        private double total;
        private TextView totalTextView;

        public CartAdapter(List<PointOfSaleActivity.Product> cartItems, TextView totalTextView) {
            this.cartItems = cartItems;
            this.total = calculateTotalPrice(cartItems);
            this.totalTextView = totalTextView;
            updateTotalTextView();
        }

        private double calculateTotalPrice(List<PointOfSaleActivity.Product> items) {
            double newTotal = 0.0;
            for (PointOfSaleActivity.Product cartItem : items) {
                newTotal += cartItem.getTotal();
            }
            return newTotal;
        }

        private void updateCartTotal() {
            double newTotal = 0.0;
            for (PointOfSaleActivity.Product cartItem : cartItems) {
                newTotal += cartItem.getTotal();
            }
            total = newTotal;
            updateTotalTextView();
        }

        public void clearCart() {
            cartItems.clear();
            notifyDataSetChanged();
            total = 0.0;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for each cart item view
            return new CartViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cart_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            // Bind data to the cart item view
            PointOfSaleActivity.Product product = cartItems.get(position);
            holder.bind(product);
        }

        private void updateTotalTextView() {
            totalTextView.setText("Total: ₱" + formatPrice(total));
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        public void updateCartItems(List<PointOfSaleActivity.Product> updatedCartItems) {
            // Update the cart items and refresh the adapter
            cartItems = updatedCartItems;
            total = calculateTotalPrice(cartItems);
            updateTotalTextView();
            notifyDataSetChanged();
        }

        // ViewHolder for each cart item in RecyclerView
        class CartViewHolder extends RecyclerView.ViewHolder {

            private ImageView productImage;
            private TextView productNameTextView;
            private TextView productDescriptionTextView;
            private TextView productCategoryTextView;
            private TextView productQuantityTextView;
            private TextView productPriceTextView;
            private TextView itemTotalTextView;
            // Find the buttons
            private Button buttonIncreaseQuantity;
            private Button buttonDecreaseQuantity;
            private int currentQuantity = 0;
            private boolean quantityChanged = false;
            private boolean quantityChangeInProgress = false;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.itemDefaultImageView);
                productNameTextView = itemView.findViewById(R.id.itemNameTextView);
                productDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                productCategoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
                productQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
                productPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
                buttonDecreaseQuantity = itemView.findViewById(R.id.buttonCartItemLayoutDec);
                buttonIncreaseQuantity = itemView.findViewById(R.id.buttonCartItemLayoutInc);
                itemTotalTextView = itemView.findViewById(R.id.itemTotalTextView);

                buttonIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!quantityChangeInProgress) {
                            quantityChangeInProgress = true;
                            incrementQuantity();
                            quantityChanged = true;
                            updateCartItemsList(true);
                            quantityChangeInProgress = false;
                        }
                    }
                });

// Set a click listener for the "Decrease Quantity" button
                buttonDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!quantityChangeInProgress) {
                            quantityChangeInProgress = true;
                            if (currentQuantity > 0) {
                                decrementQuantity();
                                quantityChanged = true;
                            } else {
                                // If quantity is zero, show a confirmation dialog to remove the item
                                showRemoveItemDialog();
                            }
                            updateCartItemsList(true);
                            quantityChangeInProgress = false;
                        }
                    }
                });
            }

            private void showRemoveItemDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Remove Item");
                builder.setMessage("The quantity of this item is zero. Do you want to remove it from the cart?");
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove the item from the cartItems list
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < cartItems.size()) {
                            cartItems.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, cartItems.size());
                            updateCartTotal();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }

            private void updateItemTotalTextView(PointOfSaleActivity.Product product) {
                int quantity = currentQuantity;
                double price = extractPriceFromString(product.getPrice());
                double itemTotal = quantity * price;
                itemTotalTextView.setText("Item Total: ₱" + formatPrice(itemTotal));
            }

            private double extractPriceFromString(String priceString) {
                try {
                    String[] priceParts = priceString.split(":");
                    if (priceParts.length == 2) {
                        String priceValue = priceParts[1].trim();
                        return Double.parseDouble(priceValue);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Invalid price format for product: " + priceString);
                }
                return 0.0; // Return 0.0 for any errors
            }

            private int extractQuantityFromString(String quantityString) {
                try {
                    String[] quantityParts = quantityString.split(":");
                    if (quantityParts.length == 2) {
                        String quantityValue = quantityParts[1].trim();
                        return Integer.parseInt(quantityValue);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Invalid quantity format for product: " + quantityString);
                }
                return 0; // Return 0 for any errors
            }


            private void updateQuantityTextView() {
                // Just update the text of the existing textViewPOSQuantity
                productQuantityTextView.setText("Quantity: " + currentQuantity);
            }

            private void decrementQuantity() {
                if (currentQuantity > 0) {
                    currentQuantity--;
                    updateQuantityTextView();
                    PointOfSaleActivity.Product product = cartItems.get(getAdapterPosition());
                    updateItemTotalTextView(product);
                }
            }

            private void incrementQuantity() {
                if (currentQuantity >= 0) {
                    currentQuantity++;
                    updateQuantityTextView();
                    PointOfSaleActivity.Product product = cartItems.get(getAdapterPosition());
                    updateItemTotalTextView(product);
                }
            }

            private void updateCartItemsList(boolean buttonClicked) {
                PointOfSaleActivity.Product product = cartItems.get(getAdapterPosition());
                int quantity = currentQuantity;
                double price = extractPriceFromString(product.getPrice());
                double itemTotal = quantity * price;
                product.setTotal(itemTotal);
                if (buttonClicked) {
                    updateCartTotal(); // Update the total when an item is modified via button clicks
                }

                // Update the totalTextView to display the overall cart total
                totalTextView.setText("Total: ₱" + formatPrice(total));
            }

            public void bind(PointOfSaleActivity.Product product) {
                productNameTextView.setText(product.getProductName());
                productDescriptionTextView.setText(product.getProductDescription());
                productCategoryTextView.setText(product.getCategory());

                // Extract the quantity and price
                int quantity = extractQuantityFromString(product.getQuantity());
                double price = extractPriceFromString(product.getPrice());

                // Calculate and set the item total
                double itemTotal = quantity * price;
                itemTotalTextView.setText("Item Total: ₱" + formatPrice(itemTotal));

                // Set the currentQuantity directly from the product's quantity
                currentQuantity = quantity;

                // Update the productQuantityTextView with the current quantity
                productQuantityTextView.setText("Quantity: " + currentQuantity);

                productPriceTextView.setText(product.getPrice());

                displayProductImage(product, productImage);
            }


            private void displayProductImage(PointOfSaleActivity.Product product, ImageView imageView) {
                // Retrieve userUid and productId from the product
                String userUid = getCurrentUserUid();
                String productId = product.getProductId();

                // Create a reference to the specific image in the "ProductImages" folder
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imageRef = storage.getReference().child("ProductImages/" + userUid + "/" + productId);

                Log.d(TAG, "UserUid Image: " + userUid);
                Log.d(TAG, "Product ID Image: " + productId);
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
    }

    public class Transaction {
        private String productId;
        private String productName;
        private double price;
        private int quantity;
        private String timestamp;
        private String userUid;

        public Transaction() {
            // Default constructor required for Firebase Realtime Database
        }

        public Transaction(String productName, double price, int quantity, String timestamp, String userUid, String productId) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = timestamp;
            this.userUid = userUid;
        }

        public  String getProductId() {return productId;}

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

}
