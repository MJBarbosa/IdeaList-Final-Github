package com.example.idealist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
        cartAdapter = new CartAdapter(cartItems);
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
                    cartItemParts[0], cartItemParts[1], cartItemParts[2], cartItemParts[3], cartItemParts[4]
            );
            updatedCartItems.add(product);
        }

        // Update the cart items in the adapter
        cartAdapter.updateCartItems(updatedCartItems);
    }

    private void calculateAndDisplayTotalFromSharedPreferences() {
        Set<String> cartItemsSet = sharedPreferences.getStringSet(CART_ITEMS_KEY, new HashSet<>());
        List<PointOfSaleActivity.Product> cartItems = new ArrayList<>();

        for (String cartItem : cartItemsSet) {
            String[] cartItemParts = cartItem.split(",");

            // Extract product details from cartItemParts
            String productName = cartItemParts[0];
            String productDesc = cartItemParts[1];
            String category = cartItemParts[2];
            String productQuantity = cartItemParts[3];
            String productPrice = cartItemParts[4];

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
                    productName, productDesc, category, productQuantity, productPrice
            );
            cartItems.add(product);

            // Add a log statement to identify the cart item
            Log.d(TAG, "Cart Item: Product Name: " + productName + ", Product Description: " + productDesc + ", Category: " + category + ", Quantity: " + quantity + ", Price: " + price);
        }
        // Log the calculated total
        Log.d(TAG, "Total: " + total);

        // Update the totalTextView with the calculated total
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

            // Clear the cart and display a success message
            clearCart();
            Log.d(TAG, "Checkout successful");
            Toast.makeText(AddToCartActivity.this, "Checkout successful", Toast.LENGTH_SHORT).show();
        } else {
            // Handle case where the cart is empty
            Log.e(TAG, "Cart is empty. Nothing to checkout.");
        }

        checkUserRoleForAccess(getCurrentUserUid());
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

        // Use the orderByChild and equalTo query to find the product by productId
        productRef.orderByChild("productId").equalTo(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Since we used equalTo, dataSnapshot will point to the product with the matching productId
                    DataSnapshot productSnapshot = dataSnapshot.getChildren().iterator().next();
                    String quantityStr = productSnapshot.child("quantity").getValue(String.class);
                    Log.d(TAG, "Quantity Current Value: " + quantityStr);

                    if (quantityStr != null) {
                        int currentQuantity = Integer.parseInt(quantityStr);

                        // Now you have the quantity value, you can use it in your code
                        int updatedQuantity = currentQuantity - quantityToDeduct;

                        if (updatedQuantity >= 0) {
                            // Update the product quantity in the database as a string
                            productSnapshot.child("quantity").getRef().setValue(String.valueOf(updatedQuantity));
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
                    Toast.makeText(AddToCartActivity.this, "User role not found.", Toast.LENGTH_LONG).show();
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

        public CartAdapter(List<PointOfSaleActivity.Product> cartItems) {
            this.cartItems = cartItems;
        }

        public void clearCart() {
            cartItems.clear();
            notifyDataSetChanged();
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

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        public void updateCartItems(List<PointOfSaleActivity.Product> updatedCartItems) {
            // Update the cart items and refresh the adapter
            cartItems = updatedCartItems;
            notifyDataSetChanged();
        }

        // ViewHolder for each cart item in RecyclerView
        class CartViewHolder extends RecyclerView.ViewHolder {

            private TextView productNameTextView;
            private TextView productDescriptionTextView;
            private TextView productCategoryTextView;
            private TextView productQuantityTextView;
            private TextView productPriceTextView;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.itemNameTextView);
                productDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                productCategoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
                productQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
                productPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            }

            public void bind(PointOfSaleActivity.Product product) {
                productNameTextView.setText(product.getProductName());
                productDescriptionTextView.setText(product.getProductDescription());
                productCategoryTextView.setText(product.getCategory());
                productQuantityTextView.setText(product.getQuantity());
                productPriceTextView.setText(product.getPrice());
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
